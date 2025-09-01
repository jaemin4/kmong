package com.kmong.domain.user;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserCommand {

    @Getter
    @AllArgsConstructor(staticName = "of")
    public static class Register {
        private String email;
        private Role role;
        private String username;
        private String password;
        private String phoneNumber;
        private Boolean privacyAgreed;
        private Boolean enabled;

        public User toEntity(String encodedPassword) {
            return User.registerOf(email,role,username,encodedPassword, phoneNumber,privacyAgreed, enabled);
        }

    }


}
