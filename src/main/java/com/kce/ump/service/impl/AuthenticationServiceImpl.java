package com.kce.ump.service.impl;

import com.kce.ump.dto.Mapper.UserMapper;
import com.kce.ump.dto.request.RefreshTokenRequest;
import com.kce.ump.dto.request.SignInRequest;
import com.kce.ump.dto.request.SignUpRequest;
import com.kce.ump.dto.request.UpdatePasswordRequest;
import com.kce.ump.dto.response.JwtAuthResponse;
import com.kce.ump.model.auth.RefreshToken;
import com.kce.ump.model.user.Role;
import com.kce.ump.model.user.User;
import com.kce.ump.repository.RefreshTokenRepository;
import com.kce.ump.repository.UserRepository;
import com.kce.ump.service.AuthenticationService;
import com.kce.ump.service.JWTService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public JwtAuthResponse signUp(@NonNull SignUpRequest signUpRequest) {

        User dbUser = userRepository.findByEmail(signUpRequest.getEmail()).orElse(null);
        if(dbUser != null){
            throw new IllegalArgumentException("User already exists");
        }else{
            User user = new User();
            user.setName(signUpRequest.getName());
            user.setEmail(signUpRequest.getEmail());
            user.setRole(Role.STUDENT);
            user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
            user.setCreatedAt(LocalDate.now());
            user.setUpdatedAt(LocalDate.now());
            userRepository.save(user);
            String jwtToken = jwtService.generateToken(user);
            String refreshToken = jwtService.generateRefreshToken(new HashMap<>(), user);
            refreshTokenRepository.save(new RefreshToken(refreshToken, user));
            JwtAuthResponse jwtAuthResponse = new JwtAuthResponse();
            jwtAuthResponse.setToken(jwtToken);
            jwtAuthResponse.setRefreshToken(refreshToken);
            jwtAuthResponse.setProfile(UserMapper.toProfile(user));
            return jwtAuthResponse;
        }
    }

    @Override
    public JwtAuthResponse signIn(@NonNull SignInRequest signInRequest){
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(signInRequest.getEmail(), signInRequest.getPassword()));
        User user = userRepository.findByEmail(signInRequest.getEmail()).orElseThrow(() -> new IllegalArgumentException("User not found"));
        String jwtToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(new HashMap<>(), user);
        refreshTokenRepository.save(new RefreshToken(refreshToken, user));
        JwtAuthResponse jwtAuthResponse = new JwtAuthResponse();
        jwtAuthResponse.setToken(jwtToken);
        jwtAuthResponse.setRefreshToken(refreshToken);
        jwtAuthResponse.setProfile(UserMapper.toProfile(user));
        return jwtAuthResponse;
    }


    @Override
    public JwtAuthResponse refreshToken(@NonNull RefreshTokenRequest refreshTokenRequest){
        String email = jwtService.extractUsername(refreshTokenRequest.getToken());
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenRequest.getToken());
        User user = userRepository.findByEmail(email).orElseThrow();
        if(refreshToken != null && refreshToken.getUser().getEmail().equals(email)){
            String jwtToken = jwtService.generateToken(user);
            JwtAuthResponse jwtAuthResponse = new JwtAuthResponse();
            jwtAuthResponse.setToken(jwtToken);
            jwtAuthResponse.setRefreshToken(refreshTokenRequest.getToken());
            return jwtAuthResponse;
        }
        return null;
    }



    @Override
    public JwtAuthResponse currentUser(@NonNull String token) {
        System.out.println("getting current user+ "+token);
        String email = jwtService.extractUsername(token);
        User user = userRepository.findByEmail(email).orElseThrow();
        String refreshToken = jwtService.generateRefreshToken(new HashMap<>(), user);
        JwtAuthResponse jwtAuthResponse = new JwtAuthResponse();
        jwtAuthResponse.setToken(token);
        jwtAuthResponse.setRefreshToken(refreshToken);
        jwtAuthResponse.setProfile(UserMapper.toProfile(user));
        return jwtAuthResponse;
    }

    @Transactional
    @Override
    public JwtAuthResponse updatePassword(@NonNull String token, @NonNull UpdatePasswordRequest updatePasswordRequest) {
        System.out.println("updating password"+token);
        String email = jwtService.extractUsername(token);
        User user = userRepository.findByEmail(email).orElse(null);
        if(user == null) {
            return null;
        }
        System.out.println(user.toString());
        String oldPassword = passwordEncoder.encode(updatePasswordRequest.getOldPassword());
        if(user.getPassword().equals(oldPassword)) {
            throw new IllegalArgumentException("Old password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(updatePasswordRequest.getNewPassword()));
        user.setUpdatedAt(LocalDate.now());
        userRepository.save(user);
        String jwtToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(new HashMap<>(), user);
        JwtAuthResponse jwtAuthResponse = new JwtAuthResponse();
        jwtAuthResponse.setToken(jwtToken);
        jwtAuthResponse.setRefreshToken(refreshToken);
        jwtAuthResponse.setProfile(UserMapper.toProfile(user));
        return jwtAuthResponse;
    }

    @Transactional
    @Override
    public boolean updateUser(@NonNull Long id, @NonNull String name, @NonNull String contactNumber) {
        User user = userRepository.findById(id).orElse(null);
        if(user == null) {
            return false;
        }
        user.setName(name);
        user.setContact(contactNumber);
        user.setUpdatedAt(LocalDate.now());
        userRepository.save(user);
        return true;
    }

    @Transactional
    @Override
    public void logout(@NonNull String token) {
        refreshTokenRepository.deleteByToken(token);
        System.out.println("Logout successful for token: " + token);
    }
}
