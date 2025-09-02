package com.kmong.domain.order;

import com.kmong.aop.log.AfterCommitLogger;
import com.kmong.aop.log.RequestFlowLogger;
import com.kmong.support.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderMainRepository orderMainRepository;
    private final OrderDetailRepository orderDetailRepository;

    @Transactional
    public void registerOrderMain(OrderCommand.RegisterOrderMain command) {
        OrderMain entity = OrderMain.of(
                command.getProductOrderId(),
                command.getOrderDate(),
                command.getOrderNumber(),
                command.getOrdererName(),
                command.getReceiverName(),
                command.getPurchaseChannel(),
                command.getProductOption(),
                command.getQuantity(),
                command.getOriginalPrice(),
                command.getCurrency(),
                command.getMessage(),
                command.getPaymentStatus(),
                command.getIssueStatus()
        );

        OrderMain saved = orderMainRepository.save(entity);

        AfterCommitLogger.logInfoAfterCommit(() ->
                String.format("[%s] saved OrderMain: %s", RequestFlowLogger.getCurrentUUID(), JsonUtils.toJson(saved))
        );
    }
    @Transactional
    public void registerOrderDetail(OrderCommand.RegisterOrderDetail command){
        OrderDetail entity = OrderDetail.of(
                command.getActivationDate(),
                command.getExpiryDate(),
                command.getIddicNumber(),
                command.getSmdpAddress(),
                command.getActivationCode(),
                command.getApn(),
                command.getDataUsage()
        );

        OrderDetail saved = orderDetailRepository.save(entity);

        AfterCommitLogger.logInfoAfterCommit(() ->
                String.format("[%s] saved OrderDetail: %s", RequestFlowLogger.getCurrentUUID(), JsonUtils.toJson(saved))
        );
    }

    public boolean existsMainByProductOrderId(String productOrderId) {
        return orderMainRepository.existsByProductOrderId(productOrderId);
    }



}
