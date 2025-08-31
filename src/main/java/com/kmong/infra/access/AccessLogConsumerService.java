package com.kmong.infra.access;

import com.kmong.domain.access.AccessLog;
import com.kmong.domain.access.AccessLogRepository;
import com.kmong.support.utils.JsonUtils;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Profile("consumer")
public class AccessLogConsumerService {

    private final AccessLogRepository accessLogRepository;
    private final List<AccessLog> accessLogList = new ArrayList<>();

    @RabbitListener(queues = "queue.access.log.save", concurrency = "1")
    public void saveAccessLog(AccessLogConsumerCommand.Save command) {
        try {
            AccessLog accessLog = AccessLog.of(command);
            accessLogList.add(accessLog);

            if (accessLogList.size() >= 5) {
                accessLogRepository.saveBatch(accessLogList);
                log.info("Save access log: {}\n", JsonUtils.toJson(accessLogList));
                accessLogList.clear();
            }

        } catch (Exception e) {
            log.error("AccessLog Save error : {},{}\n", e.getMessage(), JsonUtils.toJson(command));
        }
    }

    @PreDestroy
    public void flushRemainingLogs() {
        if (!accessLogList.isEmpty()) {
            try {
                accessLogRepository.saveBatch(accessLogList);
                log.info("Flushed remaining access logs: {}", JsonUtils.toJson(accessLogList));
                accessLogList.clear();
            } catch (Exception e) {
                log.error("Error while flushing remaining access logs: {}", e.getMessage());
            }
        }
    }


}
