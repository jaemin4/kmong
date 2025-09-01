package com.kmong.infra.access;

import com.kmong.domain.access.AccessLog;
import com.kmong.domain.access.AccessLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class AccessLogRepositoryImpl implements AccessLogRepository {

    private final AccessLogJpaRepository accessLogJpaRepository;
    private final AccessLogJdbcRepository accessLogJdbcRepository;

    @Override
    public void save(AccessLog accessLog) {
        accessLogJpaRepository.save(accessLog);
    }

    @Override
    public void saveBatch(List<AccessLog> accessLogs) {
        accessLogJdbcRepository.batchInsert(accessLogs);
    }
}
