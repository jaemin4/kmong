package com.kmong.scheduler;

import com.kmong.domain.order.OrderCommand;
import com.kmong.support.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.kmong.support.utils.CommUtils.firstNonEmpty;
import static com.kmong.support.utils.JsonPathUtils.*;
import static com.kmong.support.utils.JsonPathUtils.getS;

@Slf4j
public class OrderUtils {
    /** 주문정보 매핑 */
    public static OrderCommand.RegisterOrderMain mapToRegisterOrderMain(Map<String, Object> row, String esimOrderId, String email, String phone) {
        Double price = getD(row, "content.productOrder.unitPrice");
        if (price == null) price = 0.0;

        return OrderCommand.RegisterOrderMain.of(
                getS(row, "content.productOrder.productOrderId"),
                getS(row, "content.order.orderDate"),
                getS(row, "content.order.orderId"),
                getS(row, "content.order.ordererName"),
                getS(row, "content.productOrder.shippingAddress.name"),
                firstNonEmpty(getS(row, "content.productOrder.inflowPath"),
                        getS(row, "content.order.payLocationType")),
                getS(row, "content.productOrder.productOption"),
                getI(row, "content.productOrder.quantity"),
                price,
                "NTD",
                getS(row, "content.productOrder.shippingMemo"),
                firstNonEmpty(getS(row, "content.productOrder.productOrderStatus"),
                        getS(row, "content.productOrder.placeOrderStatus")),
                "NOT_ISSUED",
                esimOrderId,
                email,
                phone
        );
    }

    /** 이메일 추출 */
    public static String extractEmail(Map<String, Object> row) {
        String BREAK_NULL = "disabledEmail";
        String memo = getS(row, "content.productOrder.shippingMemo");
        if (memo == null) return BREAK_NULL;

        Matcher m = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
                .matcher(memo);
        if (m.find()) {
            String email = m.group();
            log.debug("이메일 추출 성공: {}", email);
            return email;
        }
        return BREAK_NULL;
    }




}
