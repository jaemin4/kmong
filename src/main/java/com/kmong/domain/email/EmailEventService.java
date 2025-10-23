package com.kmong.domain.email;

import com.kmong.infra.email.EmailConsumerCommand;
import com.kmong.support.constants.RabbitmqConstants;
import com.kmong.support.utils.MailUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;


@Service
@RequiredArgsConstructor
@Slf4j
public class EmailEventService {

    private final RabbitTemplate rabbitTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendEmailWithOutboxStart(EmailEventCommand.SendEmail command) {
        log.info("이메일 발행 : {}", command.getOrderId());

        rabbitTemplate.convertAndSend(
                RabbitmqConstants.EXCHANGE_MAIL,
                RabbitmqConstants.ROUTING_MAIL_SEND,
                EmailConsumerCommand.Issue.of(
                        command.getOrderId(),
                        command.getEmail(),
                        command.getMailSubject(),
                        command.getMailBody(),
                        MailUtils.setFrom
                )
        );

    }



}
