package com.kce.ump.emailContext;

import com.kce.ump.model.user.User;
import org.springframework.web.util.UriComponentsBuilder;

public class AccountVerificationEmailContext extends AbstractEmailContext {

    private String token;

    @Override
    public <T> void init(T context) {
        User user = (User) context;

        put("name", user.getName());
        setTemplateLocation("mailing/email-verification");
        setSubject("Forgot Password Verification");
        setFrom("vignesh.dev.se@gmail.com");
        setTo(user.getEmail());
    }

    public void setToken(String token) {
        this.token = token;
        put("token", token);
    }

    public void buildVerificationUrl(final String baseURL) {
        final String url = UriComponentsBuilder.fromHttpUrl(baseURL)
                .path("/api/v1/auth/verify").queryParam("token", token).toUriString();
        put("verificationURL", url);
    }

}
