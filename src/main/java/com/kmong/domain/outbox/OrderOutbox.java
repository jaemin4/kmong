package com.kmong.domain.outbox;

import com.kmong.support.utils.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "esim_order_outbox", uniqueConstraints = {
        @UniqueConstraint(columnNames = "orderProductId")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE, toBuilder = true)
public class OrderOutbox extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 시퀀스 PK

    @Column(nullable = false, length = 50, unique = true)
    private String orderProductId; // 네이버 상품 주문번호

    @Column(length = 100)
    private String partnerApiName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SendStatus partnerApiStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SendStatus kakaoStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SendStatus emailStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SendStatus naverOrderStatus;

    /** Factory Method */
    public static OrderOutbox of(String orderProductId, String partnerApiName, SendStatus partnerApiStatus) {
        return OrderOutbox.builder()
                .orderProductId(orderProductId)
                .partnerApiName(partnerApiName)
                .partnerApiStatus(partnerApiStatus)
                .kakaoStatus(SendStatus.PENDING)
                .emailStatus(SendStatus.PENDING)
                .naverOrderStatus(SendStatus.PENDING)
                .build();
    }

    /** 상태 업데이트 */
    public void markKakaoStatus(SendStatus status) {
        this.kakaoStatus = status;
    }

    public void markEmailStatus(SendStatus status) {
        this.emailStatus = status;
    }

    public void markNaverOrderStatus(SendStatus status) {
        this.naverOrderStatus = status;
    }

}
