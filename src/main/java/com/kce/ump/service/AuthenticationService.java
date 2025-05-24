package com.kce.ump.service;

import com.kce.ump.dto.request.RefreshTokenRequest;
import com.kce.ump.dto.request.SignInRequest;
import com.kce.ump.dto.request.UpdatePasswordRequest;
import com.kce.ump.dto.response.JwtAuthResponse;
import com.kce.ump.dto.response.UserDto;
import com.kce.ump.model.user.Role;
import com.kce.ump.model.user.User;
import lombok.NonNull;

import java.util.List;

public interface AuthenticationService {

    boolean signUp(String regNum, String name, String email, String department, Role role);

    JwtAuthResponse signIn(@NonNull SignInRequest signInRequest);

    JwtAuthResponse refreshToken(@NonNull RefreshTokenRequest refreshTokenRequest);

    boolean verify(@NonNull String token);

    JwtAuthResponse currentUser(@NonNull String token);

    JwtAuthResponse updatePassword(@NonNull String token, @NonNull UpdatePasswordRequest updatePasswordRequest);

    boolean forgotPassword(@NonNull String email);

    void logout(@NonNull String token);

    List<User> getAllStudents();
    List<User> getAllFaculty();

}
