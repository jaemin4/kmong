package com.kmong.infra.sms;

import com.kmong.support.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
@Profile("consumer")
public class SmsAuthConsumer {

    private final DefaultMessageService messageService;

    @RabbitListener(queues = com.kmong.support.constants.RabbitmqConstants.QUEUE_SMS_COOL, concurrency = "1")
    public void sendVerificationCode(SmsConsumerCommand.Issue command) {
        try {
            log.info("[SMS Consumer] 메시지 수신: {}", JsonUtils.toJson(command));

            Message message = new Message();
            message.setFrom(com.kmong.support.utils.SmsUtils.fromNumber);
            message.setTo(command.getPhoneNumber());
            message.setText(command.getBody());

            messageService.sendOne(new SingleMessageSendingRequest(message));

            log.info("[SMS 전송 성공]: {}", com.kmong.support.utils.JsonUtils.toJson(command));

        } catch (Exception e) {
            log.error("[SMS 전송 실패]: {}", e.getMessage());
        }
    }
}
