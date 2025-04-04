package com.example.MeetingsRedis.controler;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        // Get error information
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object error = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);

        // Set model attributes
        model.addAttribute("timestamp", LocalDateTime.now());
        model.addAttribute("status", status != null ? status : "N/A");
        model.addAttribute("error", error != null ? error.getClass().getSimpleName() : "N/A");
        model.addAttribute("message", message != null ? message : "No message available");
        model.addAttribute("path", request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI));

        return "error";
    }
}