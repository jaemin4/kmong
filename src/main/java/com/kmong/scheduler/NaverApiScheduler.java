package com.kmong.scheduler;

import com.kmong.domain.order.OrderCommand;
import com.kmong.domain.order.OrderService;
import com.kmong.domain.outbox.OutBoxService;
import com.kmong.domain.outbox.OutboxCommand;
import com.kmong.domain.outbox.SendStatus;
import com.kmong.infra.naver.NaverAPIClient;
import com.kmong.support.properties.EsimProperties;
import com.kmong.support.utils.JsonUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import static com.kmong.support.utils.CommUtils.*;
import static com.kmong.support.utils.JsonPathUtils.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import javax.print.DocFlavor;

@Slf4j
@Component
@RequiredArgsConstructor
public class NaverApiScheduler {

    private final OrderService orderService;
    private final NaverAPIClient naverAPIClient;
    private final OutBoxService outBoxService;
    private final EsimProperties esimProperties;
    private final RestTemplate restTemplate = new RestTemplate();
    private final HttpHeaders httpHeaders = new HttpHeaders();


    private static final DateTimeFormatter NAVER_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    private static final String BREAK_NULL = "breakNull";

    private ZonedDateTime lastFetchedTime;
    private String accessToken;

    @PostConstruct
    public void initBaseTime() {
        lastFetchedTime = ZonedDateTime.of(2025, 8, 25, 0, 0, 0, 0, ZonedDateTime.now().getZone());
        refreshToken();
    }

    /** 매 1분마다 실행 */
    @Scheduled(cron = "0 * * * * *")
    public void orderSaveSchedule() {
        ZonedDateTime from = lastFetchedTime;
        //ZonedDateTime to = ZonedDateTime.now();
        ZonedDateTime to = from.plusDays(1);
        log.info("[NAVER] 주문 조회 시작: {} ~ {}", from, to);

        executeWithRetry(from, to, LocalDateTime.of(2025, 8, 25, 0, 0, 0));
        lastFetchedTime = to;
    }

    /** 실패 시 토큰 재발급 포함 */
    private void executeWithRetry(ZonedDateTime from, ZonedDateTime to, LocalDateTime logicStartTime) {
        String f = NAVER_FMT.format(from);
        String t = NAVER_FMT.format(to);

        try {
            processOrders(f, t, logicStartTime);
        } catch (Exception e) {
            log.warn("[NAVER] fetch 실패: {} ~ {}, 토큰 재발급 후 재시도", f, t, e);
            refreshToken();
            try {
                processOrders(f, t, logicStartTime);
            } catch (Exception retryEx) {
                log.error("[NAVER] 재시도 실패: {} ~ {}", f, t, retryEx);
            }
        }
    }

    /** 토큰 재발급 */
    private void refreshToken() {
        this.accessToken = naverAPIClient.getToken();
        log.info("[NAVER] Access Token 갱신 완료");
    }

    /** API 조회 + 주문 저장 + 알림 발송 */
    private void processOrders(String from, String to, LocalDateTime logicStartTime) {
        List<Map<String, Object>> orders = naverAPIClient.fetchOrders(accessToken, from, to);

        if (orders == null) {
            log.info("[NAVER] API 응답이 null → 데이터 없음으로 간주 (토큰 재발급 불필요)");
            return;
        }

        if (orders.isEmpty()) {
            log.info("[NAVER] 주문 데이터 없음 → {} ~ {}", from, to);
            return;
        }

        log.info("[NAVER] API 응답 {}건", orders.size());

        // 1) orderId 기준 그룹핑
        Map<String, List<Map<String, Object>>> groupedByOrder = orders.stream()
                .collect(Collectors.groupingBy(row -> getS(row, "content.order.orderId")));

        // 2) 그룹 내 병합 (메인 + 추가구성 옵션 이어붙이기)
        List<Map<String, Object>> mergedOrders = groupedByOrder.values().stream()
                .map(this::mergeOrderGroup)
                .toList();

        log.info("합쳐진 mergeOrderGroup : {}", mergedOrders);

        log.info("[NAVER] 병합 완료 → {}건 → {}건으로 압축됨", orders.size(), mergedOrders.size());

        int total = mergedOrders.size();
        int saved = 0;
        int skipped = 0;

        for (Map<String, Object> mainRow : mergedOrders) {
            String orderId = getS(mainRow, "content.order.orderId");
            String productOrderId = getS(mainRow, "content.productOrder.productOrderId");

            if (isBlank(orderId) || orderService.existsMainByOrderId(orderId)) {
                skipped++;
                continue;
            }

            //todo 2.1 OrderEsim 호출 후 반환된 orderId 메인에 저장
            String esimOrderId = "example any";

            try {
                String orderDateStr = getS(mainRow, "content.order.orderDate");
                LocalDateTime orderDate = LocalDateTime.parse(orderDateStr.substring(0, 19));
                orderService.registerOrderMain(mapToRegisterOrderMain(mainRow, esimOrderId));
                saved++;

                if (orderDate.isAfter(logicStartTime)) {
                    processNotification(mainRow, productOrderId);
                } else {
                    log.info("[NAVER] 이전 주문 ({} < {}) → 메일/문자 생략", orderDate, logicStartTime);
                }

            } catch (Exception e) {
                log.warn("[NAVER] Insert 실패 또는 중복: {}", orderId);
                skipped++;
            }
        }

        log.info("[NAVER] 완료 → total={}, saved={}, skipped={}", total, saved, skipped);
    }

