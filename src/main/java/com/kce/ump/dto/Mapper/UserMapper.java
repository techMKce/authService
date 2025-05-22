package com.kce.ump.dto.Mapper;

import com.kce.ump.model.user.Profile;
import com.kce.ump.model.user.User;

public class UserMapper {
    public static Profile toProfile(User user){
        return Profile.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .regNum(user.getRegNum())
                .department(user.getDepartment())
                .role(user.getRole())
                .build();
    }
}
