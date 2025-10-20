package com.kmong.domain.outbox;

import com.kmong.aop.log.AfterCommitLogger;
import com.kmong.aop.log.RequestFlowLogger;
import com.kmong.domain.notification.Notification;
import com.kmong.domain.notification.NotificationService;
import com.kmong.infra.email.EmailConsumerCommand;
import com.kmong.support.constants.RabbitmqConstants;
import com.kmong.support.utils.JsonUtils;
import com.kmong.support.utils.MailUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import static com.kmong.support.utils.CommUtils.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutBoxService {

    private final OrderOutboxRepository orderOutboxRepository;
    private final RabbitTemplate rabbitTemplate;
    private final NotificationService notificationService;


    @Transactional
    public void registerOrderOutBox(OutboxCommand.RegisterOrderOutbox command) {
        OrderOutbox orderOutbox;
        if(command.getEnableEmail()){
           orderOutbox = OrderOutbox.enableMailOf(
                    command.getProductOrderId(),
                    command.getPartnerApiName(),
                    command.getPartnerApiStatus(),
                    command.getPhoneNumber(),
                    command.getEmail()
            );
        } else{
            orderOutbox = OrderOutbox.disableMailOf(
                    command.getProductOrderId(),
                    command.getPartnerApiName(),
                    command.getPartnerApiStatus(),
                    command.getPhoneNumber()
            );
        }

        OrderOutbox saved = orderOutboxRepository.save(orderOutbox);

        AfterCommitLogger.logInfoAfterCommit(() ->
                String.format("[%s] saved orderOutbox: %s", RequestFlowLogger.getCurrentUUID(), JsonUtils.toJson(saved))
        );

        if (command.getEnableEmail()) {
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                    @Override
                    public void afterCommit() {
                        try {
                            log.info("[AFTER COMMIT] Outbox 커밋 완료 → MQ 발행 시작");

                            Notification n = notificationService.get().getNotification();
                            String body = renderByKeys(n.getContent(), n.getKeyString(), command.getRow());

                            rabbitTemplate.convertAndSend(
                                    RabbitmqConstants.EXCHANGE_MAIL,
                                    RabbitmqConstants.ROUTING_MAIL_SEND,
                                    EmailConsumerCommand.Issue.of(
                                            command.getProductOrderId(),
                                            command.getEmail(),
                                            n.getSubject(),
                                            body,
                                            MailUtils.setFrom
                                    )
                            );

                            log.info("[AFTER COMMIT] MQ 발행 완료 → {}", command.getEmail());
                        } catch (Exception e) {
                            log.error("[AFTER COMMIT] MQ 발행 실패: {}", e.getMessage(), e);
                        }
                    }
                });
            } else {
                log.warn("[AFTER COMMIT] 활성 트랜잭션이 없어 MQ 발행이 등록되지 않음");
            }

        } else {
            log.info("메일 없음 또는 테스트 계정 아님 → 문자 발송 예정: {}", command.getPhoneNumber());
        }
    }

    @Transactional
    public void updateOrderOutBox(OutboxCommand.Update command){
        OrderOutbox orderOutbox = orderOutboxRepository.findByProductOrderId(command.getProductOrderId())
                .orElseThrow(() -> new RuntimeException("OrderOutbox not found"));

        orderOutbox.update(command.getKakaoStatus(),command.getEmailStatus(),command.getNaverOrderStatus(),command.getSmsStatus());
        OrderOutbox updated = orderOutboxRepository.save(orderOutbox);

        AfterCommitLogger.logInfoAfterCommit(() ->
                String.format("[%s] updated orderOutbox: %s", RequestFlowLogger.getCurrentUUID(), JsonUtils.toJson(updated))
        );


    }


}
