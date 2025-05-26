package com.kce.ump.dto.request;

import lombok.Data;

@Data
public class SignUpRequest {
    private String email;
    private String name;
    private String regnum;
    private String department;
}
