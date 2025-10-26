package com.kmong.infra.email;

import com.kmong.domain.outbox.OutBoxService;
import com.kmong.domain.outbox.OutboxCommand;
import com.kmong.domain.outbox.SendStatus;
import com.kmong.support.constants.RabbitmqConstants;
import com.kmong.support.utils.JsonUtils;
import com.kmong.support.utils.MailUtils;
import com.rabbitmq.client.AMQP;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
@Profile("consumer")
public class EmailConsumer {

    private final JavaMailSender mailSender;
    private final OutBoxService outBoxService;

    @RabbitListener(queues = RabbitmqConstants.QUEUE_MAIL_SEND, concurrency = "1")
    public void sendMail(
            EmailConsumerCommand.Issue command,
            org.springframework.amqp.core.Message message,
            com.rabbitmq.client.Channel channel
    ) throws IOException {
        try{
            log.info("이메일 발송 consumer 진입");
            SimpleMailMessage emailMessage = new SimpleMailMessage();
            emailMessage.setTo(command.getEmail());
            emailMessage.setSubject(command.getSubject());
            emailMessage.setText(command.getBody());
            emailMessage.setFrom(MailUtils.setFrom);
            mailSender.send(emailMessage);

            log.info("Email sent to : {}", JsonUtils.toJson(emailMessage));

            outBoxService.updateOrderOutBox(
                    OutboxCommand.Update.of
                            (command.getOrderId(),null,null,SendStatus.SUCCESS,null,null,null,
                                    null,null,null, null)
            );
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

        }catch (Exception e) {
            log.error("ERROR Send Mail : {}",e.getMessage());
            outBoxService.updateOrderOutBox(
                    OutboxCommand.Update.of
                            (command.getOrderId(),null,null,SendStatus.FAIL,null,null,null,
                                    null,null,null, null)
            );

            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

        }
    }
}
