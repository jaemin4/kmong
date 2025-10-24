package com.kmong.domain.order;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.scheduling.support.SimpleTriggerContext;

public class OrderCommand {

    @Getter
    @AllArgsConstructor(staticName = "of")
    public static class RegisterOrderMain{
        private final String productOrderId;
        private final String orderDate;       // 주문일
        private final String orderNumber;     // 주문번호
        private final String ordererName;     // 주문자명
        private final String receiverName;    // 수령자명
        private final String purchaseChannel; // 구매채널
        private final String productOption;   // 상품옵션
        private final Integer quantity;       // 수량
        private final Double originalPrice;   // 원가
        private final String currency;        // 통화(NTD 등)
        private final String message;         // 기타메세지
        private final String paymentStatus;   // 결제상태
        private final String issueStatus;     // 발급상태
        private final String orderId;
    }

    @Getter
    @AllArgsConstructor(staticName = "of")
    public static class RegisterOrderDetail{
        private final String orderId;
    }


    @Getter
    @AllArgsConstructor(staticName = "of")
    public static class RegisterEsimDetail {
        private String orderId;         // 주문 ID
        private String iccid;           // eSIM ICCID 번호
        private String productName;     // 상품명 (예: Japan, 1 Day, 1GB)
        private String qrcode;          // QR 코드 이미지 URL
        private String rcode;           // 리딤 코드
        private String qrcodeContent;   // LPA 주소 + 활성화 코드 포함
        private Integer salePlanDays;       // 요금제 일수
        private String pin1;            // PIN1
        private String pin2;            // PIN2
        private String puk1;            // PUK1
        private String puk2;            // PUK2
        private String cfCode;          // 인증 코드
        private String apnExplain;      // APN 설명 (예: rsp.demo.com)
    }
}
