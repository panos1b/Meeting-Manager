package com.example.MeetingsRedis.model;

import java.sql.*;
import java.util.Optional;

public class UserDAO {
    public void createUser(User user) throws SQLException {
        String sql = "INSERT INTO user (email, name, age, gender) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getEmail());
            pstmt.setString(2, user.getName());
            pstmt.setInt(3, user.getAge());
            pstmt.setString(4, user.getGender());
            pstmt.executeUpdate();
        }
    }

    public Optional<User> getUser(String email) throws SQLException {
        String sql = "SELECT * FROM user WHERE email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new User(
                            rs.getString("email"),
                            rs.getString("name"),
                            rs.getInt("age"),
                            rs.getString("gender")
                    ));
                }
            }
        }
        return Optional.empty();
    }

    public void updateUser(User user) throws SQLException {
        String sql = "UPDATE user SET name = ?, age = ?, gender = ? WHERE email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getName());
            pstmt.setInt(2, user.getAge());
            pstmt.setString(3, user.getGender());
            pstmt.setString(4, user.getEmail());
            pstmt.executeUpdate();
        }
    }

    public void deleteUser(String email) throws SQLException {
        String sql = "DELETE FROM user WHERE email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            pstmt.executeUpdate();
        }
    }
}