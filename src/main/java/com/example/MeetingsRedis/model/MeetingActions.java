package com.example.MeetingsRedis.model;

public enum MeetingActions {
    JOIN(1),
    LEAVE(2),
    TIMEOUT(3);

    private final int value;

    MeetingActions(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
