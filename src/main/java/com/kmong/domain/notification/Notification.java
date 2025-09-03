package com.kmong.domain.notification;

import com.kmong.support.utils.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "esim_notification")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE, toBuilder = true)
public class Notification extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 65)
    private String kakaoAccessToken;

    @Column(length = 200)
    private String subject;

    @Lob
    private String content;

    @Lob
    private String keyString;

    @Lob
    private String keyStringRange;

    public static Notification of(String kakaoAccessToken, String subject, String content, String keyString, String keyStringRange) {
        return Notification.builder()
                .kakaoAccessToken(kakaoAccessToken)
                .subject(subject)
                .content(content)
                .keyString(keyString)
                .keyStringRange(keyStringRange)
                .build();
    }

    public void update(String kakaoAccessToken, String subject, String content, String keyString, String keyStringRange) {
        if (kakaoAccessToken != null && !kakaoAccessToken.isBlank()) {
            this.kakaoAccessToken = kakaoAccessToken;
        }
        if (subject != null && !subject.isBlank()) {
            this.subject = subject;
        }
        if (content != null && !content.isBlank()) {
            this.content = content;
        }
        if(keyString != null && !keyString.isBlank()){
            this.keyString = keyString;
        }
        if(keyStringRange != null && !keyStringRange.isBlank()){
            this.keyStringRange = keyStringRange;
        }
    }
}
