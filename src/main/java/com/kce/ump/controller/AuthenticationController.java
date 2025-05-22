package com.kce.ump.controller;

import com.kce.ump.dto.request.*;
import com.kce.ump.dto.response.JwtAuthResponse;
import com.kce.ump.model.user.Role;
import com.kce.ump.service.AuthenticationService;
import com.kce.ump.service.JWTService;
import com.opencsv.CSVReader;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final JWTService jwtService;


    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestParam("for") String role, @RequestParam("file")MultipartFile file){
        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            CSVReader csvReader = new CSVReader(reader);
            List<String[]> records = csvReader.readAll();
            records.remove(0);
            Role userRole = Role.valueOf(role.toUpperCase());
            List<String> users = new ArrayList<>();

            for (String[] record : records) {
                String regNum = record[0];
                String name = record[1];
                String email = record[2];
                String department = record[3];

                if(!authenticationService.signUp(regNum,name,email,department, userRole)){
                    users.add(regNum);
                }
            }
            if(users.isEmpty()){
                return ResponseEntity.ok("All users added successfully");
            }

            return ResponseEntity.ok("Users with regNum: " + users + " already exist. Please check the file and try again. Other users Added");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing file: " + e.getMessage());
        }
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
