package com.kmong.scheduler;

import com.kmong.domain.order.OrderCommand;
import com.kmong.domain.order.OrderService;
import com.kmong.infra.naver.NaverAPIClient;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import static com.kmong.support.utils.JsonPathUtils.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class NaverApiScheduler {

    private final OrderService orderService;
    private final NaverAPIClient naverAPIClient;
    private static final DateTimeFormatter NAVER_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");


    private ZonedDateTime lastFetchedTime;
    private String accessToken;

    /** 시작 시점 초기화 */
    @PostConstruct
    public void initBaseTime() {
        // 운영용: 현재 시각 기준
        // lastFetchedTime = ZonedDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        // 테스트용: 8/30 00:00 기준
        lastFetchedTime = ZonedDateTime.of(
                2025, 8,
                30, 0, 0,
                0, 0, ZonedDateTime.now().getZone());

        accessToken = naverAPIClient.getToken();
        log.info("check access : {}",accessToken);
    }

    @Scheduled(cron = "0 * * * * *") // 1분마다 실행 (운영 시 "0 */3 * * * *")
    public void orderSaveSchedule() {
        ZonedDateTime fromTime = lastFetchedTime;
        ZonedDateTime toTime = fromTime.plusDays(1); // 테스트: 1일 단위 (운영: plusMinutes(3))

        String from = NAVER_FMT.format(fromTime);
        String to = NAVER_FMT.format(toTime);

        try {
            UpsertResult r = fetchAndUpsert(accessToken, from, to);
            log.info("[NAVER] {} ~ {} → fetched={}, saved={}, skipped={}",
                    from, to, r.total, r.saved, r.skipped);
        } catch (Exception e) {
            log.error("[NAVER] fetch failed {} ~ {}", from, to, e);
            // 토큰 만료 가능 → 새로 발급 후 재시도
            accessToken = naverAPIClient.getToken();
            try {
                UpsertResult retry = fetchAndUpsert(accessToken, from, to);
                log.info("[NAVER] retry {} ~ {} → fetched={}, saved={}, skipped={}",
                        from, to, retry.total, retry.saved, retry.skipped);


            } catch (Exception ex) {
                log.error("[NAVER] retry failed {} ~ {}", from, to, ex);
            }
        }



        lastFetchedTime = toTime;
    }

    /** 조회 후 DB 저장 */
    private UpsertResult fetchAndUpsert(String accessToken, String from, String to) {
        List<Map<String, Object>> rows = naverAPIClient.fetchOrders(accessToken, from, to);
        int total = 0, saved = 0, skipped = 0;

        for (Map<String, Object> row : rows) {
            total++;
            String productOrderId = getS(row, "content.productOrder.productOrderId");

            if (productOrderId == null || productOrderId.isBlank()) {
                skipped++;
                continue;
            }

            if (orderService.existsMainByProductOrderId(productOrderId)) {
                skipped++;
                continue;
            }

            try {
                OrderCommand.RegisterOrderMain cmd = mapToRegisterOrderMain(row);
                orderService.registerOrderMain(cmd);
                saved++;
            } catch (Exception e) {
                // Unique Key 제약 위반 등 → 스킵 처리
                log.warn("[NAVER] duplicate or failed insert: {}", productOrderId);
                skipped++;
            }

            // 회사측 API 호출(동기) 성공 or 실패 여부 DB 저장
            // 카카오톡 발송 API(비동기) OutBox
            // 이메일 발송 API(비동기) OUTBOX
            // 네이버 스마트 스토어 주문 발송 처리 OutBox

        }
        return new UpsertResult(total, saved, skipped);
    }

    private OrderCommand.RegisterOrderMain mapToRegisterOrderMain(Map<String, Object> row) {
        return OrderCommand.RegisterOrderMain.of(
                getS(row, "content.productOrder.productOrderId"),
                getS(row, "content.order.orderDate"),
                getS(row, "content.order.orderId"),
                getS(row, "content.order.ordererName"),
                getS(row, "content.productOrder.shippingAddress.name"),
                firstNonEmpty(
                        getS(row, "content.productOrder.inflowPath"),
                        getS(row, "content.order.payLocationType")),
                getS(row, "content.productOrder.productOption"),
                getI(row, "content.productOrder.quantity"),
                getD(row, "content.productOrder.unitPrice"),
                "NTD", // 고정 요구사항
                getS(row, "content.productOrder.shippingMemo"),
                firstNonEmpty(
                        getS(row, "content.productOrder.productOrderStatus"),
                        getS(row, "content.productOrder.placeOrderStatus")),
                "NOT_ISSUED"
        );
    }

    private String firstNonEmpty(String a, String b) {
        if (a != null && !a.isBlank() && !"null".equalsIgnoreCase(a)) return a;
        if (b != null && !b.isBlank() && !"null".equalsIgnoreCase(b)) return b;
        return null;
    }

    /** 결과 요약 */
    private record UpsertResult(int total, int saved, int skipped) {}
}
