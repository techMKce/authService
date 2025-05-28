package com.kce.ump.service.impl;

import com.kce.ump.emailContext.AccountVerificationEmailContext;
import com.kce.ump.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void welcomeMail(String toEmail, String password) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Welcome to ZenoUMS ");
        message.setText("Welcome Onboard: Please Find the Login Credentials below\n Email: " + toEmail +"\nPassword:" + password);
        message.setFrom("vignesh.dev.se@gmail.com");

        mailSender.send(message);
    }


    @Override
    public void sendEmail(AccountVerificationEmailContext emailContext) {
        try{
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(emailContext.getTo());
            message.setSubject(emailContext.getSubject());
            message.setFrom(emailContext.getFrom());
            message.setText(buildSimpleBody(emailContext));
            mailSender.send(message);
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("Failed to send email");
        }
    }

    private String buildSimpleBody(AccountVerificationEmailContext context) {
        // Basic example - you can customize this
        String name = (String) context.getContext().get("name");
        String token = (String) context.getContext().get("token");
        String url = (String) context.getContext().get("verificationURL");

        return "Hello " + name + ",\n\n"
                + "Please verify your email using the following link:\n"
                + url + "\n\n"
                + "Token: " + token + "\n\n"
                + "Thank you!";
    }
}
