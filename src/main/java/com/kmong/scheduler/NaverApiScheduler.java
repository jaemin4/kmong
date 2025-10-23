package com.kmong.scheduler;

import com.kmong.domain.order.OrderCommand;
import com.kmong.domain.order.OrderService;
import com.kmong.domain.outbox.OutBoxService;
import com.kmong.domain.outbox.OutboxCommand;
import com.kmong.domain.outbox.SendStatus;
import com.kmong.infra.naver.NaverAPIClient;
import com.kmong.support.properties.EsimProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import static com.kmong.scheduler.OrderUtils.*;
import static com.kmong.support.utils.CommUtils.*;
import static com.kmong.support.utils.JsonPathUtils.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class NaverApiScheduler {

    private final OrderService orderService;
    private final NaverAPIClient naverAPIClient;
    private final OutBoxService outBoxService;
    private static final DateTimeFormatter NAVER_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    private ZonedDateTime lastFetchedTime;
    private String accessToken;
    private final OrderApiCall orderApiCall;

    /** 초기화 (첫 실행 기준 시점 세팅 + 토큰 갱신) */
    @PostConstruct
    public void initBaseTime() {
        lastFetchedTime = ZonedDateTime.of(2025, 8, 25, 0, 0, 0, 0, ZonedDateTime.now().getZone());
        refreshToken();
    }

    /** 1분마다 실행 */
    @Scheduled(cron = "0 * * * * *")
    public void orderSaveSchedule() {
        ZonedDateTime from = lastFetchedTime;
        ZonedDateTime to = from.plusMonths(1); // 운영 시: plusMinutes(3)
        log.info("[NAVER] 주문 조회 시작: {} ~ {}", from, to);

        runNaverOrderJob(from, to);
        lastFetchedTime = to;
    }

    private void runNaverOrderJob(ZonedDateTime from, ZonedDateTime to) {
        String f = NAVER_FMT.format(from);
        String t = NAVER_FMT.format(to);
        LocalDateTime logicStartTime = LocalDateTime.of(2025, 8, 25, 0, 0, 0);

        try {
            processOrders(f, t, logicStartTime);
        } catch (Exception e) {
            log.warn("[NAVER] 1차 실패: {}, 재시도 중...", e.getMessage());
            refreshToken();
            try {
                processOrders(f, t, logicStartTime);
            } catch (Exception retryEx) {
                log.error("[NAVER] 2차 재시도 실패: {}", retryEx.getMessage(), retryEx);
            }
        }
    }

    /** 토큰 재발급 */
    private void refreshToken() {
        this.accessToken = naverAPIClient.getToken();
        log.info("[NAVER] Access Token 갱신 완료");
    }

    /** === 네이버 주문 조회 + eSIM 처리 === */
    private void processOrders(String from, String to, LocalDateTime logicStartTime) {
        List<Map<String, Object>> orders = naverAPIClient.fetchOrders(accessToken, from, to);
        log.info("accessToken 엑세스 토큰 : {}", accessToken);

        if (orders == null || orders.isEmpty()) {
            log.info("[NAVER] 주문 데이터 없음 → {} ~ {}", from, to);
            return;
        }

        log.info("[NAVER] API 응답 {}건", orders.size());

        List<Map<String, Object>> mergedOrders = orders.stream()
                .collect(Collectors.groupingBy(r -> getS(r, "content.order.orderId")))
                .values().stream()
                .map(this::mergeOrderGroup)
                .toList();

        log.info("[NAVER] 병합 완료 → {}건 → {}건으로 압축됨", orders.size(), mergedOrders.size());

        int saved = 0, skipped = 0;

        for (Map<String, Object> row : mergedOrders) {
            String orderId = getS(row, "content.order.orderId");
            String productOrderId = getS(row, "content.productOrder.productOrderId");

            if (isBlank(orderId) || orderService.existsMainByOrderId(orderId)) {
                skipped++;
                continue;
            }

            try {
                LocalDateTime orderDate = LocalDateTime.parse(getS(row, "content.order.orderDate").substring(0, 19));

                // 이메일 및 전화번호 추출
                String phone = Optional.ofNullable(getS(row, "content.order.ordererTel")).orElse("disablePhoneNumber");
                String email = Optional.ofNullable(extractEmail(row)).orElse("disableEmail");

                phone = "01046887175";
                email = "eheh25877@gmail.com";

                SendStatus apiStatus = SendStatus.SUCCESS;
                String esimOrderId = null;

                if (orderDate.isAfter(logicStartTime)) {
                    Map<String, Object> payload = orderApiCall.callApiRedeem();
                    esimOrderId = (String) Optional.ofNullable(payload.get("orderId")).orElse(null);
                    if (esimOrderId == null) {
                        log.warn("[WARN] 외부 eSIM API 실패 (orderId 미반환)");
                        apiStatus = SendStatus.FAIL;
                    }
                }

                // Outbox 기록
                outBoxService.registerOrderOutBox(OutboxCommand.RegisterOrderOutbox.of(
                        orderId,
                        "/Api/SOrder/mybuyesimRedemption",
                        apiStatus,
                        email,
                        phone,
                        true
                ));

                // Main 주문 저장
                orderService.registerOrderMain(mapToRegisterOrderMain(row, esimOrderId));
                saved++;

            } catch (Exception e) {
                log.error("[NAVER] 주문 처리 중 오류: {}", e.getMessage(), e);
                skipped++;
            }
        }

        log.info("[NAVER] 처리 완료 → saved={}, skipped={}", saved, skipped);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> mergeOrderGroup(List<Map<String, Object>> rows) {
        Map<String, Object> mainRow = rows.stream()
                .filter(r -> "조합형옵션상품".equals(getS(r, "content.productOrder.productClass")))
                .findFirst()
                .orElse(rows.getFirst());

        List<String> subOptions = rows.stream()
                .filter(r -> "추가구성상품".equals(getS(r, "content.productOrder.productClass")))
                .map(r -> getS(r, "content.productOrder.productOption"))
                .filter(opt -> opt != null && !opt.isBlank())
                .toList();

        if (!subOptions.isEmpty()) {
            Map<String, Object> content = (Map<String, Object>) mainRow.get("content");
            if (content != null) {
                Map<String, Object> productOrder = (Map<String, Object>) content.get("productOrder");
                if (productOrder != null) {
                    String merged = String.join(",,,,", subOptions);
                    String mainOpt = (String) productOrder.getOrDefault("productOption", "");
                    productOrder.put("productOption", mainOpt + ",,,," + merged);
                }
            }
        }

        return mainRow;
    }







}
