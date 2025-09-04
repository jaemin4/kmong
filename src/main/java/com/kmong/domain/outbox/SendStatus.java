package com.kmong.domain.outbox;

public enum SendStatus {
    PENDING,  // 아직 처리 안 됨
    SUCCESS,  // 처리 성공
    FAIL,      // 처리 실패
    SKIP     //
}
