package com.kmong.domain.notification;

import com.kmong.aop.log.AfterCommitLogger;
import com.kmong.aop.log.RequestFlowLogger;
import com.kmong.support.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public void register(NotificationCommand.Register command){
        Notification notification = Notification.of(
                command.getKakaoAccessToken(),
                command.getSubject(),
                command.getContent(),
                command.getKeyString(),
                command.getKeyStringRange()
        );

        Notification saved = notificationRepository.save(notification);

        AfterCommitLogger.logInfoAfterCommit(() ->
                String.format("[%s] saved Notification: %s", RequestFlowLogger.getCurrentUUID(), JsonUtils.toJson(saved))
        );
    }


}
