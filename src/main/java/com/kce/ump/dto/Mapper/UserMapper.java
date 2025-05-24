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
                .year(user.getYear())
                .department(user.getDepartment())
                .role(user.getRole())
                .build();
    }

    public static UserDto toUserDto(User user) {
        UserDto dto = new UserDto();
        dto.setName(user.getName());
        dto.setRegNum(user.getRegNum());
        dto.setEmail(user.getEmail());
        dto.setYear(user.getYear());
        dto.setDepartment(user.getDepartment());
        return dto;
    }
}
