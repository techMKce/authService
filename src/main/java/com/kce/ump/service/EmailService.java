package com.kce.ump.service;

import com.kce.ump.emailContext.AccountVerificationEmailContext;
import com.kce.ump.model.user.User;
import org.springframework.scheduling.annotation.Async;

public interface EmailService {


    @Async
    void welcomeMail(String toEmail, String password);


    @Async
    void sendEmail(AccountVerificationEmailContext emailContext);
}
