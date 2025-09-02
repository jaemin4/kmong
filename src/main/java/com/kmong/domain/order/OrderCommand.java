package com.kmong.domain.order;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class OrderCommand {

    @Getter
    @AllArgsConstructor(staticName = "of")
    public static class RegisterOrderMain{
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
    }

    @Getter
    @AllArgsConstructor(staticName = "of")
    public static class RegisterOrderDetail{

        private final String activationDate;  // 개통일
        private final String expiryDate;      // 종료일
        private final String iddicNumber;     // IDDIC 넘버
        private final String smdpAddress;     // SM-DP 주소
        private final String activationCode;  // 활성화 코드
        private final String apn;             // APN 값
        private final String dataUsage;       // 데이터 사용량 조회
    }


}
