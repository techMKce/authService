package com.kce.ump.model.user;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Profile {
    private Long id;
    private String name;
    private String email;
    private String regNum;
    private String year;
    private String department;
    private Role role;
}