    /** 그룹 내 병합: 메인(조합형옵션상품) 1개 + 추가구성상품의 productOption 이어붙이기 */
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

        log.info("서브옵션 : {}", JsonUtils.toJson(subOptions));

        if (!subOptions.isEmpty()) {
            Map<String, Object> content = (Map<String, Object>) mainRow.get("content");
            if (content != null) {
                Map<String, Object> productOrder = (Map<String, Object>) content.get("productOrder");
                if (productOrder != null) {
                    String mainOption = (String) productOrder.get("productOption");
                    String mergedOption = (mainOption == null ? "" : mainOption)
                            + ",,,," + String.join(",,,,", subOptions);

                    productOrder.put("productOption", mergedOption);
                    log.info("머지 옵션 : {}", mergedOption);
                }
            }
        }

        log.info("메인 로우 : {}", JsonUtils.toJson(mainRow));
        return mainRow;
    }

    /** 이메일/문자 알림 처리 */
    private void processNotification(Map<String, Object> row, String productOrderId) {
        Map<String,Object> result = callApiCompany();
        boolean isApiRequest = (boolean) result.get("isSuccess");

        if(isApiRequest){

        }

        String phone = getS(row, "content.order.ordererTel");
        String email = "eheh25877@gmail.com";
        boolean enabledEmail = !email.equals("disableEmail");

/*        outBoxService.registerOrderOutBox(OutboxCommand.RegisterOrderOutbox.of(
                productOrderId,
                "testId",
                isApiRequest ? SendStatus.SUCCESS : SendStatus.FAIL,
                enabledEmail,
                email,
                phone,
                row
        ));*/


    }

    /** 이메일 추출 */
    private String extractEmail(Map<String, Object> row) {
        String memo = getS(row, "content.productOrder.shippingMemo");
        if (memo == null) return BREAK_NULL;

        Matcher m = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
                .matcher(memo);
        if (m.find()) {
            String email = m.group();
            log.debug("이메일 추출 성공: {}", email);
            return email;
        }
        return BREAK_NULL;
    }

    /** 주문정보 매핑 */
    private OrderCommand.RegisterOrderMain mapToRegisterOrderMain(Map<String, Object> row,String esimOrderId) {
        Double price = getD(row, "content.productOrder.unitPrice");
        if (price == null) price = 0.0;

        return OrderCommand.RegisterOrderMain.of(
                getS(row, "content.productOrder.productOrderId"),
                getS(row, "content.order.orderDate"),
                getS(row, "content.order.orderId"),
                getS(row, "content.order.ordererName"),
                getS(row, "content.productOrder.shippingAddress.name"),
                firstNonEmpty(getS(row, "content.productOrder.inflowPath"),
                        getS(row, "content.order.payLocationType")),
                getS(row, "content.productOrder.productOption"),
                getI(row, "content.productOrder.quantity"),
                price,
                "NTD",
                getS(row, "content.productOrder.shippingMemo"),
                firstNonEmpty(getS(row, "content.productOrder.productOrderStatus"),
                        getS(row, "content.productOrder.placeOrderStatus")),
                "NOT_ISSUED",
                esimOrderId
        );
    }

    private Map<String, Object> callApiCompany() {
        String apiUrl = "https://tfmshippingsys.fastmove.com.tw/Api/QuoteMg/myQueryAll";

        Map<String, Object> result = new HashMap<>();
        boolean isSuccess = false;

        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("merchantId", esimProperties.getMerchantId());
            requestBody.put("encStr", esimProperties.getEncStr());

            httpHeaders.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, httpHeaders);

            // API 호출
            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl, HttpMethod.POST, entity, Map.class
            );

            Map<String, Object> body = response.getBody();
            Object productList = body != null ? body.get("prodList") : null;

            if (productList == null) {
                log.warn("[WARN] productList is null — API 요청 실패로 간주");
            } else {
                log.info("[INFO] API 호출 성공");
                isSuccess = true;
            }

            result.put("body", body);
            result.put("isSuccess", isSuccess);

        } catch (Exception e) {
            log.error("[ERROR] API 요청 중 예외 발생: {}", e.getMessage());
            result.put("body", null);
            result.put("isSuccess", false);
        }

        return result;
    }

}
