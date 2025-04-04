package com.example.MeetingsRedis.model;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class LogDAO {
    public void logAction(Log log) throws SQLException {
        String sql = "INSERT INTO log (email, meetingID, timestamp, action) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, log.getEmail());
            pstmt.setLong(2, log.getMeetingID());
            pstmt.setString(3, log.getTimestamp().toString());
            pstmt.setInt(4, log.getAction());
            pstmt.executeUpdate();
        }
    }

    public List<Log> getLogsForMeeting(long meetingId) throws SQLException {
        String sql = "SELECT * FROM log WHERE meetingID = ? ORDER BY timestamp";
        List<Log> logs = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, meetingId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(new Log(
                            rs.getString("email"),
                            rs.getInt("meetingID"),
                            LocalDateTime.parse(rs.getString("timestamp")),
                            rs.getInt("action")
                    ));
                }
            }
        }
        return logs;
    }

    public void bulkInsertLogs(List<Log> logs) throws SQLException {
        if (logs == null || logs.isEmpty()) {
            return; // Nothing to insert
        }

        String sql = "INSERT INTO log (email, meetingID, timestamp, action) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);

            try {
                for (Log log : logs) {
                    pstmt.setString(1, log.getEmail());
                    pstmt.setLong(2, log.getMeetingID());
                    pstmt.setString(3, log.getTimestamp().toString());
                    pstmt.setInt(4, log.getAction());
                    pstmt.addBatch();
                }


                pstmt.executeBatch();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }
}