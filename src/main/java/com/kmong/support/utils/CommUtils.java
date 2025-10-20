package com.kmong.support.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static com.kmong.support.utils.JsonPathUtils.*;
import static com.kmong.support.utils.JsonPathUtils.getD;


@RequiredArgsConstructor
@Slf4j
public class CommUtils {

    private static final Random random = new Random();
    private static final Integer PASSWORD_LENGTH = 8;
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    public static String generateCode6() {
        Integer code = random.nextInt(1_000_000);
        return String.format("%06d", code);
    }

    public static String generateTempPw() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(8);

        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }

        String rawPw = sb.toString();
        log.info("Generated Temp Password: {}", rawPw);
        return rawPw;
    }

    /** 템플릿 렌더링 */
    public static String renderByKeys(String template, String keyCsv, Map<String, Object> row) {
        if (isBlank(template) || isBlank(keyCsv)) return template;

        String result = template;
        for (String rawKey : keyCsv.split(",")) {
            String key = rawKey.trim();
            String value = resolveValueByKey(key, row);
            result = result.replace("{" + key + "}", value == null ? "" : value);
        }
        return result;
    }

    public static String resolveValueByKey(String key, Map<String, Object> row) {
        String path = KEY_PATHS.get(key);
        Object value = path != null ? getO(row, path) : null;

        if (value == null) {
            return switch (key) {
                case "purchaseChannel" -> firstNonEmpty(
                        getS(row, "content.productOrder.inflowPath"),
                        getS(row, "content.order.payLocationType"));
                case "paymentStatus" -> firstNonEmpty(
                        getS(row, "content.productOrder.productOrderStatus"),
                        getS(row, "content.productOrder.placeOrderStatus"));
                default -> "";
            };
        }
        return value.toString();
    }

    /** 공통 유틸 */
    public static boolean isBlank(String s) { return s == null || s.isBlank(); }
    public static String firstNonEmpty(String a, String b) {
        return (a != null && !a.isBlank() && !"null".equalsIgnoreCase(a)) ? a : b;
    }

    public static Object getO(Map<String, Object> row, String path) {
        try {
            Object val = getS(row, path);
            if (val != null) return val;
            if (getI(row, path) != null) return getI(row, path);
            if (getD(row, path) != null) return getD(row, path);
        } catch (Exception ignored) {}
        return null;
    }

    public static final Map<String, String> KEY_PATHS = Map.ofEntries(
            Map.entry("productOrderId", "content.productOrder.productOrderId"),
            Map.entry("orderDate", "content.order.orderDate"),
            Map.entry("orderNumber", "content.order.orderId"),
            Map.entry("ordererName", "content.order.ordererName"),
            Map.entry("receiverName", "content.productOrder.shippingAddress.name"),
            Map.entry("purchaseChannel", "content.productOrder.inflowPath"),
            Map.entry("productOption", "content.productOrder.productOption"),
            Map.entry("quantity", "content.productOrder.quantity"),
            Map.entry("originalPrice", "content.productOrder.unitPrice"),
            Map.entry("currency", "content.productOrder.currency"),
            Map.entry("message", "content.productOrder.shippingMemo"),
            Map.entry("paymentStatus", "content.productOrder.productOrderStatus"),
            Map.entry("issueStatus", "content.productOrder.issueStatus")
    );

}
