package com.kmong.application;

import com.kmong.domain.email.EmailEventCommand;
import com.kmong.domain.notification.Notification;
import com.kmong.domain.notification.NotificationService;
import com.kmong.domain.order.EsimDetail;
import com.kmong.domain.order.OrderCommand;
import com.kmong.domain.order.OrderMain;
import com.kmong.domain.order.OrderService;
import com.kmong.domain.outbox.OrderOutbox;
import com.kmong.domain.outbox.OutBoxService;
import com.kmong.domain.outbox.OutboxCommand;
import com.kmong.domain.outbox.SendStatus;
import com.kmong.domain.sms.SmsEventCommand;
import com.kmong.scheduler.OrderApiCall;
import com.kmong.support.utils.JsonUtils;
import com.kmong.support.valid.OrderOutBoxFacadeValid;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderOutBoxFacade {

    private final OrderService orderService;
    private final NotificationService notificationService;
    private final OutBoxService outBoxService;
    private final ApplicationEventPublisher eventPublisher;
    private final OrderApiCall orderApiCall;

    @Transactional
    public void processCallBack2_5(Map<String, Object> payload) throws InterruptedException {
        Thread.sleep(3000); // 외부 시스템 지연 고려

        String orderId = payload.get("orderId") != null ? payload.get("orderId").toString() : null;
        List<Map<String, Object>> itemList = (List<Map<String, Object>>) payload.get("itemList");

        if (itemList == null || itemList.isEmpty() || orderId == null) {
            log.warn("[CALLBACK 2-5] itemList가 비어 있습니다. orderId={}", orderId);
            return;
        }

        log.info("orderId={}", orderId);

        // 1. Outbox 조회
        OrderOutbox orderOutbox = outBoxService.findByOrderId(orderId);
        int retryCount = 0;
        while (orderOutbox == null && retryCount < 5) {
            Thread.sleep(1000);
            retryCount++;
            orderOutbox = outBoxService.findByOrderId(orderId);
        }

        if (orderOutbox == null) {
            log.error("[OUTBOX] 5회 재시도 후에도 데이터 조회 실패 → orderId={}", orderId);
            return;
        }

        // 중복 콜백 방지
        if (SendStatus.SUCCESS.equals(orderOutbox.getIsCallBack2_5Success())) {
            log.info("[CALLBACK] 이미 처리된 콜백입니다 → orderId={}", orderId);
            return;
        }

        // 2. eSIM 상세 저장
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
                        (String) map.get("apnExplain"),
                        null,
                        null
                );
                orderService.registerEsimDetail(command);
            } catch (Exception e) {
                log.error("[CALLBACK] eSIM 상세 저장 중 오류 발생 (orderId={}): {}", orderId, e.getMessage(), e);
            }
        }

        // 3. Outbox 상태 갱신
        outBoxService.updateOrderOutBox(
                OutboxCommand.Update.of
                        (orderId,null,null,null,null,null,null,
                                null,null,null,SendStatus.SUCCESS,true)
        );

        // 4. 이메일 본문 생성
        String ordererName = Optional.ofNullable(orderOutbox.getOrdererName()).orElse("고객님");

        StringBuilder body = new StringBuilder();
        body.append("안녕하세요 ").append(ordererName).append("님, 유심스타입니다.\n\n")
                .append("구매하신 해외 eSIM QR코드 보내드립니다. 사용 전 아래 링크의 eSIM Guide의 등록 방법 및 주의사항 안내를 참고해서 사용해주시기 바랍니다.\n\n")
                .append("[주문번호] : ").append(orderId).append("\n\n");

        int index = 1;
        for (Map<String, Object> map : itemList) {
            body.append("=== eSIM 정보 ").append(index++).append(" ===\n")
                    .append("[상품명] : ").append(map.getOrDefault("productName", "-")).append("\n")
                    .append("[eSIM 정보] : ").append(map.getOrDefault("qrcode", "-")).append("\n")
                    .append("[ICCID] : ").append(map.getOrDefault("iccid", "-")).append("\n")
                    .append("[APN] : ").append(map.getOrDefault("apnExplain", "-")).append("\n")
                    .append("[SM-DP 주소] : qrcode.esimcase.com\n")
                    .append("[활성화 코드] : ").append(map.getOrDefault("rcode", "-")).append("\n")
                    .append("[확인코드] : ").append(map.getOrDefault("cfCode", "-")).append("\n\n");
        }

        body.append("[eSIM Guide]\nhttps://bit.ly/3y4UOVS\n\n")
                .append("[eSIM QR코드 스캔이 불가한 경우]\n")
                .append("QR코드를 스캔이 어려우신 경우 eSIM QR코드 등록방법의 \"세부사항 직접 입력 방법\"을 참고해서 직접 eSIM을 추가하실 수 있습니다.\n\n")
                .append("모쪼록 즐거운 여행되시기 바라며, 추가 문의 사항이 있으신 경우 네이버 톡톡이나 카톡에서 '유심스타'를 친추하시어 연락주시면 빠르고 정확하게 답변 드리도록 노력하겠습니다.");

        // 5. 알림 발행 (이메일 + SMS)
        Notification notification = notificationService.get().getNotification();

        eventPublisher.publishEvent(
                EmailEventCommand.SendEmail.of(
                        orderId,
                        orderOutbox.getEmail(),
                        notification.getSubject(),
                        body.toString()
                )
        );

        eventPublisher.publishEvent(
                SmsEventCommand.Issue.of(
                        orderOutbox.getPhoneNumber(),
                        body.toString(),
                        orderId
                )
        );

        log.info("[CALLBACK] 처리 완료 → orderId={}", orderId);
    }

    @Transactional
    public void processCallBack2_2(Map<String, Object> payload) throws InterruptedException {
        Thread.sleep(1000); // 외부 시스템 지연 고려

        String orderId = payload.get("orderId") != null ? payload.get("orderId").toString() : null;
        List<Map<String, Object>> itemList = (List<Map<String, Object>>) payload.get("itemList");

        if (itemList == null || itemList.isEmpty() || orderId == null) {
            log.warn("[CALLBACK] itemList가 비어 있습니다. orderId={}", orderId);
            return;
        }

        log.info("orderId={}", orderId);

        // 1. Outbox 조회
        OrderOutbox orderOutbox = outBoxService.findByOrderId(orderId);
        int retryCount = 0;
        while (orderOutbox == null && retryCount < 5) {
            Thread.sleep(1000);
            retryCount++;
            orderOutbox = outBoxService.findByOrderId(orderId);
        }

        if (orderOutbox == null) {
            log.error("[OUTBOX] 5회 재시도 후에도 데이터 조회 실패 → orderId={}", orderId);
            return;
        }

        // 중복 콜백 방지
        if (SendStatus.SUCCESS.equals(orderOutbox.getIsCallBack2_5Success())) {
            log.info("[CALLBACK 2-2] 이미 처리된 콜백입니다 → orderId={}", orderId);
            return;
        }
        else if(SendStatus.FAIL.equals(orderOutbox.getIsCallBack2_5Success())){
            log.info("[CALLBACK 2-2] 실패 처리된 API 입니다.");
        }

        outBoxService.updateOrderOutBox(
                OutboxCommand.Update.of
                        (orderId,null,null,null,null,null,null,
                                null,null,null,SendStatus.SUCCESS,null
                        )
        );

        // 2. eSIM 상세 저장
        for (Map<String, Object> map : itemList) {
            try {
                boolean isSuccess3_1 = true;
                boolean isSuccess = orderApiCall.callApiOrder3_1(map.get("redemptionCode").toString(),"2");

                if(!isSuccess){
                    isSuccess3_1 = false;
                }

                OrderCommand.RegisterEsimDetail command = OrderCommand.RegisterEsimDetail.of(
                        orderId,
                        (String) map.get("iccid"),
                        (String) map.get("productName"),
                        null,
                        (String) map.get("redemptionCode"),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        isSuccess3_1,
                        null
                );


                orderService.registerEsimDetail(command);
            } catch (Exception e) {
                log.error("[CALLBACK] eSIM 상세 저장 중 오류 발생 (orderId={}): {}", orderId, e.getMessage(), e);
            }
        }
    }

    @Transactional
    public void processCallBack3_2(Map<String, Object> payload) throws InterruptedException {
        String rcode = null;
        try {
            Thread.sleep(2000); // 외부 시스템 지연 고려

            rcode = payload.get("rcode") != null ? payload.get("rcode").toString() : null;

            if(payload.isEmpty()){
                log.warn("[CALLBACK] payload가 비어 있습니다. rcode ={}", rcode);
                return;
            }
            log.info("rcode={}", rcode);

            // 1. Outbox 조회
            EsimDetail esimDetail = orderService.getOrderDetailByRcode(rcode);
            int retryCount = 0;
            while (esimDetail == null && retryCount < 5) {
                Thread.sleep(1000);
                retryCount++;
                esimDetail = orderService.getOrderDetailByRcode(rcode);
            }

            if (esimDetail == null) {
                log.error("[EsimDetail 3-2] 5회 재시도 후에도 데이터 조회 실패 → rcode={}", rcode);
                return;
            }

            // 중복 콜백 방지
            if (Boolean.TRUE.equals(esimDetail.getIsSuccessCallBack3_2())) {
                log.info("[CALLBACK 3-2] 이미 처리된 콜백입니다 → rcode={}", rcode);
                return;
            }
            else if (Boolean.FALSE.equals(esimDetail.getIsSuccessCallBack3_2())) {
                log.info("[CALLBACK 3-2] 실패 처리된 API 입니다.");
            }

            OrderCommand.UpdateEsimDetail command = OrderCommand.UpdateEsimDetail.of(
                    rcode,
                    (String)payload.get("qrcode"),
                    (String)payload.get("qrcodeContent"),
                    (Integer)payload.get("salePlanDays"),
                    (String)payload.get("pin1"),
                    (String)payload.get("pin2"),
                    (String)payload.get("puk1"),
                    (String)payload.get("puk2"),
                    (String)payload.get("cfCode"),
                    (String)payload.get("apnExplain"),
                    null,
                    true
            );

            orderService.updateEsimDetail(command);

            // 5. 알림 발행 (이메일 + SMS)
            Notification notification = notificationService.get().getNotification();
            OrderOutbox orderOutbox = outBoxService.findByOrderId(esimDetail.getOrderId());

            eventPublisher.publishEvent(
                    EmailEventCommand.SendEmail.of(
                            esimDetail.getOrderId(),
                            orderOutbox.getEmail(),
                            notification.getSubject(),
                            "test"
                    )
            );

/*            eventPublisher.publishEvent(
                    SmsEventCommand.Issue.of(
                            orderOutbox.getPhoneNumber(),
                            "test",
                            esimDetail.getOrderId()
                    )
            );*/

        }catch (Exception e){
            OrderCommand.UpdateEsimDetail command = OrderCommand.UpdateEsimDetail.of(
                    rcode,null,null,null,
                    null,null
                    ,null,null,null,
                    null,null,false
            );

            orderService.updateEsimDetail(command);
        }
    }
    @Transactional(readOnly = true)
    public void resendEmail(String orderId) {
        // todo 템플릿 양식 가져오기
        Notification notification = notificationService.get().getNotification();

        // todo outbox 주문 가져오기
        OrderOutbox orderOutbox = outBoxService.findByOrderId(orderId);

        // todo outbox valid
        OrderOutBoxFacadeValid.resendEmailValid(orderOutbox);

        // todo 이메일 이벤트 발행
        eventPublisher.publishEvent(
                EmailEventCommand.SendEmail.of(
                        orderId,orderOutbox.getEmail(),
                        notification.getSubject(),notification.getContent()
                )
        );

    }

    @Transactional(readOnly = true)
    public void resendSms(String orderId) {
        OrderOutbox orderOutbox = outBoxService.findByOrderId(orderId);

        OrderOutBoxFacadeValid.resendSmsValid(orderOutbox);

        Notification notification = notificationService.get().getNotification();

        eventPublisher.publishEvent(
                SmsEventCommand.Issue.of(
                        orderOutbox.getPhoneNumber(),
                        notification.getContent(),
                        orderId
                )
        );
    }

    public void resendOrderLocal(String orderId) throws InterruptedException {
        OrderMain orderMain = orderService.findOrderMainByOrderId(orderId);

        Map<String,Object> result = orderApiCall.callApiOrder2_1();
        String esimOrderId = (String) result.get("orderId");

        if(result.get("isSuccess").equals(false)){
            throw new RuntimeException("2-1. 주문에 실패했습니다.");
        }

       log.info("OrderData : {}", JsonUtils.toJson(result));

        orderService.updateOrderMain(
                OrderCommand.UpdateOrderMain.of(
                       orderId,esimOrderId
                )
        );

        outBoxService.registerOrderOutBox(OutboxCommand.RegisterOrderOutbox.of(
                esimOrderId,
                orderMain.getEmail(),
                orderMain.getPhone(),
                true,
                orderMain.getOrdererName(),
                false,
                true,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        ));


        outBoxService.updateOrderOutBox(OutboxCommand.UpdateOfFail.of(
                orderId,
                false
        ));

        Thread.sleep(1000);
    }


}
