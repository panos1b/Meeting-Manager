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

    public static MeetingActions fromValue(int value) {
        for (MeetingActions action : values()) {
            if (action.value == value) {
                return action;
            }
        }
        throw new IllegalArgumentException("Invalid MeetingActions value: " + value);
    }
}
