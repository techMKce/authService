package com.kce.ump.model.user;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Profile {
    private String id;
    private String name;
    private String email;
    private String department;
    private Integer semester;
    private String year;
    private Role role;
}
