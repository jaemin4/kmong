package com.kmong.domain.outbox;

import com.kmong.support.utils.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "esim_order_outbox")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE, toBuilder = true)
public class OrderOutbox extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 시퀀스 PK

    @Column(nullable = false, length = 50, unique = true)
    private String productOrderId; // 네이버 상품 주문번호

    @Column(length = 100)
    private String partnerApiName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SendStatus partnerApiStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SendStatus kakaoStatus;

    @Column(nullable = false, length = 60)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SendStatus smsStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SendStatus emailStatus;

    @Column(length = 100)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SendStatus naverOrderStatus;

    /** Factory Method */
    public static OrderOutbox disableMailOf(
            String productOrderId,
            String partnerApiName,
            SendStatus partnerApiStatus,
            String phoneNumber
    ) {
        return OrderOutbox.builder()
                .productOrderId(productOrderId)
                .partnerApiName(partnerApiName)
                .partnerApiStatus(partnerApiStatus)
                .kakaoStatus(SendStatus.PENDING)
                .emailStatus(SendStatus.SKIP)
                .naverOrderStatus(SendStatus.PENDING)
                .smsStatus(SendStatus.PENDING)
                .phoneNumber(phoneNumber)
                .build();
    }

    public static OrderOutbox enableMailOf(
            String productOrderId,
            String partnerApiName,
            SendStatus partnerApiStatus,
            String phoneNumber,
            String email
    ) {
        return OrderOutbox.builder()
                .productOrderId(productOrderId)
                .partnerApiName(partnerApiName)
                .partnerApiStatus(partnerApiStatus)
                .kakaoStatus(SendStatus.PENDING)
                .emailStatus(SendStatus.PENDING)
                .naverOrderStatus(SendStatus.PENDING)
                .smsStatus(SendStatus.SKIP)
                .phoneNumber(phoneNumber)
                .email(email)
                .build();
    }

    public void update(
            SendStatus kakaoStatus,
            SendStatus emailStatus,
            SendStatus naverOrderStatus,
            SendStatus smsStatus
    ) {
        if (kakaoStatus != null)     {
            this.kakaoStatus = kakaoStatus;
        }
        if (emailStatus != null)      {
            this.emailStatus = emailStatus;
        }
        if (naverOrderStatus != null) {
            this.naverOrderStatus = naverOrderStatus;
        }
        if (smsStatus != null)        {
            this.smsStatus = smsStatus;
        }
    }


}
