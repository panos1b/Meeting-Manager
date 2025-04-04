package com.example.MeetingsRedis.model;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MeetingDAO {
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public int createMeeting(Meeting meeting) throws SQLException {
        String sql = """
            INSERT INTO meeting 
            (title, description, t1, t2, lat, long, participants)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            conn.setAutoCommit(false);

            pstmt.setString(1, meeting.getTitle());
            pstmt.setString(2, meeting.getDescription());
            pstmt.setString(3, meeting.getStartTime().toString());
            pstmt.setString(4, meeting.getEndTime().toString());
            pstmt.setDouble(5, meeting.getLat());
            pstmt.setDouble(6, meeting.getLon());
            pstmt.setString(7, String.join(",", meeting.getParticipants()));

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating meeting failed, no rows affected");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int newId = generatedKeys.getInt(1);
                    conn.commit();
                    return newId;
                } else {
                    conn.rollback();
                    throw new SQLException("Creating meeting failed, no ID obtained");
                }
            }
        }
    }

    public Optional<Meeting> getMeetingById(int meetingId) throws SQLException {
        String sql = "SELECT * FROM meeting WHERE meetingID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, meetingId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Meeting(
                            rs.getInt("meetingID"),
                            rs.getString("title"),
                            rs.getString("description"),
                            LocalDateTime.parse(rs.getString("t1")),
                            LocalDateTime.parse(rs.getString("t2")),
                            rs.getDouble("lat"),
                            rs.getDouble("long"),
                            List.of(rs.getString("participants").split(","))
                    ));
                }
            }
        }
        return Optional.empty();
    }

    public List<Meeting> getMeetingsByParticipant(String email) throws SQLException {
        String sql = "SELECT * FROM meeting WHERE participants LIKE ?";
        List<Meeting> meetings = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + email + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    meetings.add(new Meeting(
                            rs.getInt("meetingID"),
                            rs.getString("title"),
                            rs.getString("description"),
                            LocalDateTime.parse(rs.getString("t1")),
                            LocalDateTime.parse(rs.getString("t2")),
                            rs.getDouble("lat"),
                            rs.getDouble("long"),
                            List.of(rs.getString("participants").split(","))
                    ));
                }
            }
        }
        return meetings;
    }

    public List<Meeting> getActiveMeetings(LocalDateTime now) throws SQLException {
        String sql = """
        SELECT * FROM meeting 
        WHERE t1 <= ? AND t2 > ?
        """;

        List<Meeting> meetings = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, now.format(ISO_FORMATTER));
            pstmt.setString(2, now.format(ISO_FORMATTER));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    meetings.add(new Meeting(
                            rs.getInt("meetingID"),
                            rs.getString("title"),
                            rs.getString("description"),
                            LocalDateTime.parse(rs.getString("t1")), // Direct ISO parse
                            LocalDateTime.parse(rs.getString("t2")),   // Direct ISO parse
                            rs.getDouble("lat"),
                            rs.getDouble("long"),
                            List.of(rs.getString("participants").split(","))
                            )
                    );
                }
            }
        }
        return meetings;
    }

    public void updateMeeting(Meeting meeting) throws SQLException {
        String sql = """
        UPDATE meeting 
        SET title = ?, 
            description = ?, 
            t1 = ?, 
            t2 = ?, 
            lat = ?, 
            long = ?, 
            participants = ?
        WHERE meetingID = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);

            pstmt.setString(1, meeting.getTitle());
            pstmt.setString(2, meeting.getDescription());
            pstmt.setString(3, meeting.getStartTime().toString());
            pstmt.setString(4, meeting.getEndTime().toString());
            pstmt.setDouble(5, meeting.getLat());
            pstmt.setDouble(6, meeting.getLon());
            pstmt.setString(7, String.join(",", meeting.getParticipants()));
            pstmt.setInt(8, meeting.getMeetingID());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                conn.rollback();
                throw new SQLException("Updating meeting failed, no rows affected");
            }

            conn.commit();
        }
    }


}