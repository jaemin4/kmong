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

    /** 상세내용 컬럼 */
    @Column(length = 30)
    private String activationDate;   // 개통일

    @Column(length = 30)
    private String expiryDate;       // 종료일

    @Column(length = 100)
    private String iddicNumber;      // IDDIC 넘버

    @Column(length = 255)
    private String smdpAddress;      // SM-DP 주소

    @Column(length = 255)
    private String activationCode;   // 활성화 코드

    @Column(length = 100)
    private String apn;              // APN 값

    @Column(length = 100)
    private String dataUsage;        // 데이터 사용량 조회

    public static OrderDetail of(
            String activationDate,
            String expiryDate,
            String iddicNumber,
            String smdpAddress,
            String activationCode,
            String apn,
            String dataUsage
    ) {
        return OrderDetail.builder()
                .activationDate(activationDate)
                .expiryDate(expiryDate)
                .iddicNumber(iddicNumber)
                .smdpAddress(smdpAddress)
                .activationCode(activationCode)
                .apn(apn)
                .dataUsage(dataUsage)
                .build();
    }
}
