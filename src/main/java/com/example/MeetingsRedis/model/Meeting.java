package com.example.MeetingsRedis.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class Meeting {
    private int meetingID;
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private double lat;
    private double lon;
    private final List<String> participants = new ArrayList<>();
    private final List<Message> messages = new LinkedList<>();


    // Minimal constructor (required fields only)
    public Meeting(String title, LocalDateTime startTime, LocalDateTime endTime) {
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // Full constructor (all fields except auto-generated meetingID)
    public Meeting(String title, String description,
                   LocalDateTime startTime, LocalDateTime endTime,
                   double lat, double lon, List<String> participants) {
        this(title, startTime, endTime); // Call minimal constructor
        this.description = description;
        this.lat = lat;
        this.lon = lon;
        if (participants != null) {
            this.participants.addAll(participants);
        }
    }

    // Database reconstruction constructor (includes meetingID)
    public Meeting(int meetingID, String title, String description,
                   LocalDateTime startTime, LocalDateTime endTime,
                   double lat, double lon, List<String> participants) {
        this(title, description, startTime, endTime, lat, lon, participants);
        this.meetingID = meetingID;
    }

    // Getters and Setters (same as before)
    public int getMeetingID() { return meetingID; }
    public void setMeetingID(int meetingID) { this.meetingID = meetingID; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = Objects.requireNonNull(title); }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = Objects.requireNonNull(startTime);
    }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = Objects.requireNonNull(endTime);
    }

    public double getLat() { return lat; }
    public void setLat(double lat) { this.lat = lat; }

    public double getLon() { return lon; }
    public void setLon(double lon) { this.lon = lon; }

    public List<String> getParticipants() { return new ArrayList<>(participants); }

    // Participant management
    public void addParticipant(String email) {
        participants.add(Objects.requireNonNull(email));
    }

    public boolean removeParticipant(String email) {
        return participants.remove(email);
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void addMessage(Message message){
        this.messages.add(message);
    }

    @Override
    public String toString() {
        return String.format(
                "Meeting[id=%d, title=%s, start=%s, end=%s, location=(%.6f,%.6f), participants=%s]",
                meetingID, title, startTime, endTime, lat, lon, participants
        );
    }
}