package com.kmong.domain.user;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserResult {

    @Getter
    @AllArgsConstructor(staticName = "of")
    public static class Register {
        private User user;
    }


}
