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

    /** 주문 기본 정보 */
    @Column(nullable = false)
    private String orderDate;         // 주문일

    @Column(nullable = false, unique = true, length = 50)
    private String orderNumber;       // 주문번호

    @Column(nullable = false, length = 50)
    private String ordererName;       // 주문자명

    @Column(nullable = false, length = 50)
    private String receiverName;      // 수령자명

    @Column(nullable = false, length = 30)
    private String purchaseChannel;   // 구매채널

    @Column(nullable = false, length = 255)
    private String productOption;     // 상품옵션

    @Column(nullable = false)
    private Integer quantity;         // 수량

    @Column(nullable = false)
    private Double originalPrice;     // 원가

    @Column(nullable = false, length = 10)
    private String currency;          // 통화 (예: NTD)

    @Column(length = 500)
    private String message;           // 기타메세지

    @Column(nullable = false, length = 20)
    private String paymentStatus;     // 결제상태

    @Column(nullable = false, length = 20)
    private String issueStatus;       // 발급상태

    @Lob
    private String detailContent;     // 상세내용

    /** eSIM 개통 관련 정보 */
    @Column
    private String activationDate;    // 개통일

    @Column
    private String expiryDate;        // 종료일

    @Column(length = 100)
    private String iddicNumber;       // IDDIC 넘버

    @Column(length = 255)
    private String smdpAddress;       // SM-DP 주소

    @Column(length = 255)
    private String activationCode;    // 활성화 코드

    @Column(length = 100)
    private String apn;               // APN 값

    @Column(length = 100)
    private String dataUsage;         // 데이터 사용량 조회

    @Version
    private Long version;

    /** 정적 팩토리 메서드 */
    public static OrderDetail registerOf(
            String orderDate, String orderNumber, String ordererName, String receiverName,
            String purchaseChannel, String productOption, Integer quantity, Double originalPrice, String currency,
            String message, String paymentStatus, String issueStatus, String detailContent,
            String activationDate, String expiryDate, String iddicNumber, String smdpAddress,
            String activationCode, String apn, String dataUsage
    ) {
        return OrderDetail.builder()
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
                .detailContent(detailContent)
                .activationDate(activationDate)
                .expiryDate(expiryDate)
                .iddicNumber(iddicNumber)
                .smdpAddress(smdpAddress)
                .activationCode(activationCode)
                .apn(apn)
                .dataUsage(dataUsage)
                .build();
    }

    /** 업데이트 메서드 */
    public void update(
            String paymentStatus, String issueStatus,
            String activationDate, String expiryDate,
            String activationCode, String dataUsage
    ) {
        this.paymentStatus = paymentStatus != null ? paymentStatus : this.paymentStatus;
        this.issueStatus = issueStatus != null ? issueStatus : this.issueStatus;
        this.activationDate = activationDate != null ? activationDate : this.activationDate;
        this.expiryDate = expiryDate != null ? expiryDate : this.expiryDate;
        this.activationCode = activationCode != null ? activationCode : this.activationCode;
        this.dataUsage = dataUsage != null ? dataUsage : this.dataUsage;
    }
}
