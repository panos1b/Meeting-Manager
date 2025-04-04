package com.example.MeetingsRedis.tasks;

import com.example.MeetingsRedis.model.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import static com.example.MeetingsRedis.model.RedisDataAccess.*;

@Component
public class RedisRefresh {
    private static final MeetingDAO meetingDAO = new MeetingDAO();
    private static final LogDAO logDAO = new LogDAO();
    private static final Jedis jedis = RedisConnection.getResource();

    @Scheduled(fixedRate = 60000) // runs every 60,000 ms = 1 minute
    public static void refreshActiveMeetings() throws SQLException {

        LocalDateTime now = LocalDateTime.now();
        List<Meeting> activeMeetings = meetingDAO.getActiveMeetings(now);
        List<Integer> meetingsAlreadyInRedis = RedisDataAccess.activeMeetings(jedis);

        for (Meeting meeting : activeMeetings) { //Add new
            if (!meetingsAlreadyInRedis.contains(meeting.getMeetingID()))
                addNewMeeting(jedis, meeting);
        }

        for (Integer meetingID : meetingsAlreadyInRedis){ //Remove old ended
            if (endTime(jedis,meetingID).isBefore(now)){
                Meeting meeting = getMeeting(jedis,meetingID); // get the meeting
                meetingDAO.updateMeeting(meeting); // update in DB
                logDAO.bulkInsertLogs(endMeeting(jedis, meetingID)); // Update logs
                // In the current implementation Messages are lost as per the assignment rules
            }

        }
    }


}