package com.kmong.domain.outbox;

import com.kmong.aop.log.AfterCommitLogger;
import com.kmong.aop.log.RequestFlowLogger;
import com.kmong.domain.user.User;
import com.kmong.domain.user.UserCommand;
import com.kmong.domain.user.UserResult;
import com.kmong.support.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutBoxService {

    private final OrderOutboxRepository orderOutboxRepository;

    @Transactional
    public void registerOrderOutBox(OutboxCommand.RegisterOrderOutbox command) {
        OrderOutbox orderOutbox = OrderOutbox.of(
                command.getOrderProductId(),
                command.getPartnerApiName(),
                command.getPartnerApiStatus()
        );

        OrderOutbox saved = orderOutboxRepository.save(orderOutbox);

        AfterCommitLogger.logInfoAfterCommit(() ->
                String.format("[%s] saved orderOutbox: %s", RequestFlowLogger.getCurrentUUID(), JsonUtils.toJson(saved))
        );
    }

}
