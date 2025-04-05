package com.example.MeetingsRedis.controler;

import com.example.MeetingsRedis.model.*;
import com.example.MeetingsRedis.tasks.RedisRefresh;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import redis.clients.jedis.Jedis;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static com.example.MeetingsRedis.model.RedisDataAccess.*;

@Controller
public class MainController {
    private final Jedis jedis = RedisConnection.getResource();
    private final MeetingDAO meetingDAO = new MeetingDAO();
    private final UserDAO userDAO = new UserDAO();
    private final LogDAO logDAO = new LogDAO();

    @GetMapping("/test")
    public String index() {
        // Create user
        User user = new User("alice@example.com", "Alice Doe", 43, "Female");
        User user2 = new User("bob@example.com", "Bod Doe", 23, "Male");

        // Create a meeting
        MeetingDAO meetingDao = new MeetingDAO();
        Meeting meeting = new Meeting(
                "Slet",
                "Slettt",
                LocalDateTime.now().minusMinutes(1),
                LocalDateTime.now().plusHours(2),
                43.712800, -78.006400,
                List.of("alice@example.com", "bob@example.com")
        );

        try {
            meeting.setMeetingID(meetingDao.createMeeting(meeting));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        int meetingId = meeting.getMeetingID();

        // Create another meeting
        Meeting meeting2 = new Meeting(
                "Project Review",
                "Quarterly project review",
                LocalDateTime.now().minusMinutes(1),
                LocalDateTime.now().plusHours(2),
                40.712844, -74.006440,
                List.of("alice@example.com", "bob@example.com")
        );

        try {
            meeting.setMeetingID(meetingDao.createMeeting(meeting2));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


        userJoins(jedis, user, meeting);
        userJoins(jedis, user2, meeting);

        System.out.println("--- inMeeting ---");
        System.out.println(inMeeting(jedis, meetingId));

        System.out.println("--- postToChat ---");
        System.out.println(postToChat(jedis, user, "YOO FUCK YOU!!!").getBody());
        System.out.println(postToChat(jedis, user2, "YOO FUCK YOU TOO").getBody());

        System.out.println("--- getMeetingMessages ---");
        System.out.println(getMeetingMessages(jedis, meeting));

        System.out.println("--- getUserMessages ---");
        System.out.println(getUserMessages(jedis, user2));

        userLeaves(jedis, user, meeting);

        System.out.println("--- runningMeetings ---");
        System.out.println(RedisDataAccess.activeMeetings(jedis));

        System.out.println("--- findNearbyMeetings ---");
        System.out.println(RedisDataAccess.findNearbyMeetings(jedis, -74.0064, 40.7128));

        System.out.println("--- endMeeting ---");
        List<Log> logs = endMeeting(jedis, meeting.getMeetingID());

        LogDAO logDAO = new LogDAO();
        try {
            logDAO.bulkInsertLogs(logs);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        System.out.println(logs);
        return "Greetings from Spring Boot!";
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("welcomeMessage", "Welcome to Meeting Manager!");
        return "index";
    }

    @GetMapping("/meetings")
    public String meetings(Model model) {
        List<Integer> meetings = RedisDataAccess.activeMeetings(jedis);
        model.addAttribute("meetings", getMeetings(jedis, meetings));
        return "active_meetings";
    }

    @GetMapping("/active-meetings")
    public String activeMeetings(
            @RequestParam @Min(-90) @Max(90) double lat,
            @RequestParam @Min(-180) @Max(180) double lng,
            Model model) {
        List<String> nearbyMeetingIDs = findNearbyMeetings(jedis, lng, lat);
        model.addAttribute("meetings", getMeetings(jedis, nearbyMeetingIDs));
        return "active_meetings";
    }


    //Create Meeting Card
    @PostMapping("/create-meeting")
    public String createMeeting(@RequestParam @NotNull @NotEmpty String title,
                                @RequestParam @NotNull @NotEmpty String description,
                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
                                @RequestParam @Min(-90) @Max(90) double lat,
                                @RequestParam @Min(-180) @Max(180) double lon,
                                RedirectAttributes redirectAttributes) {


        Meeting meeting = new Meeting(title, description, startTime, endTime, lat, lon, new ArrayList<>());
        try {
            meeting.setMeetingID(meetingDAO.createMeeting(meeting));
            RedisRefresh.refreshActiveMeetings(); // manual refresh for no delays as it might have started
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        redirectAttributes.addFlashAttribute("message", "Meeting '" + title + "' created successfully!");

        return "redirect:/"; // redirect to homepage
    }

    // Create User Card
    @PostMapping("/create-user")
    public String createUser(@RequestParam @Email(message = "Invalid email format") String email,
                             @RequestParam @NotNull @NotEmpty String name,
                             @RequestParam @Min(0) @Max(125) int age,
                             @RequestParam @Pattern(regexp = "^(Male|Female|Other)$") String gender,
                             RedirectAttributes redirectAttributes) {
        User user = new User(email, name, age, gender);
        try {
            userDAO.createUser(user);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        redirectAttributes.addFlashAttribute("message", "User " + name + " was successfully created!");
        return "redirect:/"; // redirects back to index.html with flash message
    }

    // Join Meeting Card
    @PostMapping("/join")
    @GetMapping("/join")
    public String joinMeeting(
            @RequestParam @Email(message = "Invalid email format") String email,
            @RequestParam @Min(0) int meetingId,
            RedirectAttributes redirectAttributes
    ) {
        try {
            Meeting meeting = getMeeting(jedis,meetingId);
            userJoins(jedis,userDAO.getUser(email).get(), meeting);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (NoSuchElementException e) {
            System.err.println("That user does not exist");
            throw new IllegalArgumentException(e);
        } catch (Exception e){
            System.err.println("That meeting does not exist");
            throw new IllegalArgumentException(e);
        }

        redirectAttributes.addFlashAttribute("message", "User " + email +
                " was successfully added to meeting " + meetingId);
        return "redirect:/"; // redirects back to index.html with flash message
    }


    // Leave Meeting Card
    @PostMapping("/leave")
    @GetMapping("/leave")
    public String leaveMeeting(
            @RequestParam @Email(message = "Invalid email format") String email,
            @RequestParam @Min(0) int meetingId,
            RedirectAttributes redirectAttributes
    ) {
        try {
            Meeting meeting = getMeeting(jedis,meetingId);
            userLeaves(jedis,userDAO.getUser(email).get(), meeting);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (NoSuchElementException e) {
            System.err.println("That user does not exist");
            throw new IllegalArgumentException(e);
        } catch (Exception e){
            System.err.println("That meeting does not exist");
            throw new IllegalArgumentException(e);
        }

        redirectAttributes.addFlashAttribute("message", "User " + email +
                " was successfully removed from meeting " + meetingId);
        return "redirect:/"; // redirects back to index.html with flash message
    }

    // End Meeting Card meetingId
    @PostMapping("/end")
    @GetMapping("/end")
    public String earlyEndMeeting(
            @RequestParam @Min(0) int meetingId,
            RedirectAttributes redirectAttributes
    ) {
        try {
            Meeting meeting = getMeeting(jedis,meetingId);
            meeting.setEndTime(LocalDateTime.now());
            meetingDAO.updateMeeting(meeting);
            logDAO.bulkInsertLogs(endMeeting(jedis, meetingId));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (NoSuchElementException e){
            throw new IllegalArgumentException(e);
        }
        redirectAttributes.addFlashAttribute("message", "Meeting " + meetingId +
                " has now ended :(");
        return "redirect:/"; // redirects back to index.html with flash message
    }



}