package com.kmong.domain.order;

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

        orderMainRepository.save(entity);
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

        orderDetailRepository.save(entity);
    }

}
