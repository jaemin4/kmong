package com.kmong.domain.outbox;

import com.kmong.aop.log.AfterCommitLogger;
import com.kmong.aop.log.RequestFlowLogger;
import com.kmong.support.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutBoxService {

    private final OrderOutboxRepository orderOutboxRepository;

    @Transactional
    public void registerOrderOutBox(OutboxCommand.RegisterOrderOutbox command) {

        OrderOutbox orderOutbox = OrderOutbox.of(
                command.getOrderId(),
                command.getPartnerApiName(),
                command.getEmail(),
                command.getPhoneNumber(),
                command.isEnableEmail(),
                command.getPartnerApiStatus()
        );

        OrderOutbox saved = orderOutboxRepository.save(orderOutbox);

        AfterCommitLogger.logInfoAfterCommit(() ->
                String.format("[%s] saved orderOutbox: %s", RequestFlowLogger.getCurrentUUID(), JsonUtils.toJson(saved))
        );


    }

    @Transactional
    public void updateOrderOutBox(OutboxCommand.Update command){
        OrderOutbox orderOutbox = orderOutboxRepository.findByOrderId(command.getOrderId())
                .orElseThrow(() -> new RuntimeException("OrderOutbox not found"));

        orderOutbox.update(
                command.getKakaoStatus(),
                command.getEmailStatus(),
                command.getNaverOrderStatus(),
                command.getSmsStatus(),
                command.getCallBackSuccess()
        );
        OrderOutbox updated = orderOutboxRepository.save(orderOutbox);

        AfterCommitLogger.logInfoAfterCommit(() ->
                String.format("[%s] updated orderOutbox: %s", RequestFlowLogger.getCurrentUUID(), JsonUtils.toJson(updated))
        );


    }


    public OrderOutbox findByOrderId(String orderId) {
        return orderOutboxRepository.findByOrderId(orderId).orElse(null);
    }
}
