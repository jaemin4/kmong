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
            String apnExplain
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
                .build();
    }
}
