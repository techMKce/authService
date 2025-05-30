package com.kce.ump.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UpdatePasswordRequest {
    private String email;
    private String newPassword;
}
