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

    // === API 상태 관리 ===
    private Boolean isCall2_1Success;
    @Enumerated(EnumType.STRING)
    private SendStatus isCallBack2_2Success;

    private Boolean isCall3_1Success;

    @Enumerated(EnumType.STRING)
    private SendStatus isCallBack3_2Success;

    private Boolean isCall2_4Success;

    @Enumerated(EnumType.STRING)
    private SendStatus isCallBack2_5Success;

    private Boolean lesim; // true: 2.4→2.5 flow, false: 2.1→3.2 flow

    private String ordererName;

    public static OrderOutbox of(
            String orderId,
            String email,
            String phoneNumber,
            boolean isEnableEmail,
            String ordererName,
            Boolean lesim,
            Boolean isCall2_1Success,
            SendStatus isCallBack2_2Success,
            Boolean isCall3_1Success,
            SendStatus isCallBack3_2Success,
            Boolean isCall2_4Success,
            SendStatus isCallBack2_5Success
    ) {
        SendStatus emailStatus = SendStatus.PENDING;
        SendStatus smsStatus = SendStatus.SKIP;

        if (!isEnableEmail) {
            emailStatus = SendStatus.SKIP;
            smsStatus = SendStatus.PENDING;
        }

        return OrderOutbox.builder()
                .orderId(orderId)
                .kakaoStatus(SendStatus.PENDING)
                .smsStatus(smsStatus)
                .emailStatus(emailStatus)
                .naverOrderStatus(null)
                .phoneNumber(phoneNumber)
                .email(email)
                .isCall2_1Success(isCall2_1Success)
                .isCallBack2_2Success(isCallBack2_2Success)
                .isCall3_1Success(isCall3_1Success)
                .isCallBack3_2Success(isCallBack3_2Success)
                .isCall2_4Success(isCall2_4Success)
                .isCallBack2_5Success(isCallBack2_5Success)
                .lesim(lesim)
                .ordererName(ordererName)
                .build();
    }

    // ====== 업데이트 메서드 ======
    public void update(
            SendStatus kakaoStatus,
            SendStatus emailStatus,
            SendStatus naverOrderStatus,
            SendStatus smsStatus,
            Boolean isCall2_1Success,
            SendStatus isCallBack2_2Success,
            Boolean isCall3_1Success,
            SendStatus isCallBack3_2Success,
            Boolean isCall2_4Success,
            SendStatus isCallBack2_5Success
    ) {
        if (kakaoStatus != null) this.kakaoStatus = kakaoStatus;
        if (emailStatus != null) this.emailStatus = emailStatus;
        if (naverOrderStatus != null) this.naverOrderStatus = naverOrderStatus;
        if (smsStatus != null) this.smsStatus = smsStatus;
        if (isCall2_1Success != null) this.isCall2_1Success = isCall2_1Success;
        if (isCallBack2_2Success != null) this.isCallBack2_2Success = isCallBack2_2Success;
        if (isCall3_1Success != null) this.isCall3_1Success = isCall3_1Success;
        if (isCallBack3_2Success != null) this.isCallBack3_2Success = isCallBack3_2Success;
        if (isCall2_4Success != null) this.isCall2_4Success = isCall2_4Success;
        if (isCallBack2_5Success != null) this.isCallBack2_5Success = isCallBack2_5Success;
    }
}
