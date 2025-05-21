package com.kce.ump.controller;

import com.kce.ump.dto.request.*;
import com.kce.ump.dto.response.JwtAuthResponse;
import com.kce.ump.service.AuthenticationService;
import com.kce.ump.service.JWTService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final JWTService jwtService;


    @PostMapping("/signup")
    public ResponseEntity<JwtAuthResponse> signup(@RequestBody SignUpRequest signUpRequest) {
        return ResponseEntity.ok(authenticationService.signUp(signUpRequest));
    }

    @PostMapping("/faculty/signup")
    public ResponseEntity<Boolean> facultySignup(@RequestBody SignUpRequest signUpRequest) {
        return ResponseEntity.ok(authenticationService.signUpFaculty(signUpRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<JwtAuthResponse> signIn(@RequestBody SignInRequest signInRequest) {
        System.out.println("signing in user: "+signInRequest.toString());
        return ResponseEntity.ok(authenticationService.signIn(signInRequest));
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtAuthResponse> refresh(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        //TODO: update to fetch from requestHeader
        return ResponseEntity.ok(authenticationService.refreshToken(refreshTokenRequest));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String token) {
        authenticationService.logout(token);
        return ResponseEntity.ok("Logout successful");
    }

    @PostMapping("/updatePassword")
    public ResponseEntity<JwtAuthResponse> updatePassword(@RequestHeader("Authorization") String token, @RequestBody UpdatePasswordRequest updatePasswordRequest) {
        //TODO: after integrating to frontend
        System.out.println("updating user: "+updatePasswordRequest.toString());
        String jwtToken = token.substring(7);
        return ResponseEntity.ok(authenticationService.updatePassword(jwtToken, updatePasswordRequest));
    }

    @GetMapping("/verify")
    public ResponseEntity<Boolean> verify(@RequestParam("token") String token) {
        boolean isValid = authenticationService.verify(token);
        String email = jwtService.extractUsername(token);
        try{
            URI redirectUri = isValid
                    ? URI.create("http://localhost:3000/changePassword?email="+ URLEncoder.encode(email, StandardCharsets.UTF_8)) // Your frontend success page
                    : URI.create("http://localhost:3000/error");
            return ResponseEntity.status(HttpStatus.SEE_OTHER)
                    .location(redirectUri)
                    .build();
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }

    @PostMapping("/forgotPassword")
    public ResponseEntity<Boolean> forgotPassword(@RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        return ResponseEntity.ok(authenticationService.forgotPassword(forgotPasswordRequest.getEmail()));
    }

    @GetMapping
    public ResponseEntity<JwtAuthResponse> currentUser(@RequestHeader("Authorization") String token) {
        System.out.println("getting current user");
        String jwtToken = token.substring(7);
        JwtAuthResponse jwtAuthResponse = authenticationService.currentUser(jwtToken);
        return ResponseEntity.ok(jwtAuthResponse);
    }
}
