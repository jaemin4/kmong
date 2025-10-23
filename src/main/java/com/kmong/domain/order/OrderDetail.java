package com.kmong.domain.order;

import com.kmong.support.utils.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "esim_order_detail")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE, toBuilder = true)
public class OrderDetail extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String productOrderId;
    private String orderId;

    public static OrderDetail of(
            String orderId
    ) {
        return OrderDetail.builder()
                .productOrderId(productOrderId)
                .orderId(orderId)
                .build();
    }
}
