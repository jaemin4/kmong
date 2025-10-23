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
    private String orderId;

    @Column(nullable = false, length = 100)
    private String partnerApiName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SendStatus partnerApiStatus;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private SendStatus kakaoStatus;

    @Column(length = 60)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private SendStatus smsStatus;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private SendStatus emailStatus;

    @Column(length = 100)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private SendStatus naverOrderStatus;

    public static OrderOutbox of(String orderId,String partnerApiName,String email,String phoneNumber, boolean isEnableEmail){
        SendStatus emailStatus = SendStatus.PENDING;
        SendStatus smsStatus = SendStatus.SKIP;

        if(!isEnableEmail){
            emailStatus = SendStatus.SKIP;
            smsStatus = SendStatus.PENDING;

        }
        return OrderOutbox.builder()
                .orderId(orderId)
                .partnerApiName(partnerApiName)
                .partnerApiStatus(SendStatus.PENDING)
                .kakaoStatus(SendStatus.PENDING)
                .emailStatus(emailStatus)
                .naverOrderStatus(null)
                .smsStatus(smsStatus)
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
