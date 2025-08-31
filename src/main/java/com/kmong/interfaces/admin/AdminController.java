package com.kmong.interfaces.admin;

import com.kmong.domain.user.Role;
import com.kmong.domain.user.UserService;
import com.kmong.support.response.APIResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
@Tag(name = "유저 관리 API", description = "")
@Slf4j
public class AdminController {

    private final UserService userService;

    @PostMapping("/register/admin")
    @Operation(summary = "관리자 계정 등록", description = "최상위 관리자 계정을 등록합니다. ")
    public APIResponse<Void> registerAdmin(@RequestBody @Valid AdminUserRequest.RegisterAdmin request) {
        userService.register(request.toEntity(Role.ADMIN));
        return APIResponse.message("관리자 계정 등록이 완료되었습니다.");
    }

}
