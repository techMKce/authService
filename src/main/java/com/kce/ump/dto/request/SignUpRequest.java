package com.kce.ump.dto.request;

import lombok.Data;

@Data
public class SignUpRequest {
    private String name;
    private String email;
    private String regNum;
    private String password;
}
