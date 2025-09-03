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
        boolean enable = notificationRepository.count() < 1;

        if(!enable){
            throw new RuntimeException("최초 등록만 가능합니다. 업데이를 진행해주세요. ");
        }

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

    @Transactional
    public void update(NotificationCommand.Update command){

        Notification notification = notificationRepository.findByeKey(1).orElseThrow(() ->
                new RuntimeException("존재하지 않는 ID 입니다."));

        notification.update(
                command.getKakaoAccessToken(),
                command.getSubject(),
                command.getContent(),
                command.getKeyString(),
                command.getKeyStringRange()
        );

        Notification saved = notificationRepository.save(notification);

        AfterCommitLogger.logInfoAfterCommit(() ->
                String.format("[%s] updated Notification: %s", RequestFlowLogger.getCurrentUUID(), JsonUtils.toJson(saved))
        );
    }

    public NotificationResult.Get get(){
        Notification notification = notificationRepository.findByeKey(1).orElseThrow(() ->
                new RuntimeException("데이터가 존재하지 않습니다."));

        return NotificationResult.Get.of(notification);
    }


}
