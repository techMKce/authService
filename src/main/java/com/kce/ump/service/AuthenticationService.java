package com.kce.ump.service;

import com.kce.ump.dto.request.RefreshTokenRequest;
import com.kce.ump.dto.request.SignInRequest;
import com.kce.ump.dto.request.SignUpRequest;
import com.kce.ump.dto.request.UpdatePasswordRequest;
import com.kce.ump.dto.response.JwtAuthResponse;
import lombok.NonNull;

public interface AuthenticationService {
    JwtAuthResponse signUp(@NonNull SignUpRequest signUpRequest);

    JwtAuthResponse signIn(@NonNull SignInRequest signInRequest);

    JwtAuthResponse refreshToken(@NonNull RefreshTokenRequest refreshTokenRequest);

    JwtAuthResponse currentUser(@NonNull String token);

    JwtAuthResponse updatePassword(@NonNull String token, @NonNull UpdatePasswordRequest updatePasswordRequest);

    boolean updateUser(@NonNull Long id, @NonNull String name, @NonNull String profileImage);

    void logout(@NonNull String token);
}
