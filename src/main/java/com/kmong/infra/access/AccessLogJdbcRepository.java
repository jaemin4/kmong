package com.kmong.infra.access;

import com.kmong.domain.access.AccessLog;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AccessLogJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public void batchInsert(List<AccessLog> accessLogs) {
        String sql = """
            INSERT INTO tb_access_log (
                user_id, role, method, uri, query, request_body, response_body,
                headers, user_agent, remote_ip, status, thread_name,
                request_at, response_at, duration_ms
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                AccessLog log = accessLogs.get(i);
                ps.setLong(1, log.getUserId());
                ps.setString(2, log.getRole());
                ps.setString(3, log.getMethod());
                ps.setString(4, log.getUri());
                ps.setString(5, log.getQuery());
                ps.setString(6, log.getRequestBody());
                ps.setString(7, log.getResponseBody());
                ps.setString(8, log.getHeaders());
                ps.setString(9, log.getUserAgent());
                ps.setString(10, log.getRemoteIp());
                ps.setInt(11, log.getStatus());
                ps.setString(12, log.getThreadName());
                ps.setObject(13, log.getRequestAt());
                ps.setObject(14, log.getResponseAt());
                ps.setLong(15, log.getDurationMs());
            }

            @Override
            public int getBatchSize() {
                return accessLogs.size();
            }
        });
    }
}
