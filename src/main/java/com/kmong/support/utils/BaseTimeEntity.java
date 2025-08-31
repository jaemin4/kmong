package com.kmong.support.utils;

import jakarta.persistence.*;
import lombok.Getter;
import java.time.LocalDateTime;

@MappedSuperclass
@Getter
public class BaseTimeEntity {

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = null;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
