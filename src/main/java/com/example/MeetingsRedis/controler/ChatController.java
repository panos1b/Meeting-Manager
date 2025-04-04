package com.example.MeetingsRedis.controler;
import com.example.MeetingsRedis.model.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import redis.clients.jedis.Jedis;

import static com.example.MeetingsRedis.model.RedisDataAccess.*;

@Controller
@RequestMapping()
public class ChatController {
    private final Jedis jedis = RedisConnection.getResource();

    @GetMapping("/chat")
    public String chat(
            Model model,
            @RequestParam @Min(0) int meetingID
    ) {
        Meeting meeting = getMeeting(jedis, meetingID);
        model.addAttribute("meeting", meeting);
        model.addAttribute("messages", getMeetingMessages(jedis, meeting));
        return "chat";
    }


    @PostMapping("/create-message")
    public String postMessage(
            @RequestParam String messageBody,
            @RequestParam @Email String email,
            @RequestParam  int meetingID,
            RedirectAttributes redirectAttributes

    ) {
        redirectAttributes.addAttribute("meetingID",meetingID);
        Meeting meeting;
        try{
            meeting = getUsersMeeting(jedis, email);
            if (meetingID != (meeting.getMeetingID()))
                throw new NumberFormatException();
        }catch (NumberFormatException e){
            redirectAttributes.addFlashAttribute("warningMessage","You cant post messages to meetings you arent in!");
            return "redirect:/chat";
        }
        Message message = new Message(email, meetingID, messageBody);
        addNewMessage(jedis,message);

        return "redirect:/chat";
    }
}