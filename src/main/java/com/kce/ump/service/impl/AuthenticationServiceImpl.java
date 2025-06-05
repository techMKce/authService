package com.kce.ump.service.impl;

import com.kce.ump.dto.Mapper.UserMapper;
import com.kce.ump.dto.request.RefreshTokenRequest;
import com.kce.ump.dto.request.SignInRequest;
import com.kce.ump.dto.request.UpdatePasswordRequest;
import com.kce.ump.dto.response.JwtAuthResponse;
import com.kce.ump.emailContext.AccountVerificationEmailContext;
import com.kce.ump.model.auth.RefreshToken;
import com.kce.ump.model.user.Profile;
import com.kce.ump.model.user.Role;
import com.kce.ump.model.user.User;
import com.kce.ump.repository.RefreshTokenRepository;
import com.kce.ump.repository.UserRepository;
import com.kce.ump.service.AuthenticationService;
import com.kce.ump.service.EmailService;
import com.kce.ump.service.JWTService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;



@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailService emailService;

    private final RestTemplate restTemplate = new RestTemplate();


    @Override
    public boolean signUp(String id, String name, String email, String department, String year, Role role ,Integer semester) {

        User dbUser = userRepository.findByEmail(email).orElse(null);
        if(dbUser != null){
            throw new IllegalArgumentException("User already exists");
        }else{
            User user = new User();
            user.setId(id);
            user.setName(name);
            user.setEmail(email);
            user.setRole(role);
            if(role == Role.STUDENT) {
                user.setYear(year);
                user.setSemester(semester);
            }
            int password = 100000 + new java.util.Random().nextInt(900000);
            String passwordStr = String.valueOf(password);
            emailService.welcomeMail(email,passwordStr);
            user.setPassword(passwordEncoder.encode(passwordStr));
            user.setDepartment(department);
            user.setCreatedAt(LocalDate.now());
            user.setUpdatedAt(LocalDate.now());
            userRepository.save(user);
            return true;
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
    public boolean verify(@NonNull String token) {
        String email = jwtService.extractUsername(token);
        User user = userRepository.findByEmail(email).orElse(null);
        if(user == null){
            return false;
        }
        return jwtService.isTokenValid(token, user);
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
    public boolean updatePassword(@NonNull UpdatePasswordRequest updatePasswordRequest) {
        User user = userRepository.findByEmail(updatePasswordRequest.getEmail()).orElse(null);
        if(user == null) {
            return false;
        }
        System.out.println(user.toString());
        user.setPassword(passwordEncoder.encode(updatePasswordRequest.getNewPassword()));
        user.setUpdatedAt(LocalDate.now());
        userRepository.save(user);
        return true;
    }

    @Override
    public boolean forgotPassword(@NonNull String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return false;
        }
        String resetToken = jwtService.generateResetToken(new HashMap<>(), user);
        AccountVerificationEmailContext context = new AccountVerificationEmailContext();
        context.init(user);
        context.setToken(resetToken);
        context.buildVerificationUrl("http://localhost:8080");
        emailService.sendEmail(context);
        System.out.println("Forgot password email sent to: " + email);
        return true;
    }

    @Override
    public List<Profile> getAllStudents() {
        List<User> students = userRepository.findAllByRole(Role.STUDENT);
        return students.stream()
                .map(UserMapper::toProfile)
                .collect(Collectors.toList());
    }

    @Override
    public List<Profile> getAllFaculty() {
        List<User> faculty = userRepository.findAllByRole(Role.FACULTY);
        return faculty.stream()
                .map(UserMapper::toProfile)
                .collect(Collectors.toList());
    }


    @Transactional
    @Override
    public void logout(@NonNull String token) {
        refreshTokenRepository.deleteByToken(token);
        System.out.println("Logout successful for token: " + token);
    }

    @Override
    public Profile updateUser(@NonNull String id, @NonNull Profile updatedProfile) {
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setName(updatedProfile.getName());
        user.setEmail(updatedProfile.getEmail());
        user.setDepartment(updatedProfile.getDepartment());
        user.setSemester(updatedProfile.getSemester());
        user.setYear(updatedProfile.getYear());
        user.setUpdatedAt(LocalDate.now());

        userRepository.save(user);
        return UserMapper.toProfile(user);
    }

    @Override
    public void deleteUser(@NonNull String id) {
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        if(user != null){
            List<RefreshToken> token = refreshTokenRepository.findByUserId(user.getId());
            for(RefreshToken rt : token) {
                refreshTokenRepository.delete(rt);
            }
            if(user.getRole()==Role.STUDENT) {
                String url = "http://localhost:8082/api/v1/profile/admin/student/delete/" + id;
                String attendanceServiceUrl = "http://localhost:8084/api/v1/attendance/deletestudentid?studentid=" +id;
                try {
                    restTemplate.delete(url);
                    restTemplate.delete(attendanceServiceUrl);
                } catch (Exception e) {
                    System.out.println("Failed to delete user in profile service: " + e.getMessage());
                }
                userRepository.delete(user);
            }
            if(user.getRole()==Role.FACULTY) {
                String url = "http://localhost:8082/api/v1/profile/admin/faculty/delete/" + id;
                try {
                    restTemplate.delete(url);
                } catch (Exception e) {
                    System.out.println("Failed to delete the User " + e.getMessage());
                }
                userRepository.delete(user);
            }
        }
    }


    @Override
    public User getUserById(String id) {
        return userRepository.findById(id).orElse(null);
    }
}
