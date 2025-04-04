package com.example.MeetingsRedis.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Log {
    private static int counter = 0;

    private final int Id;
    private final String email;
    private final long meetingID;
    private final LocalDateTime timestamp;
    private final int action;


    public Log(String email, int meetingID, int action) {
        this(email, meetingID, LocalDateTime.now(), action);
    }

    public Log(String email, int meetingID, LocalDateTime timestamp, int action) {
        this.email = Objects.requireNonNull(email);
        this.meetingID = meetingID;
        this.timestamp = Objects.requireNonNull(timestamp);
        this.action = validateAction(action);
        this.Id = counter;
        counter++;
    }

    private static int validateAction(int action) {
        if (action < 1 || action > 3) {
            throw new IllegalArgumentException("Action must be 1 (join), 2 (leave), or 3 (timeout)");
        }
        return action;
    }

    // Getters
    public String getEmail() { return email; }
    public long getMeetingID() { return meetingID; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public int getAction() { return action; }
    public int getId() {
        return Id;
    }

    public String getActionName() {
        return switch (action) {
            case 1 -> "JOIN";
            case 2 -> "LEAVE";
            case 3 -> "TIMEOUT";
            default -> throw new IllegalStateException("Invalid action value");
        };
    }

    @Override
    public String toString() {
        return String.format(
                "Log[email=%s, meeting=%d, time=%s, action=%s]",
                email, meetingID, timestamp, getActionName()
        );
    }
}