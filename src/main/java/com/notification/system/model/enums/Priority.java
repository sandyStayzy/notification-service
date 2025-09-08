package com.notification.system.model.enums;

public enum Priority {
    HIGH(1),
    MEDIUM(2),
    LOW(3);

    private final int value;

    Priority(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}