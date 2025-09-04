package com.kmong.scheduler;

import com.kmong.domain.notification.Notification;
import com.kmong.domain.notification.NotificationService;
import com.kmong.domain.order.OrderCommand;
import com.kmong.domain.order.OrderService;
import com.kmong.domain.outbox.OutBoxService;
import com.kmong.domain.outbox.OutboxCommand;
import com.kmong.domain.outbox.SendStatus;
import com.kmong.infra.email.EmailConsumer;
import com.kmong.infra.email.EmailConsumerCommand;
import com.kmong.infra.naver.NaverAPIClient;
import com.kmong.support.constants.RabbitmqConstants;
import com.kmong.support.utils.MailUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
                25, 0, 0,
                0, 0, ZonedDateTime.now().getZone());

        accessToken = naverAPIClient.getToken();
        log.info("check access : {}",accessToken);
    }

    @Scheduled(cron = "0 * * * * *")
    // 1분마다 실행 (운영 시 "0 */3 * * * *")
    //@Scheduled(cron = "0 */3 * * * *")
    public void orderSaveSchedule() {
        ZonedDateTime fromTime = lastFetchedTime;
        //ZonedDateTime toTime = fromTime.plusMinutes(3); // 테스트: 1일 단위 (운영: plusMinutes(3))
        ZonedDateTime toTime = fromTime.plusDays(1);

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
            String phoneNumber = getS(row, "content.order.ordererTel");
            if (isBlank(productOrderId) || orderService.existsMainByProductOrderId(productOrderId)) {
                skipped++;
                continue;
            }

            try {
                OrderCommand.RegisterOrderMain cmd = mapToRegisterOrderMain(row);
                orderService.registerOrderMain(cmd);
                saved++;
            } catch (Exception e) {
                log.warn("[NAVER] duplicate or failed insert: {}", productOrderId);
                skipped++;
                continue;
            }

            // 이메일 처리
            String email = extractEmail(row);

            Notification notification = notificationService.get().getNotification();
            String subject = notification.getSubject();
            String template = notification.getContent();
            String keyString = notification.getKeyString();
            String body = renderByKeys(template, keyString, row);

            if (!"breakNull".equals(email)) {
                outBoxService.registerOrderOutBox(OutboxCommand.RegisterOrderOutbox.of(
                        productOrderId,
                        "testId",
                        SendStatus.SUCCESS,
                        true,
                        email,
                        phoneNumber
                ));

                rabbitTemplate.convertAndSend(
                        RabbitmqConstants.EXCHANGE_MAIL,
                        RabbitmqConstants.ROUTING_MAIL_SEND,
                        EmailConsumerCommand.Issue.of(
                                productOrderId, email, subject, body, MailUtils.setFrom
                        )
                );
            } else {
                outBoxService.registerOrderOutBox(OutboxCommand.RegisterOrderOutbox.of(
                        productOrderId,
                        "testId",
                        SendStatus.SUCCESS,
                        true,
                        email,
                        phoneNumber
                ));

                rabbitTemplate.convertAndSend(
                        RabbitmqConstants.EXCHANGE_MAIL,
                        RabbitmqConstants.ROUTING_MAIL_SEND,
                        EmailConsumerCommand.Issue.of(
                                productOrderId, email, subject, body, MailUtils.setFrom
                        )
                );

                log.info("메일 주소 없음 → 문자 발송 예정");
            }
        }
        return new UpsertResult(total, saved, skipped);
    }

    /** 이메일 추출 로직 */
    private String extractEmail(Map<String, Object> row) {
        try {
            String shippingMemo = getS(row, "content.productOrder.shippingMemo");
            if (shippingMemo == null) return "breakNull";

            Pattern pattern = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
            Matcher matcher = pattern.matcher(shippingMemo);

            if (matcher.find()) {
                String email = matcher.group();
                log.info("이메일 추출 성공: {}", email);
                return "eheh4756@naver.com";
            }
            return "breakNull";
        } catch (Exception e) {
            return "breakNull";
        }
    }

    /** Outbox 등록 + 메일 발송 */
    private void processEmailSend(String productOrderId, Map<String, Object> row, String email, String phoneNumber) {

    }

    /** 유틸 */
    private boolean isBlank(String str) {
        return (str == null || str.isBlank());
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




    // NaverApiScheduler 내부 어딘가(필드/메서드들 사이)에 추가
// 키 -> JsonPath 매핑 (필요한 만큼 보강하세요)
    private static final Map<String, String> KEY_PATHS = Map.ofEntries(
            Map.entry("productOrderId",  "content.productOrder.productOrderId"),
            Map.entry("orderDate",       "content.order.orderDate"),
            Map.entry("orderNumber",     "content.order.orderId"),
            Map.entry("ordererName",     "content.order.ordererName"),
            Map.entry("receiverName",    "content.productOrder.shippingAddress.name"),
            Map.entry("purchaseChannel", "content.productOrder.inflowPath"),
            Map.entry("productOption",   "content.productOrder.productOption"),
            Map.entry("quantity",        "content.productOrder.quantity"),
            Map.entry("originalPrice",   "content.productOrder.unitPrice"),
            Map.entry("currency",        "content.productOrder.currency"), // 없으면 빈값
            Map.entry("message",         "content.productOrder.shippingMemo"),
            Map.entry("paymentStatus",   "content.productOrder.productOrderStatus"),
            Map.entry("issueStatus",     "content.productOrder.issueStatus"),
            // 상세 필드 (실제 경로 다르면 수정)
            Map.entry("activationDate",  "content.productOrder.activationDate"),
            Map.entry("expiryDate",      "content.productOrder.expiryDate"),
            Map.entry("iddicNumber",     "content.productOrder.iddicNumber"),
            Map.entry("smdpAddress",     "content.productOrder.smdpAddress"),
            Map.entry("activationCode",  "content.productOrder.activationCode"),
            Map.entry("apn",             "content.productOrder.apn"),
            Map.entry("dataUsage",       "content.productOrder.dataUsage"),
            // 외부에서 계산/추가되는 값(없으면 빈값 처리)
            Map.entry("esimQrImage",     "content.productOrder.esimQrImage"),
            Map.entry("checkCode",       "content.productOrder.checkCode")
    );

    private String renderByKeys(String template, String keyCsv, Map<String, Object> row) {
        if (template == null || template.isBlank()) return "";
        if (keyCsv == null || keyCsv.isBlank()) return template;

        String result = template;
        for (String rawKey : keyCsv.split(",")) {
            String key = rawKey.trim();
            if (key.isEmpty()) continue;

            String path = KEY_PATHS.get(key);
            String value = null;

            if (path != null) {
                // 숫자/실수도 문자열로 안전하게 변환
                Object v = getO(row, path); // JsonPathUtils.getO(Object)
                if (v == null) {
                    // 보조 경로들(예: 결제상태/유입경로 등) 필요 시 보조 처리
                    if ("purchaseChannel".equals(key)) {
                        value = firstNonEmpty(getS(row, "content.productOrder.inflowPath"),
                                getS(row, "content.order.payLocationType"));
                    } else if ("paymentStatus".equals(key)) {
                        value = firstNonEmpty(getS(row, "content.productOrder.productOrderStatus"),
                                getS(row, "content.productOrder.placeOrderStatus"));
                    }
                } else {
                    value = String.valueOf(v);
                }
            }

            if (value == null) value = "";
            result = result.replace("{" + key + "}", value);
        }
        return result;
    }

    private Object getO(Map<String, Object> row, String path) {
        try {
            String s = getS(row, path);
            if (s != null) return s;
            Integer i = getI(row, path);
            if (i != null) return i;
            Double d = getD(row, path);
            if (d != null) return d;
        } catch (Exception ignored) {}
        return null;
    }




}
