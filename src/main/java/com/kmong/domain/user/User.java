package com.kmong.domain.user;

import com.kmong.support.utils.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "esim_user")
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE, toBuilder = true)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = false, length = 255)
    @Setter(AccessLevel.PROTECTED)
    private String password;

    @Column(length = 20, unique = true, nullable = false)
    private String phoneNumber;

    @Column(name = "privacy_agreed", nullable = false)
    private Boolean privacyAgreed;

    @Column(nullable = false)
    private Boolean enabled;

    @Version
    private Long version;

    public static User registerOf(String email, Role role, String username, String password, String phoneNumber, Boolean privacyAgreed, Boolean enabled) {
        if (!privacyAgreed) {
            throw new RuntimeException("개인정보 수집 및 이용에 동의하지 않으면 회원가입할 수 없습니다.");
        }

        return User.builder()
                .email(email)
                .role(role)
                .username(username)
                .password(password)
                .phoneNumber(phoneNumber)
                .privacyAgreed(true)
                .enabled(enabled)
                .build();
    }

    public void update(
            String email,
            String username,
            String password,
            String phoneNumber,
            Boolean enabled
    ) {
        User updated = this.toBuilder()
                .email(email != null ? email : this.email)
                .username(username != null ? username : this.username)
                .password(password != null ? password : this.password)
                .phoneNumber(phoneNumber != null ? phoneNumber : this.phoneNumber)
                .enabled(enabled != null ? enabled : this.enabled)
                .build();

        this.email = updated.email;
        this.username = updated.username;
        this.password = updated.password;
        this.phoneNumber = updated.phoneNumber;
        this.enabled = updated.enabled;
    }



}
