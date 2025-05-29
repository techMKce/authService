package com.kce.ump.controller;


import com.kce.ump.dto.request.RejectionEmailRequest;
import com.kce.ump.model.user.User;
import com.kce.ump.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequiredArgsConstructor
@RequestMapping("/api/v1/email")
@RestController
public class EmailController {

    private final JavaMailSender mailSender;

    private final AuthenticationService authenticationService;

    @PostMapping("/sendRejectEmail")
    public ResponseEntity<Boolean> sendRejectEmail(@RequestParam("id") String id, @RequestBody Map<String, String> request) {

        try{
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            System.out.println("Sending rejection email to user with ID: " + id);
            User user = authenticationService.getUserById(id);
            mailMessage.setSubject("Your Assignment Submission has been Rejected");
            mailMessage.setText("Dear Student,\n\nYour assignment submission has been rejected due to some issues. Please check the feedback provided and resubmit your assignment.\n\nBest regards,\nThe UMP Team");
            mailMessage.setFrom("your-email@gmail.com");

            mailMessage.setTo(user.getEmail());

            mailSender.send(mailMessage);
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(false);
        }
        return ResponseEntity.ok(true);

    }

    @GetMapping
    public String getEmailServiceStatus() {
        return "Email service is running";
    }
}
