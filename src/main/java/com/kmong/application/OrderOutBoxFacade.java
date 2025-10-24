package com.kmong.application;

import com.kmong.domain.email.EmailEventCommand;
import com.kmong.domain.notification.Notification;
import com.kmong.domain.notification.NotificationService;
import com.kmong.domain.order.OrderCommand;
import com.kmong.domain.order.OrderService;
import com.kmong.domain.outbox.OrderOutbox;
import com.kmong.domain.outbox.OutBoxService;
import com.kmong.domain.outbox.OutboxCommand;
import com.kmong.domain.outbox.SendStatus;
import com.kmong.domain.sms.SmsEventCommand;
import com.kmong.support.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderOutBoxFacade {
    private final OrderService orderService;
    private final NotificationService notificationService;
    private final OutBoxService outBoxService;
    private final ApplicationEventPublisher eventPublisher;

    /* todo 체크사항
        1. payload에 orderId가 있는지
        2. 이미 주문인 경우에는 return 되는지
        3.
     */

    @Transactional
    public void processOfCallBack(Map<String, Object> payload) throws InterruptedException {
        Thread.sleep(3000); // 외부 시스템 지연 고려

        String orderId = payload.get("orderId") != null ? payload.get("orderId").toString() : null;
        List<Map<String, Object>> itemList = (List<Map<String, Object>>) payload.get("itemList");

        if (itemList == null || itemList.isEmpty() || orderId == null) {
            log.warn("[CALLBACK] itemList가 비어 있습니다. orderId={}", orderId);
            return;
        }

        log.info("orderId={}", orderId);

        // todo 1. Outbox 조회 (최초 시도)
        OrderOutbox orderOutbox = outBoxService.findByOrderId(orderId);
        if (orderOutbox == null) {
            log.warn("[OUTBOX] 최초 조회 실패 → orderId={}", orderId);
        } else {
            log.info("[OUTBOX] 최초 조회 성공 → {}", JsonUtils.toJson(orderOutbox));
        }

        // todo 2. 최대 5회까지 재시도
        int retryCount = 0;
        while (orderOutbox == null && retryCount < 5) {
            Thread.sleep(1000); // 0.5초 대기
            retryCount++;
            orderOutbox = outBoxService.findByOrderId(orderId);
            if (orderOutbox != null) {
                log.info("[OUTBOX] {}회차 재시도 성공 → {}", retryCount, orderId);
                break;
            } else {
                log.info("[OUTBOX] {}회차 재시도 실패", retryCount);
            }
        }

        // todo 3. 5회 이후에도 null이면 종료
        if (orderOutbox == null) {
            log.error("[OUTBOX] 5회 재시도 후에도 데이터 조회 실패 → orderId={}", orderId);
            return;
        }

        // todo 4. 콜백 중복 처리 방지
        if (Boolean.TRUE.equals(orderOutbox.getCallBackSuccess())) {
            log.info("[CALLBACK] 이미 처리된 콜백입니다 → orderId={}", orderId);
            return;
        }

        // todo 5. 정상 처리 로직 (OrderDetail, eSIM Detail 저장)
        //orderService.registerOrderDetail(OrderCommand.RegisterOrderDetail.of(orderId));

        for (Map<String, Object> map : itemList) {
            try {
                OrderCommand.RegisterEsimDetail command = OrderCommand.RegisterEsimDetail.of(
                        orderId,
                        (String) map.get("iccid"),
                        (String) map.get("productName"),
                        (String) map.get("qrcode"),
                        (String) map.get("rcode"),
                        (String) map.get("qrcodeContent"),
                        map.get("salePlanDays") != null ? Integer.parseInt(map.get("salePlanDays").toString()) : 0,
                        (String) map.get("pin1"),
                        (String) map.get("pin2"),
                        (String) map.get("puk1"),
                        (String) map.get("puk2"),
                        (String) map.get("cfCode"),
                        (String) map.get("apnExplain")
                );
                orderService.registerEsimDetail(command);
            } catch (Exception e) {
                log.error("[CALLBACK] eSIM 상세 저장 중 오류 발생 (orderId={}): {}", orderId, e.getMessage(), e);
            }
        }

        // todo  6. Outbox 상태 갱신
        outBoxService.updateOrderOutBox(
                OutboxCommand.Update.of(orderId, null, null, null, null, true)
        );

        // todo 7. 알림 발행
        Notification notification = notificationService.get().getNotification();

        if(orderOutbox.getEmailStatus().equals(SendStatus.PENDING)){
            eventPublisher.publishEvent(
                    EmailEventCommand.SendEmail.of(
                            orderId,
                            orderOutbox.getEmail(),
                            notification.getSubject(),
                            notification.getContent()
                    )
            );

        } else if(orderOutbox.getEmailStatus().equals(SendStatus.SKIP)){
            eventPublisher.publishEvent(
                    SmsEventCommand.Issue.of(
                            orderOutbox.getPhoneNumber(),
                            notification.getContent(),
                            orderId
                    )
            );
        }



        log.info("[CALLBACK] 처리 완료 → orderId={}", orderId);
    }


}
