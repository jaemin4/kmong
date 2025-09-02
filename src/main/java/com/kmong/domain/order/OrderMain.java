package com.kmong.domain.order;

import com.kmong.support.utils.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "esim_order_main")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE, toBuilder = true)
public class OrderMain extends BaseTimeEntity {

    @Id
    @Column(length = 60)
    private String productOrderId;

    /** 메인 UI 컬럼 */
    @Column(nullable = false, length = 30)
    private String orderDate;        // 주문일

    @Column(nullable = false, length = 50)
    private String orderNumber;      // 주문번호

    @Column(nullable = false, length = 50)
    private String ordererName;      // 주문자명

    @Column(nullable = false, length = 50)
    private String receiverName;     // 수령자명

    @Column(nullable = false, length = 100)
    private String purchaseChannel;  // 구매채널

    @Column(nullable = false, length = 500)
    private String productOption;    // 상품옵션

    @Column(nullable = false)
    private Integer quantity;        // 수량

    @Column(nullable = false)
    private Double originalPrice;    // 원가

    @Column(nullable = false, length = 10)
    private String currency;         // 통화(NTD 등)

    @Column(length = 1000)
    private String message;          // 기타메세지

    @Column(nullable = false, length = 30)
    private String paymentStatus;    // 결제상태

    @Column(nullable = false, length = 30)
    private String issueStatus;      // 발급상태

    /** 정적 팩토리 */
    public static OrderMain of(
            String productOrderId,
            String orderDate,
            String orderNumber,
            String ordererName,
            String receiverName,
            String purchaseChannel,
            String productOption,
            Integer quantity,
            Double originalPrice,
            String currency,
            String message,
            String paymentStatus,
            String issueStatus
    ) {
        return OrderMain.builder()
                .productOrderId(productOrderId)
                .orderDate(orderDate)
                .orderNumber(orderNumber)
                .ordererName(ordererName)
                .receiverName(receiverName)
                .purchaseChannel(purchaseChannel)
                .productOption(productOption)
                .quantity(quantity)
                .originalPrice(originalPrice)
                .currency(currency)
                .message(message)
                .paymentStatus(paymentStatus)
                .issueStatus(issueStatus)
                .build();
    }
}
