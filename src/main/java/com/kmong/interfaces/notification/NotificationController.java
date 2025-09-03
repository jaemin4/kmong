package com.kmong.interfaces.notification;

import com.kmong.domain.notification.NotificationCommand;
import com.kmong.domain.notification.NotificationResult;
import com.kmong.domain.notification.NotificationService;
import com.kmong.support.response.APIResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notification")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/register")
    @Operation(summary = "알림톡 내용 등록")
    public APIResponse<Void> registerNotification(@RequestBody NotificationRequest.Register request) {

        notificationService.register(NotificationCommand.Register.of(
                request.getKakaoAccessToken(),
                request.getSubject(),
                request.getContent(),
                request.getKeyString(),
                request.getKeyStringRange()
        ));

        return APIResponse.message("알림톡 내용이 등록되었습니다.");
    }

    @PostMapping("/update")
    @Operation(summary = "알림톡 내용 수정")
    public APIResponse<Void> updateNotification(@RequestBody NotificationRequest.Update request) {

        notificationService.update(NotificationCommand.Update.of(
                request.getKakaoAccessToken(),
                request.getSubject(),
                request.getContent(),
                request.getKeyString(),
                request.getKeyStringRange()
        ));

        return APIResponse.message("알림톡 내용이 업데이트 되었습니다.");
    }

    @GetMapping("/get")
    @Operation(summary = "알림톡 내용 조회")
    public APIResponse<NotificationResult.Get> getNotification() {

        NotificationResult.Get data = notificationService.get();

        return APIResponse.success(data);
    }


}
