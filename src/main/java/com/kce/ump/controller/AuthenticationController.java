package com.kce.ump.controller;

import com.kce.ump.dto.request.*;
import com.kce.ump.dto.response.JwtAuthResponse;
import com.kce.ump.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;


    @PostMapping("/signup")
    public ResponseEntity<JwtAuthResponse> signup(@RequestBody SignUpRequest signUpRequest) {
        return ResponseEntity.ok(authenticationService.signUp(signUpRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<JwtAuthResponse> signIn(@RequestBody SignInRequest signInRequest) {
        System.out.println("signing in user: "+signInRequest.toString());
        return ResponseEntity.ok(authenticationService.signIn(signInRequest));
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtAuthResponse> refresh(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        return ResponseEntity.ok(authenticationService.refreshToken(refreshTokenRequest));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String token) {
        authenticationService.logout(token);
        return ResponseEntity.ok("Logout successful");
    }

    @PostMapping("/updatePassword")
    public ResponseEntity<JwtAuthResponse> updatePassword(@RequestHeader("Authorization") String token, @RequestBody UpdatePasswordRequest updatePasswordRequest) {
        System.out.println("updating user: "+updatePasswordRequest.toString());
        String jwtToken = token.substring(7);
        return ResponseEntity.ok(authenticationService.updatePassword(jwtToken, updatePasswordRequest));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Boolean> updateUser(@PathVariable Long id, @RequestBody ProfileUpdateRequest profileUpdateRequest) {
        System.out.println("updating user"+profileUpdateRequest.getContact());
        return ResponseEntity.ok(authenticationService.updateUser(id, profileUpdateRequest.getName().replace("\"",""), profileUpdateRequest.getContact().replace("\"","")));
    }

    @GetMapping
    public ResponseEntity<JwtAuthResponse> currentUser(@RequestHeader("Authorization") String token) {
        System.out.println("getting current user");
        String jwtToken = token.substring(7);
        JwtAuthResponse jwtAuthResponse = authenticationService.currentUser(jwtToken);
        return ResponseEntity.ok(jwtAuthResponse);
    }
}
