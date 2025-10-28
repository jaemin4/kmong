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
                command.getEmail(),
                command.getPhoneNumber(),
                command.isEnableEmail(),
                command.getOrdererName(),
                command.getLesim(),
                command.getIsCall2_1Success(),
                command.getIsCallBack2_2Success(),
                command.getIsCall3_1Success(),
                command.getIsCallBack3_2Success(),
                command.getIsCall2_4Success(),
                command.getIsCallBack2_5Success()
        );

        OrderOutbox saved = orderOutboxRepository.save(orderOutbox);

        AfterCommitLogger.logInfoAfterCommit(() ->
                String.format("[%s] saved orderOutbox: %s",
                        RequestFlowLogger.getCurrentUUID(), JsonUtils.toJson(saved))
        );
    }

    /** 상태 업데이트 */
    @Transactional
    public void updateOrderOutBox(OutboxCommand.Update command) {
        OrderOutbox orderOutbox = orderOutboxRepository.findByOrderId(command.getOrderId())
                .orElseThrow(() -> new RuntimeException("OrderOutbox not found → orderId=" + command.getOrderId()));

        orderOutbox.update(
                command.getKakaoStatus(),
                command.getEmailStatus(),
                command.getNaverOrderStatus(),
                command.getSmsStatus(),
                command.getIsCall2_1Success(),
                command.getIsCallBack2_2Success(),
                command.getIsCall3_1Success(),
                command.getIsCallBack3_2Success(),
                command.getIsCall2_4Success(),
                command.getIsCallBack2_5Success(),
                null
        );

        OrderOutbox updated = orderOutboxRepository.save(orderOutbox);

        AfterCommitLogger.logInfoAfterCommit(() ->
                String.format("[%s] updated orderOutbox: %s",
                        RequestFlowLogger.getCurrentUUID(), JsonUtils.toJson(updated))
        );
    }


    public OrderOutbox findByOrderId(String orderId) {
        return orderOutboxRepository.findByOrderId(orderId).orElse(null);
    }

    @Transactional
    public void deleteByOrderId(String orderId) {
        orderOutboxRepository.deleteByOrderId(orderId);
    }

    @Transactional
    public void updateOrderOutBox(OutboxCommand.UpdateOfFail command) {
        OrderOutbox orderOutbox = orderOutboxRepository.findByOrderId(command.getOrderId())
                .orElseThrow(() -> new RuntimeException("OrderOutbox not found → orderId=" + command.getOrderId()));

        orderOutbox.update(
                null,null,null,
                null,null,
                null,null,
                null,null,
                null,command.getIsFailed()
        );

        OrderOutbox updated = orderOutboxRepository.save(orderOutbox);

        AfterCommitLogger.logInfoAfterCommit(() ->
                String.format("[%s] updated orderOutbox: %s",
                        RequestFlowLogger.getCurrentUUID(), JsonUtils.toJson(updated))
        );    }
}
