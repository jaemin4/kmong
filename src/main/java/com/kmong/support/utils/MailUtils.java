package com.kmong.support.utils;

public class MailUtils {

    public static final String setFrom = "eheh8734@naver.com";
    public static final Integer validDuration = 120;

    public static String getSubjectForVerificationEmail() {
        return "[보비벳] 이메일 인증 코드 안내";
    }

    public static String getBodyForVerificationEmail(String code) {
        return String.format(
                "안녕하세요, 보비벳입니다.\n\n" +
                        "요청하신 서비스에 대한 이메일 인증 코드입니다.\n\n" +
                        "아래 인증 코드를 입력해 주세요:\n\n" +
                        "%s\n\n" +
                        "※ 본 인증 코드는 %d초간 유효합니다.\n" +
                        "※ 본 메일은 발신 전용입니다. 문의사항은 관리자에게 문의해주세요",
                code,validDuration
        );
    }

    public static String getSubjectForIssueTempPw() {
        return "[보비벳] 임시 비밀번호 발급 안내";
    }

    public static String getBodyForIssueTempPw(String tempPassword) {
        return String.format(
                "안녕하세요, 보비벳입니다.\n\n" +
                        "요청하신 비밀번호 재설정 요청이 확인되어 임시 비밀번호를 발급해드렸습니다.\n\n" +
                        "아래 임시 비밀번호로 로그인하신 후, 반드시 [마이페이지 > 비밀번호 변경] 메뉴에서 새 비밀번호로 변경해 주세요.\n\n" +
                        "임시 비밀번호: %s\n\n" +
                        "※ 본 메일은 발신 전용입니다. 문의사항은 관리자에게 연락 부탁드립니다.",
                tempPassword, validDuration
        );
    }


}
