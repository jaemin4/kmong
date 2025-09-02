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
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import static com.kmong.support.utils.JsonPathUtils.*; // <-- JsonPathUtils static import

@Slf4j
@Component
@RequiredArgsConstructor
public class NaverApiScheduler {

    private final OrderService orderService;
    private final NaverAPIClient naverAPIClient;

    private static final DateTimeFormatter NAVER_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    private ZonedDateTime lastFetchedTime;

    @PostConstruct
    public void initBaseTime() {
        //lastFetchedTime = ZonedDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        lastFetchedTime = ZonedDateTime.of(
                2025, 8, 30,
                0, 0, 0, 0,
                ZonedDateTime.now().getZone()
        );
    }

    //@Scheduled(cron = "0 */3 * * * *")
    @Scheduled(cron = "0 * * * * *") // 매 1분마다 실행
    public void orderSaveSchedule() {
        ZonedDateTime fromTime = lastFetchedTime;
        ZonedDateTime toTime   = fromTime.plusDays(1); // 1시간 단위로 범위 확장

        // 테스트용: 날짜를 2025-09-01로 고정
        fromTime = fromTime.withYear(2025).withMonth(9).withDayOfMonth(1);
        toTime   = toTime.withYear(2025).withMonth(9).withDayOfMonth(1);

        String from = NAVER_FMT.format(fromTime);
        String to   = NAVER_FMT.format(toTime);

        String token = naverAPIClient.getToken();
        try {
            UpsertResult r = fetchAndUpsert(token, from, to);
            log.info("[NAVER][TEST] {}~{} fetched={}, saved={}, skipped={}",
                    from, to, r.total, r.saved, r.skipped);
        } catch (Exception e) {
            log.error("[NAVER][TEST] fetch failed {}~{}", from, to, e);
        }

        // 다음 루프를 위해 기준 업데이트
        lastFetchedTime = toTime;
    }
    /** 토큰으로 조회해서 DB upsert(존재시 skip) */
    private UpsertResult fetchAndUpsert(String accessToken, String from, String to) {
        List<Map<String, Object>> rows = naverAPIClient.fetchOrders(accessToken, from, to);
        int total = 0, saved = 0, skipped = 0;

        for (Map<String, Object> row : rows) {
            total++;
            String productOrderId = getS(row, "content.productOrder.productOrderId");

            if (productOrderId == null || productOrderId.isBlank()) {
                log.debug("[NAVER] skip: missing productOrderId");
                skipped++;
                continue;
            }

            if (orderService.existsMainByProductOrderId(productOrderId)) {
                skipped++;
                continue;
            }

            OrderCommand.RegisterOrderMain cmd = mapToRegisterOrderMain(row);
            orderService.registerOrderMain(cmd);
            saved++;
        }
        return new UpsertResult(total, saved, skipped);
    }

    private OrderCommand.RegisterOrderMain mapToRegisterOrderMain(Map<String, Object> row) {
        String productOrderId  = getS(row, "content.productOrder.productOrderId");
        String orderDate       = getS(row, "content.order.orderDate");
        String orderNumber     = getS(row, "content.order.orderId");
        String ordererName     = getS(row, "content.order.ordererName");
        String receiverName    = getS(row, "content.productOrder.shippingAddress.name");
        String purchaseChannel = firstNonEmpty(
                getS(row, "content.productOrder.inflowPath"),
                getS(row, "content.order.payLocationType")
        );
        String productOption   = getS(row, "content.productOrder.productOption");
        Integer quantity       = getI(row, "content.productOrder.quantity");
        Double originalPrice   = getD(row, "content.productOrder.unitPrice");
        String currency        = "NTD"; // 요구사항대로 표시
        String message         = getS(row, "content.productOrder.shippingMemo");
        String paymentStatus   = firstNonEmpty(
                getS(row, "content.productOrder.productOrderStatus"),
                getS(row, "content.productOrder.placeOrderStatus")
        );
        String issueStatus     = "NOT_ISSUED";

        return OrderCommand.RegisterOrderMain.of(
                productOrderId, orderDate, orderNumber, ordererName, receiverName, purchaseChannel,
                productOption, quantity, originalPrice, currency, message,
                paymentStatus, issueStatus
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
