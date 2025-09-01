package com.kmong.domain.access;

import java.util.List;

public interface AccessLogRepository {
    void save(AccessLog accessLog);
    void saveBatch(List<AccessLog> accessLogs);
}
