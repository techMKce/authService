package com.kce.ump.service;

import com.kce.ump.emailContext.AccountVerificationEmailContext;
import com.kce.ump.model.user.User;

public interface EmailService {

    void sendEmail(AccountVerificationEmailContext emailContext);
}
