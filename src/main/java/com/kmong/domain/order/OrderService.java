package com.kmong.domain.order;

import com.github.pagehelper.PageInfo;
import com.kmong.aop.log.AfterCommitLogger;
import com.kmong.aop.log.RequestFlowLogger;
import com.kmong.support.utils.JsonUtils;
import com.kmong.support.utils.PagingUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

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

    public OrderResult.GetOrderMainPaging getOrderMainPaging(String keyword, Pageable pageable, String startDate,String endDate){

        PagingUtil.getPagingCall(pageable);
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : null;
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : null;

        List<OrderMain> data = orderMainRepository.findAllByKeyword(keyword,start,end);
        PageInfo<OrderMain> pageInfo = new PageInfo<>(data);

        return OrderResult.GetOrderMainPaging.of(data,PagingUtil.ofPagingCommResult(pageInfo));

    }


    public boolean existsMainByOrderId(String orderId) {
        return orderMainRepository.existsMainByOrderId(orderId);
    }
}
