package com.kce.ump.dto.Mapper;

import com.kce.ump.model.user.Profile;
import com.kce.ump.model.user.User;
import com.kce.ump.dto.response.UserDto;

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

    public static UserDto toUserDto(User user) {
        UserDto dto = new UserDto();
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRegNum(user.getRegNum());
        dto.setDepartment(user.getDepartment());
        return dto;
    }
}
