package com.kmong.application;

import com.kmong.domain.notification.Notification;
import com.kmong.domain.notification.NotificationService;
import com.kmong.domain.order.OrderService;
import com.kmong.domain.outbox.OutBoxService;
import com.kmong.domain.outbox.OutboxCommand;
import com.kmong.domain.outbox.SendStatus;
import com.kmong.infra.email.EmailConsumerCommand;
import com.kmong.support.constants.RabbitmqConstants;
import com.kmong.support.utils.MailUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import static com.kmong.support.utils.CommUtils.renderByKeys;
import static com.kmong.support.utils.JsonPathUtils.getS;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageFacade {

    private final OrderService orderService;
    private final OutBoxService outBoxService;
    private final NotificationService notificationService;
    private final RabbitTemplate rabbitTemplate;

    public void processEsinInfra(MessageFacadeCriteria.ProcessEsinInfra criteria){
        String phoneNumber = getS(criteria.getRow(), "content.order.ordererTel");
        String email = "eheh25877@gmail.com";
        boolean enabledEmail = !email.equals("disableEmail");

        Notification notification = notificationService.get().getNotification();
        String messageBody = renderByKeys(
                notification.getContent(),
                notification.getKeyString(),
                criteria.getRow()
        );

        // todo 2.  OutBox Register






    }



}
