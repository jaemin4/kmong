package com.kmong.aop.log;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.util.function.Supplier;

@Slf4j
public class AfterCommitLogger {

    public static void logInfoAfterCommit(Supplier<String> messageSupplier) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                log.info(messageSupplier.get());
            }
        });
    }

    public static void logDebugAfterCommit(Supplier<String> messageSupplier) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                log.debug(messageSupplier.get());
            }
        });
    }

}
