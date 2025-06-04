package com.kce.ump.dto.Mapper;

import com.kce.ump.model.user.Profile;
import com.kce.ump.model.user.User;

public class UserMapper {
    public static Profile toProfile(User user){
        return Profile.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .department(user.getDepartment())
                .semester(user.getSemester())
                .year(user.getYear())
                .role(user.getRole())
                .build();
    }
}
