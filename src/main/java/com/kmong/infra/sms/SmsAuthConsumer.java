package com.kmong.infra.sms;

import com.kmong.domain.outbox.OutBoxService;
import com.kmong.domain.outbox.OutboxCommand;
import com.kmong.domain.outbox.SendStatus;
import com.kmong.support.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
@Service
@Profile("consumer")
public class SmsAuthConsumer {

    private final DefaultMessageService messageService;
    private final OutBoxService outBoxService;

    @RabbitListener(queues = com.kmong.support.constants.RabbitmqConstants.QUEUE_SMS_COOL, concurrency = "1")
    public void sendVerificationCode(SmsConsumerCommand.Issue command,
                                     com.rabbitmq.client.Channel channel,
                                     org.springframework.amqp.core.Message message
    ) throws IOException {
        try {
            log.info("[SMS Consumer] 메시지 수신: {}", JsonUtils.toJson(command));

            Message smsMessage = new Message();
            smsMessage.setFrom(com.kmong.support.utils.SmsUtils.fromNumber);
            smsMessage.setTo(command.getPhoneNumber());
            smsMessage.setText(command.getBody());

            messageService.sendOne(new SingleMessageSendingRequest(smsMessage));

            log.info("[SMS 전송 성공]: {}", com.kmong.support.utils.JsonUtils.toJson(command));

            outBoxService.updateOrderOutBox(
                    OutboxCommand.Update.of
                            (command.getOrderId(),null,SendStatus.SUCCESS,null,null,null,null,
                                    null,null,null, null, null)
            );
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

        } catch (Exception e) {
            log.error("[SMS 전송 실패]: {}", e.getMessage());

            outBoxService.updateOrderOutBox(
                    OutboxCommand.Update.of
                            (command.getOrderId(),null,SendStatus.FAIL,null,null,null,null,
                                    null,null,null, null,null)
            );

            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }
    }
}
