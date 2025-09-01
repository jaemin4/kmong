package com.kmong.interfaces.admin;

import com.kmong.domain.user.Role;
import com.kmong.domain.user.UserCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class AdminUserRequest {

    @AllArgsConstructor(staticName = "of")
    @Getter
    public static class RegisterAdmin {
        @Schema(description = "관리자 이메일", example = "admin@example.com")
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        private String email;

        @Schema(description = "관리자 이름", example = "관리자홍길동")
        @NotBlank(message = "이름은 필수입니다.")
        private String username;

        @Schema(description = "비밀번호", example = "SecurePass123!")
        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 8, max = 50, message = "비밀번호는 8자 이상 50자 이하여야 합니다.")
        private String password;

        @Schema(description = "전화번호", example = "01012345678")
        @NotBlank(message = "전화번호는 필수입니다.")
        @Pattern(regexp = "^\\d{10,11}$", message = "전화번호는 숫자만 10~11자리여야 합니다.")
        private String phoneNumber;

        public UserCommand.Register toEntity(Role role){
            return UserCommand.Register.of(email, role, username, password, phoneNumber, true, true);
        }

    }

}
