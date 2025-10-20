package com.kmong.scheduler;

import com.kmong.domain.notification.Notification;
import com.kmong.domain.notification.NotificationService;
import com.kmong.domain.order.OrderCommand;
import com.kmong.domain.order.OrderService;
import com.kmong.domain.outbox.OutBoxService;
import com.kmong.domain.outbox.OutboxCommand;
import com.kmong.domain.outbox.SendStatus;
import com.kmong.infra.email.EmailConsumerCommand;
import com.kmong.infra.naver.NaverAPIClient;
import com.kmong.support.constants.RabbitmqConstants;
import com.kmong.support.utils.JsonUtils;
import com.kmong.support.utils.MailUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.kmong.support.utils.JsonPathUtils.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class NaverApiScheduler {
        private final OrderService orderService;
        private final NaverAPIClient naverAPIClient;
        private final RabbitTemplate rabbitTemplate;
        private final NotificationService notificationService;
        private final OutBoxService outBoxService;

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
            ZonedDateTime to = from.plusDays(1); // 운영 시: plusMinutes(3)
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
            log.info("[NAVER] API 응답 {}건", orders.size());

            Map<String, List<Map<String, Object>>> groupedByOrder = orders.stream()
                    .collect(Collectors.groupingBy(row -> getS(row, "content.order.orderId")));

            List<Map<String, Object>> mergedOrders = groupedByOrder.values().stream()
                    .map(rows -> {
                        Map<String, Object> mainRow = rows.stream()
                                .filter(r -> "조합형옵션상품".equals(getS(r, "content.productOrder.productClass")))
                                .findFirst()
                                .orElse(rows.get(0)); // fallback

                        List<String> subOptions = rows.stream()
                                .filter(r -> "추가구성상품".equals(getS(r, "content.productOrder.productClass")))
                                .map(r -> getS(r, "content.productOrder.productOption"))
                                .filter(opt -> opt != null && !opt.isBlank())
                                .toList();

                        if (!subOptions.isEmpty()) {
                            String mainOption = getS(mainRow, "content.productOrder.productOption");
                            mainRow.put("content.productOrder.productOption",
                                    mainOption + ",,,," + String.join(",,,,", subOptions));
                        }

                        return mainRow;
                    })
                    .toList();

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

                try {
                    String orderDateStr = getS(mainRow, "content.order.orderDate");
                    LocalDateTime orderDate = LocalDateTime.parse(orderDateStr.substring(0, 19));

                    orderService.registerOrderMain(mapToRegisterOrderMain(mainRow));
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




    /** 이메일/문자 알림 처리 */
        private void processNotification(Map<String, Object> row, String productOrderId) {
            String phone = getS(row, "content.order.ordererTel");
            //String email = extractEmail(row);
            String email = "eheh25877@gmail.com";

            Notification n = notificationService.get().getNotification();
            String body = renderByKeys(n.getContent(), n.getKeyString(), row);

            outBoxService.registerOrderOutBox(OutboxCommand.RegisterOrderOutbox.of(
                    productOrderId, "testId", SendStatus.SUCCESS, true, email, phone
            ));

            if (email.equals("eheh25877@gmail.com")) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                    @Override
                    public void afterCommit() {
                        log.info("[AFTER COMMIT] Outbox 커밋 완료 → MQ 발행 시작");
                        rabbitTemplate.convertAndSend(
                                RabbitmqConstants.EXCHANGE_MAIL,
                                RabbitmqConstants.ROUTING_MAIL_SEND,
                                EmailConsumerCommand.Issue.of(
                                        productOrderId, email, n.getSubject(), body, MailUtils.setFrom
                                )
                        );
                    }
                });
            } else {
                log.info("메일 없음 → 문자 발송 예정: {}", phone);
            }
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
    private OrderCommand.RegisterOrderMain mapToRegisterOrderMain(Map<String, Object> row) {
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
                "NOT_ISSUED"
        );
    }


    /** 템플릿 렌더링 */
        private String renderByKeys(String template, String keyCsv, Map<String, Object> row) {
            if (isBlank(template) || isBlank(keyCsv)) return template;

            String result = template;
            for (String rawKey : keyCsv.split(",")) {
                String key = rawKey.trim();
                String value = resolveValueByKey(key, row);
                result = result.replace("{" + key + "}", value == null ? "" : value);
            }
            return result;
        }

        private String resolveValueByKey(String key, Map<String, Object> row) {
            String path = KEY_PATHS.get(key);
            Object value = path != null ? getO(row, path) : null;

            if (value == null) {
                return switch (key) {
                    case "purchaseChannel" -> firstNonEmpty(
                            getS(row, "content.productOrder.inflowPath"),
                            getS(row, "content.order.payLocationType"));
                    case "paymentStatus" -> firstNonEmpty(
                            getS(row, "content.productOrder.productOrderStatus"),
                            getS(row, "content.productOrder.placeOrderStatus"));
                    default -> "";
                };
            }
            return value.toString();
        }

        /** 공통 유틸 */
        private boolean isBlank(String s) { return s == null || s.isBlank(); }
        private String firstNonEmpty(String a, String b) {
            return (a != null && !a.isBlank() && !"null".equalsIgnoreCase(a)) ? a : b;
        }

        private Object getO(Map<String, Object> row, String path) {
            try {
                Object val = getS(row, path);
                if (val != null) return val;
                if (getI(row, path) != null) return getI(row, path);
                if (getD(row, path) != null) return getD(row, path);
            } catch (Exception ignored) {}
            return null;
        }

        private static final Map<String, String> KEY_PATHS = Map.ofEntries(
                Map.entry("productOrderId", "content.productOrder.productOrderId"),
                Map.entry("orderDate", "content.order.orderDate"),
                Map.entry("orderNumber", "content.order.orderId"),
                Map.entry("ordererName", "content.order.ordererName"),
                Map.entry("receiverName", "content.productOrder.shippingAddress.name"),
                Map.entry("purchaseChannel", "content.productOrder.inflowPath"),
                Map.entry("productOption", "content.productOrder.productOption"),
                Map.entry("quantity", "content.productOrder.quantity"),
                Map.entry("originalPrice", "content.productOrder.unitPrice"),
                Map.entry("currency", "content.productOrder.currency"),
                Map.entry("message", "content.productOrder.shippingMemo"),
                Map.entry("paymentStatus", "content.productOrder.productOrderStatus"),
                Map.entry("issueStatus", "content.productOrder.issueStatus")
        );
}


