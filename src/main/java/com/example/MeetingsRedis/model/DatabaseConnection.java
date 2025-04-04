package com.example.MeetingsRedis.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private static final String DB_URL = "jdbc:sqlite:meetings.db";

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to load SQLite JDBC driver", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void initializeDatabase() {
        // SQL statements with proper syntax
        String[] createTables = {
                """
            CREATE TABLE IF NOT EXISTS user (
                email TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                age INTEGER,
                gender TEXT
            )
            """,
                """
            CREATE TABLE IF NOT EXISTS meeting (
                meetingID INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                description TEXT,
                t1 TEXT NOT NULL,
                t2 TEXT NOT NULL,
                lat REAL,
                long REAL,
                participants TEXT
            )
            """,
                """
            CREATE TABLE IF NOT EXISTS log (
                email TEXT NOT NULL,
                meetingID INTEGER NOT NULL,
                timestamp TEXT NOT NULL,
                action INTEGER NOT NULL,
                FOREIGN KEY (email) REFERENCES user (email) ON DELETE CASCADE,
                FOREIGN KEY (meetingID) REFERENCES meeting (meetingID) ON DELETE CASCADE
            )
            """
        };

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Enable foreign key support
            stmt.execute("PRAGMA foreign_keys = ON");

            // Create all tables
            for (String sql : createTables) {
                stmt.execute(sql);
            }

            // Verify tables were created
            System.out.println("Database initialized successfully");

        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    // Optional: Add methods for database maintenance
    public static void clearDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("PRAGMA foreign_keys = OFF");

            String[] tables = {"log", "meeting", "user"};
            for (String table : tables) {
                stmt.execute("DROP TABLE IF EXISTS " + table);
            }

            stmt.execute("PRAGMA foreign_keys = ON");
            System.out.println("Database cleared successfully");

        } catch (SQLException e) {
            throw new RuntimeException("Failed to clear database", e);
        }
    }
}