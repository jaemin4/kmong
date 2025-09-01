package com.kmong.support.utils;

public class SmsUtils {

    public static final String fromNumber = "01046887175";
    public static final Integer validDuration = 120;

    public static String getContentForVerificationCode(String code) {
        return String.format("[Bovivet] 인증번호는 [%s]입니다. %d초 내에 입력해주세요.", code, validDuration);
    }


}
