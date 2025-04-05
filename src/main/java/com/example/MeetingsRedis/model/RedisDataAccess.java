package com.example.MeetingsRedis.model;

import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.GeoCoordinate;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.args.GeoUnit;
import redis.clients.jedis.params.GeoSearchParam;
import redis.clients.jedis.resps.GeoRadiusResponse;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

// Functions are labeled [1] through [10] to correspond with the exercise requirements
public class RedisDataAccess {
    // add a new meeting
    public static void addNewMeeting(@NotNull Jedis jedis, Meeting meeting ){
        String meetingID = String.valueOf(meeting.getMeetingID());
        jedis.sadd("meetings", meetingID);

        jedis.set("mt_title_" + meetingID, meeting.getTitle());
        jedis.set("mt_desc_" + meetingID, meeting.getDescription());
        jedis.set("mt_start_" + meetingID, meeting.getStartTime().toString());
        jedis.set("mt_end_" + meetingID, meeting.getEndTime().toString());

        jedis.geoadd(
                "mt_locations",
                meeting.getLon(),
                meeting.getLat(),
                String.valueOf(meeting.getMeetingID())
        );



    }

    // gets a meeting from redis
    public static Meeting getMeeting(@NotNull Jedis jedis, int meetingID){
        GeoCoordinate coordinates = jedis.geopos("mt_locations", String.valueOf(meetingID)).getFirst();

        return new Meeting(
                meetingID,
                jedis.get("mt_title_" + meetingID),
                jedis.get("mt_desc_" + meetingID),
                LocalDateTime.parse(jedis.get("mt_start_" + meetingID)),
                LocalDateTime.parse(jedis.get("mt_end_" + meetingID)),
                coordinates.getLatitude(),
                coordinates.getLongitude(),
                inMeeting(jedis, meetingID)
        );
    }

    // gets a meeting from redis
    public static Meeting getMeeting(@NotNull Jedis jedis, String meetingID){
        return getMeeting(jedis, Integer.parseInt(meetingID));
    }

    // get bulk meeting from redis
    public static List<Meeting> getMeetings(@NotNull Jedis jedis, List<?> meetingIDs) {
        List<Meeting> meetings = new ArrayList<>();
        for (Object meetingID : meetingIDs) {
            if (meetingID instanceof Integer) {
                meetings.add(getMeeting(jedis, (Integer) meetingID));
            } else {
                meetings.add(getMeeting(jedis, (String) meetingID));
            }
        }
        return meetings;
    }

    // get users meeting
    public static Meeting getUsersMeeting(Jedis jedis, String email){
        String meetingID = jedis.get(email);
        return getMeeting(jedis, meetingID);
    }

    // adds a new message
    public static void addNewMessage(@NotNull Jedis jedis, Message message){
        String messageList = "messages_" + message.getMeetingId();
        String messageID = Integer.toString(message.getId());
        jedis.rpush(
                messageList,
                messageID
        );

        jedis.set(
                "ms_sender_" + messageID,
                message.getSenderEmail()
        );

        jedis.set(
                "ms_body_" + messageID,
                message.getBody()
        );
    }

    // something is logged
    public static Log log(@NotNull Jedis jedis, User user, Meeting meeting, MeetingActions action){
        String userEmail = user.getEmail();
        int meetingID = meeting.getMeetingID();

        return log(jedis, userEmail, meetingID, action);
    }

    // something is logged
    public static Log log(@NotNull Jedis jedis, String userEmail, int meetingID, MeetingActions action){
        int actionValue = action.getValue();
        Log log = new Log(userEmail, meetingID, actionValue);
        String logId = String.valueOf(log.getId());

        // Logg keeper
        jedis.rpush("logs_" + log.getMeetingID(), String.valueOf(log.getId()));

        // Log contents
        jedis.set(
                "lg_email_" + logId,
                log.getEmail()
        );

        jedis.set(
                "lg_meetingId_" + logId,
                String.valueOf(log.getMeetingID())
        );

        jedis.set(
                "lg_timestamp_" + logId,
                String.valueOf(log.getTimestamp())
        );

        jedis.set(
                "lg_action_" + logId,
                String.valueOf(actionValue)
        );

        return log;
    }

    // users in meeting from ID
    public static List<String> inMeeting(@NotNull Jedis jedis, int meetingID){
        Set<String> temp = jedis.smembers("mt_participants_" + meetingID);
        return new ArrayList<>(temp);
    }

    // users in meeting from ID
    public static List<String> inMeeting(@NotNull Jedis jedis, String meetingID){
        Set<String> temp = jedis.smembers("mt_participants_" + meetingID);
        return new ArrayList<>(temp);
    }

    // find nearby meetings
    public static List<String> findNearbyMeetings(@NotNull Jedis jedis, double longitude, double latitude, int searchRadius) {
        // Configure geo search parameters
        GeoSearchParam params = GeoSearchParam.geoSearchParam()
                .byRadius(searchRadius, GeoUnit.M)
                .fromLonLat(longitude,latitude);

        // Execute the search and return meeting IDs
        return jedis.geosearch("mt_locations", params)
                .stream()
                .map(GeoRadiusResponse::getMemberByString)
                .collect(Collectors.toList());
    }

    // manually terminate redis run - killswitch - clears everything
    public static void end(@NotNull Jedis jedis){
        List<Integer> activeMeetings = activeMeetings(jedis);
        for(int meetingID : activeMeetings){
            endMeeting(jedis, meetingID);
        }
        jedis.flushAll();
    }

