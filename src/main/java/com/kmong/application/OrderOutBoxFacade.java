package com.kmong.application;

import com.kmong.domain.notification.NotificationService;
import com.kmong.domain.order.OrderCommand;
import com.kmong.domain.order.OrderService;
import com.kmong.domain.outbox.OrderOutbox;
import com.kmong.domain.outbox.OutBoxService;
import com.kmong.domain.outbox.OutboxCommand;
import com.kmong.domain.outbox.SendStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Transactional
    public void processOfCallBack(Map<String,Object> payload){
        // todo 0. orderValid
        String orderId = payload.get("orderId") != null ? payload.get("orderId").toString() : null;
        List<Map<String, Object>> itemList = (List<Map<String, Object>>) payload.get("itemList");

        if (itemList == null || itemList.isEmpty() || orderId == null) {
            log.warn("[CALLBACK] itemList가 비어 있습니다. orderId={}", orderId);
            return;
        }

        // todo 1. outBoxValid
        OrderOutbox orderOutbox = outBoxService.findByOrderId(orderId);
        int retryCount = 0;

        while (orderOutbox == null && retryCount < 5) {
            orderOutbox = outBoxService.findByOrderId(orderId);
            if (orderOutbox != null) {
                log.info("[OUTBOX] 조회 성공 → orderId={}, attempt={}", orderId, retryCount + 1);
                break;
            }

            retryCount++;
            log.info("[OUTBOX] 데이터 미존재 → {}번째 재시도 (0.5초 대기)", retryCount);
            try {
                Thread.sleep(500); // 0.5초 대기
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("[OUTBOX] 재시도 대기 중 인터럽트 발생");
                break;
            }
        }

        if (orderOutbox == null) {
            log.error("[OUTBOX] 5회 재시도 후에도 데이터 조회 실패 → orderId={}", orderId);
            return;
        }

        // todo 2. orderDetailRegister
        orderService.registerOrderDetail(OrderCommand.RegisterOrderDetail.of(orderId));

        for (Map<String, Object> map : itemList) {
            try {
                OrderCommand.RegisterEsimDetail command = OrderCommand.RegisterEsimDetail.of(
                        orderId,
                        map.get("iccid") != null ? map.get("iccid").toString() : null,
                        map.get("productName") != null ? map.get("productName").toString() : null,
                        map.get("qrcode") != null ? map.get("qrcode").toString() : null,
                        map.get("rcode") != null ? map.get("rcode").toString() : null,
                        map.get("code") != null ? Integer.parseInt(map.get("code").toString()) : 0,
                        map.get("msg") != null ? map.get("msg").toString() : null,
                        map.get("qrcodeContent") != null ? map.get("qrcodeContent").toString() : null,
                        map.get("salePlanDays") != null ? Integer.parseInt(map.get("salePlanDays").toString()) : 0,
                        map.get("pin1") != null ? map.get("pin1").toString() : null,
                        map.get("pin2") != null ? map.get("pin2").toString() : null,
                        map.get("puk1") != null ? map.get("puk1").toString() : null,
                        map.get("puk2") != null ? map.get("puk2").toString() : null,
                        map.get("cfCode") != null ? map.get("cfCode").toString() : null,
                        map.get("apnExplain") != null ? map.get("apnExplain").toString() : null
                );

                orderService.registerEsimDetail(command);

            } catch (Exception e) {
                log.error("[CALLBACK] eSIM 상세 저장 중 오류 발생 (orderId={}): {}", orderId, e.getMessage(), e);
            }
        }

       // todo 3. outBoxRegister
        outBoxService.updateOrderOutBox(
                OutboxCommand.Update.of(
                        orderId,
                        null,
                        null,
                        null,
                        null
                )
        );
       // todo 4. emailEvent(outBoxUpdate)
        if(!orderOutbox.getEmailStatus().equals(SendStatus.SKIP)){
            //이메일 event
        }

        // todo 5. smsEvent(outBoxUpdate)
        else {

        }

       // todo 6. kakaoEvent(outBoxUpdate)


    }

}
