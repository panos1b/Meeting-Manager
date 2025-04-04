package com.example.MeetingsRedis.model;

public class Message {
    private static int count = 0;

    private final int Id;
    private final String senderEmail;
    private final int meetingId;
    private final String body;


    public Message(String senderEmail, int meetingId, String body){
        this.senderEmail = senderEmail;
        this.meetingId = meetingId;
        this.body = body;
        this.Id = count;
        count++;
    }

    public int getId() {
        return Id;
    }

    public int getMeetingId() {
        return meetingId;
    }

    public String getBody() {
        return body;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    @Override
    public String toString() {
        return "Message:" +
                "sender=" + senderEmail +
                ", meeting=" + meetingId +
                ", body='" + body + '\'';
    }
}
