package com.kce.ump.service;

import com.kce.ump.dto.request.RefreshTokenRequest;
import com.kce.ump.dto.request.SignInRequest;
import com.kce.ump.dto.request.UpdatePasswordRequest;
import com.kce.ump.dto.response.JwtAuthResponse;
import com.kce.ump.model.user.Profile;
import com.kce.ump.model.user.Role;
import com.kce.ump.model.user.User;
import lombok.NonNull;

import java.util.List;

public interface AuthenticationService {


    boolean signUp(String id, String name, String email, String department, String year, Role role);

    JwtAuthResponse signIn(@NonNull SignInRequest signInRequest);

    JwtAuthResponse refreshToken(@NonNull RefreshTokenRequest refreshTokenRequest);

    boolean verify(@NonNull String token);

    JwtAuthResponse currentUser(@NonNull String token);

    boolean updatePassword(@NonNull UpdatePasswordRequest updatePasswordRequest);

    boolean forgotPassword(@NonNull String email);

    void logout(@NonNull String token);


    List<Profile> getAllStudents();

    List<Profile> getAllFaculty();

    User getUserById(String id);
}
