package com.kmong.domain.order;

import com.kmong.support.utils.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * eSIM 상세 정보 엔티티
 */
@Entity
@Table(name = "esim_info_detail")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE, toBuilder = true)
public class EsimDetail extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 시퀀스 PK

    @Column
    private String orderId;         // 주문 ID

    @Column
    private String iccid;           // eSIM ICCID 번호

    @Column
    private String productName;     // 상품명 (예: Japan, 1 Day, 1GB)

    @Column
    private String qrcode;          // QR 코드 이미지 URL

    @Column
    private String rcode;           // 리딤 코드

    @Column
    private String qrcodeContent;   // LPA 주소 + 활성화 코드 포함

    @Column
    private Integer salePlanDays;   // 요금제 일수 (nullable 허용)

    @Column
    private String pin1;            // PIN1

    @Column
    private String pin2;            // PIN2

    @Column
    private String puk1;            // PUK1

    @Column
    private String puk2;            // PUK2

    @Column
    private String cfCode;          // 인증 코드

    @Column
    private String apnExplain;      // APN 설명 (예: rsp.demo.com)

    @Column
    private Boolean isSuccess3_1;

    @Column
    private Boolean isSuccessCallBack3_2;

    public static EsimDetail of(
            String orderId,
            String iccid,
            String productName,
            String qrcode,
            String rcode,
            String qrcodeContent,
            Integer salePlanDays,
            String pin1,
            String pin2,
            String puk1,
            String puk2,
            String cfCode,
            String apnExplain,
            Boolean isSuccess3_1,
            Boolean isSuccessCallBack3_2
    ) {
        return EsimDetail.builder()
                .orderId(orderId)
                .iccid(iccid)
                .productName(productName)
                .qrcode(qrcode)
                .rcode(rcode)
                .qrcodeContent(qrcodeContent)
                .salePlanDays(salePlanDays)
                .pin1(pin1)
                .pin2(pin2)
                .puk1(puk1)
                .puk2(puk2)
                .cfCode(cfCode)
                .apnExplain(apnExplain)
                .isSuccess3_1(isSuccess3_1)
                .isSuccessCallBack3_2(isSuccessCallBack3_2)
                .build();
    }

    public void update(OrderCommand.UpdateEsimDetail command) {
        if (command.getQrcode() != null) this.qrcode = command.getQrcode();
        if (command.getQrcodeContent() != null) this.qrcodeContent = command.getQrcodeContent();
        if (command.getSalePlanDays() != null) this.salePlanDays = command.getSalePlanDays();
        if (command.getPin1() != null) this.pin1 = command.getPin1();
        if (command.getPin2() != null) this.pin2 = command.getPin2();
        if (command.getPuk1() != null) this.puk1 = command.getPuk1();
        if (command.getPuk2() != null) this.puk2 = command.getPuk2();
        if (command.getCfCode() != null) this.cfCode = command.getCfCode();
        if (command.getApnExplain() != null) this.apnExplain = command.getApnExplain();
        if (command.getIsSuccess3_1() != null) this.isSuccess3_1 = command.getIsSuccess3_1();
        if (command.getIsSuccessCallBack3_2() != null) this.isSuccessCallBack3_2 = command.getIsSuccessCallBack3_2();
    }

}
