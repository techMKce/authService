package com.kce.ump.dto.response;

import com.kce.ump.model.user.Profile;
import lombok.Data;

@Data
public class JwtAuthResponse {
    private String token;
    private String refreshToken;
    private Profile profile;
}
