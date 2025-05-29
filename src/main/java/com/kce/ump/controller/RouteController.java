package com.kce.ump.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@RestController
@RequestMapping("/api/v1")
public class RouteController {
    @Value("${routes.profile-service}")
    private String profileService;
    @Value("${routes.course-service}")
    private String courseService;
    @Value("${routes.attendance-service}")
    private String attendanceService;
    @Value("${routes.assignment-service}")
    private String assignmentService;


    private final RestTemplate restTemplate = new RestTemplate();

    @RequestMapping("/profile/**")
    public ResponseEntity<?> profileProxy(HttpServletRequest request, @RequestBody(required = false) String body) {
        return proxyRequest(request, body, profileService);
    }

    @RequestMapping("/course/**")
    public ResponseEntity<?> courseProxy(HttpServletRequest request, @RequestBody(required = false) String body) {
        System.out.println("Course Proxy Called");
        return proxyRequest(request, body, courseService);
    }

    @RequestMapping("/attendance/**")
    public ResponseEntity<?> attendanceProxy(HttpServletRequest request, @RequestBody(required = false) String body) {
        return proxyRequest(request, body, attendanceService);
    }

    @RequestMapping("/assignments/**")
    public ResponseEntity<?> assignmentProxy(HttpServletRequest request, @RequestBody(required = false) String body) {
        return proxyRequest(request, body, assignmentService);
    }

    @RequestMapping("/gradings/**")
    public ResponseEntity<?> gradingProxy(HttpServletRequest request, @RequestBody(required = false) String body) {
        return proxyRequest(request, body, assignmentService);
    }

    @RequestMapping("/submissions/**")
    public ResponseEntity<?> submissionProxy(HttpServletRequest request, @RequestBody(required = false) String body) {
        return proxyRequest(request, body, assignmentService);
    }

    private ResponseEntity<?> proxyRequest(HttpServletRequest request, String body, String serviceUrl) {
        try{
            String path = request.getRequestURI();
            String query = request.getQueryString();
            System.out.println(query);
            String url = serviceUrl + path + (query != null ? "?" + query : "");
            System.out.println("url: " + url);

            HttpHeaders headers = new HttpHeaders();
            Collections.list(request.getHeaderNames()).forEach(
                    h -> headers.set(h, request.getHeader(h))
            );
            HttpMethod method = HttpMethod.valueOf(request.getMethod());
            HttpEntity<String> entity;

            if (method == HttpMethod.POST || method == HttpMethod.PUT || method == HttpMethod.PATCH) {
                entity = new HttpEntity<>(body, headers);
            } else {
                entity = new HttpEntity<>(headers);
            }

            return restTemplate.exchange(url, method, entity, String.class);
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Gateway Error: " + e.getMessage());
        }
    }
}
