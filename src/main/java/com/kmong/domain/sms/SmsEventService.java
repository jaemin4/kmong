package com.kmong.domain.sms;

import com.kmong.infra.sms.SmsConsumerCommand;
import com.kmong.support.constants.RabbitmqConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsEventService {
    private final RabbitTemplate rabbitTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendEmailWithOutboxStart(SmsEventCommand.Issue command) {
        log.info("SMS 발행 : {}", command.getOrderId());

        rabbitTemplate.convertAndSend(
                RabbitmqConstants.EXCHANGE_SMS_COOL,
                RabbitmqConstants.ROUTING_SMS_SEND,
                SmsConsumerCommand.Issue.of(
                        command.getPhoneNumber(),
                        command.getSmsBody(),
                        command.getOrderId()
                )
        );

    }



}
