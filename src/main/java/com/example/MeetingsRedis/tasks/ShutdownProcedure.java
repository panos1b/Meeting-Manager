package com.example.MeetingsRedis.tasks;

import com.example.MeetingsRedis.model.Log;
import com.example.MeetingsRedis.model.LogDAO;
import com.example.MeetingsRedis.model.RedisConnection;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import java.sql.SQLException;
import java.util.List;

import static com.example.MeetingsRedis.model.RedisDataAccess.activeMeetings;
import static com.example.MeetingsRedis.model.RedisDataAccess.endMeeting;

@Component
public class ShutdownProcedure {
    private final Jedis jedis = RedisConnection.getResource();
    private final LogDAO logDAO = new LogDAO();

    @PreDestroy
    public void onShutdown() {
        System.out.println("ShutdownCleanup: Running cleanup before Spring shuts down.");
        List<Log> logs;

        List<Integer> activeMeetings = activeMeetings(jedis);
        for(int meetingID : activeMeetings){

            System.out.println("Ending Meeting " + meetingID);
            logs = endMeeting(jedis, meetingID);

            System.out.println("Saving Logs for " + meetingID);
            try {
                logDAO.bulkInsertLogs(logs);
            } catch (SQLException e) {
                System.err.println("[ERROR] Saving Logs failed for " + meetingID);
                throw new RuntimeException(e);
            }

        }
    }
}
