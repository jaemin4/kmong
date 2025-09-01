package com.kmong.domain.user;

public enum Role {
    ADMIN("관리자"),
    ANY("권한 없음");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
