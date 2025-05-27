package com.kce.ump.dto.request;

import lombok.Data;

@Data
public class SignUpRequest {
    private String id;
    private String email;
    private String name;
    private String department;
    private String year;
}