    // get a meeting endtime
    public static LocalDateTime endTime(Jedis jedis, int meetingID) {
        return LocalDateTime.parse(jedis.get("mt_end_" + meetingID));
    }

    // find nearby meetings at 100 meters [1]
    public static List<String> findNearbyMeetings(@NotNull Jedis jedis, double longitude, double latitude) {
        return findNearbyMeetings(jedis, longitude, latitude, 100);
    }

    // user joins [2]
    public static Log userJoins(@NotNull Jedis jedis, User user, Meeting meeting){
        String meetingId = Long.toString(meeting.getMeetingID());

        // Assign them to the meeting
        jedis.set(
                user.getEmail(),
                meetingId
        );

        // Add to meeting participants
        jedis.sadd(
                "mt_participants_" + meetingId,
                user.getEmail()
        );

        // Log it
        return RedisDataAccess.log(jedis, user, meeting, MeetingActions.JOIN);

    }

    // user leaves [3]
    public static Log userLeaves(@NotNull Jedis jedis, User user, Meeting meeting){
        String meetingId = Long.toString(meeting.getMeetingID());

        // Unassign them from the meeting
        jedis.del(user.getEmail());

        // Remove from meeting participants
        jedis.srem(
                "mt_participants_" + meetingId,
                user.getEmail()
        );

        // Log it
        return RedisDataAccess.log(jedis, user, meeting, MeetingActions.LEAVE);

    }

    // users in meeting [4]
    public static List<String> inMeeting(@NotNull Jedis jedis, Meeting meeting){
        return inMeeting(jedis,meeting.getMeetingID());
    }

    // active meetings in list [5]
    public static List<Integer> activeMeetings(@NotNull Jedis jedis){
        Set<String> stringIds = jedis.smembers("meetings");

        Set<Integer> intIds = new HashSet<>();

        for (String str : stringIds) {
            try {
                intIds.add(Integer.parseInt(str));
            } catch (NumberFormatException ignored) {
                System.err.println("INTEGER NOT PARSED! runningMeetings");
            }
        }
        return new ArrayList<>(intIds);
    }

    //end meeting and return logs for storage [6]
    public static List<Log> endMeeting(@NotNull Jedis jedis, int meetingID){
        // remove from list of active meetings
        jedis.srem("meetings", String.valueOf(meetingID));

        // remove the participant keys that show which meeting they are in and logs timeouts
        List<String> participants = inMeeting(jedis,meetingID);
        for (String email: participants){
            log(jedis, email, meetingID, MeetingActions.TIMEOUT);
            jedis.del(email);
        }

        // remove meeting info
        jedis.zrem("mt_locations", String.valueOf(meetingID));
        jedis.del("mt_participants_" + meetingID);
        jedis.del("mt_title_" + meetingID);
        jedis.del("mt_desc_" + meetingID);


        String messagesKey = "messages_" + meetingID;
        List<String> messageIds = jedis.lrange(messagesKey, 0, -1);

        // delete individual messages
        for(String messageId : messageIds){
            jedis.del("ms_sender_" + messageId);
            jedis.del("ms_body_" + messageId);
        }

        // delete message list
        jedis.del(messagesKey);


        String logsKey = "logs_" + meetingID;
        List<String> logIds = jedis.lrange(logsKey, 0, -1);

        List<Log> logs = new ArrayList<>();

        // save and then delete logs
        for(String logId : logIds){
            logs.add(new Log(
                    jedis.get("lg_email_" + logId),
                    Integer.parseInt(jedis.get("lg_meetingId_" + logId)) ,
                    LocalDateTime.parse(jedis.get("lg_timestamp_" + logId)),
                    Integer.parseInt(jedis.get("lg_action_" + logId))
            ));
            jedis.del("lg_email_" + logId);
            jedis.del("lg_meetingId_" + logId);
            jedis.del("lg_timestamp_" + logId);
            jedis.del("lg_action_" + logId);
        }

        // delete logs list
        jedis.del(logsKey);

        return logs;
    }

    // post message to the meeting user is in [7]
    public static Message postToChat(@NotNull Jedis jedis, User user, String text){
        int meetingId = Integer.parseInt(jedis.get(user.getEmail()));
        Message message = new Message(user.getEmail(), meetingId, text);
        addNewMessage(jedis, message);
        return message;
    }

    // get meeting messages [8]
    public static List<Message> getMeetingMessages(@NotNull Jedis jedis, Meeting meeting){
        int meetingId = meeting.getMeetingID();
        String messagesKey = "messages_" + meetingId;
        List<String> messageIds = jedis.lrange(messagesKey, 0, -1);

        List<Message> messages = new ArrayList<>();

        for(String messageId : messageIds){
            String email = jedis.get("ms_sender_" + messageId);
            String body = jedis.get("ms_body_" + messageId);
            messages.add(new Message(email, meetingId, body));
        }

        return messages;
    }

    // get user messages [9] - [10]
    public static List<Message> getUserMessages(@NotNull Jedis jedis, User user){
        String e = user.getEmail();
        String meetingId = jedis.get(e);
        int meetingIdInteger = Integer.parseInt(meetingId);
        String messagesKey = "messages_" + meetingId;
        List<String> messageIds = jedis.lrange(messagesKey, 0, -1);

        List<Message> messages = new ArrayList<>();

        for(String messageId : messageIds){
            String email = jedis.get("ms_sender_" + messageId);
            if(!email.equals(e)){
                continue;
            }
            String body = jedis.get("ms_body_" + messageId);
            messages.add(new Message(email, meetingIdInteger, body));
        }

        return messages;
    }

}
