package com.kce.ump.controller;

import com.kce.ump.dto.request.*;
import com.kce.ump.dto.response.JwtAuthResponse;
import com.kce.ump.model.user.Profile;
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


    @PostMapping("/signup/all")
    public ResponseEntity<?> bulkSignup(@RequestParam("for") String role, @RequestParam("file")MultipartFile file){
        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            CSVReader csvReader = new CSVReader(reader);
            List<String[]> records = csvReader.readAll();
            records.remove(0);
            Role userRole = Role.valueOf(role.toUpperCase());
            List<String> users = new ArrayList<>();

            for (String[] record : records) {
                String id = record[0];
                String name = record[1];
                String email = record[2];
                String department = record[3];
                String year = record.length > 4 ? record[4] : null; // Handle optional year field

                if(!authenticationService.signUp(id,name,email,department,year, userRole)){
                    users.add(id);
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

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestParam("for") String role, @RequestBody SignUpRequest signUpRequest) {
        System.out.println("signing up user: "+signUpRequest.toString());
        Role userRole = Role.valueOf(role.toUpperCase());
        if(userRole == Role.FACULTY){
            if(authenticationService.signUp(signUpRequest.getId(), signUpRequest.getName(), signUpRequest.getEmail(), signUpRequest.getDepartment(),null, userRole)){
                return ResponseEntity.ok("User registered successfully");
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("User already exists");
            }
        }else{
            if(authenticationService.signUp(signUpRequest.getId(), signUpRequest.getName(), signUpRequest.getEmail(), signUpRequest.getDepartment(),signUpRequest.getYear(), userRole)){
                return ResponseEntity.ok("User registered successfully");
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("User already exists");
            }
        }

    }

    @PostMapping("/login")
    public ResponseEntity<?> signIn(@RequestBody SignInRequest signInRequest) {
        System.out.println("signing in user: "+signInRequest.toString());
        try{
            JwtAuthResponse response = authenticationService.signIn(signInRequest);
            return ResponseEntity.ok(response);
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
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
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        try{
            boolean isValid = authenticationService.forgotPassword(forgotPasswordRequest.getEmail());
            if (!isValid) {
                throw new Exception("User not found or email not registered");
            }
            return ResponseEntity.ok(true);
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }



    @GetMapping("/students/all")
    public ResponseEntity<List<Profile>> getAllStudents() {
        return ResponseEntity.ok(authenticationService.getAllStudents());
    }

    @GetMapping("/faculty/all")
    public ResponseEntity<List<Profile>> getAllFaculty() {
        return ResponseEntity.ok(authenticationService.getAllFaculty());
    }

    @GetMapping
    public ResponseEntity<JwtAuthResponse> currentUser(@RequestHeader("Authorization") String token) {
        System.out.println("getting current user");
        String jwtToken = token.substring(7);
        JwtAuthResponse jwtAuthResponse = authenticationService.currentUser(jwtToken);
        return ResponseEntity.ok(jwtAuthResponse);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateUser(@PathVariable String id, @RequestBody Profile updatedProfile) {
        try {
            Profile updated = authenticationService.updateUser(id, updatedProfile);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable String id) {
        try {
            authenticationService.deleteUser(id);
            return ResponseEntity.ok("User deleted successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

}
