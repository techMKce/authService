package com.kce.ump.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public ResponseEntity<?> profileProxy(HttpServletRequest request) {
        return proxyRequest(request, profileService);
    }

    @RequestMapping("/course-enrollment/**")
    public ResponseEntity<?> courseEnrollmentProxy(HttpServletRequest request) {
        System.out.println("Course Enrollment Proxy Called");
        return proxyRequest(request, profileService);
    }

    @RequestMapping("/faculty-student-assigning/admin/**")
    public ResponseEntity<?> facultyStudentAssignmentProxy(HttpServletRequest request) {
        System.out.println("Faculty Student Assignment Proxy Called");
        return proxyRequest(request, profileService);
    }

    @RequestMapping("/course/**")
    public ResponseEntity<?> courseProxy(HttpServletRequest request) {
        System.out.println("Course Proxy Called");
        return proxyRequest(request, courseService);
    }

    @RequestMapping("/attendance/**")
    public ResponseEntity<?> attendanceProxy(HttpServletRequest request) {
//        System.out.println(request);
        return proxyRequest(request, attendanceService);
    }

    @RequestMapping("/assignments/**")
    public ResponseEntity<?> assignmentProxy(HttpServletRequest request) {
        return proxyRequest(request, assignmentService);
    }

    @RequestMapping("/gradings/**")
    public ResponseEntity<?> gradingProxy(HttpServletRequest request) {
        return proxyRequest(request, assignmentService);
    }

    @RequestMapping("/submissions/**")
    public ResponseEntity<?> submissionsProxy(HttpServletRequest request) {
        return proxyRequest(request, assignmentService);
    }
    @RequestMapping("/todos/**")
    public ResponseEntity<?> todosProxy(HttpServletRequest request) {
        return proxyRequest(request, assignmentService);
    }

    @RequestMapping("/submissions")
    public ResponseEntity<?> submissionProxy(HttpServletRequest request) {
        return proxyRequest(request, assignmentService);
    }

    private ResponseEntity<?> proxyRequest(HttpServletRequest request, String serviceUrl) {
        try {
            String path = request.getRequestURI();
            String query = request.getQueryString();
            String url = serviceUrl + path + (query != null ? "?" + query : "");

            HttpHeaders headers = new HttpHeaders();
            HttpServletRequest finalRequest = request;
            Collections.list(request.getHeaderNames()).forEach(
                    h -> {
                        // Skip content-length header to avoid mismatch issues
                        if (!"content-length".equalsIgnoreCase(h)) {
                            headers.set(h, finalRequest.getHeader(h));
                        }
                    }
            );

            HttpMethod method = HttpMethod.valueOf(request.getMethod());

            // Handle multipart/form-data
            if (headers.getContentType() != null && headers.getContentType().toString().startsWith("multipart/")) {
                return handleMultipartRequest(request, url, method, headers);
            }

            // Non-multipart: read as string
            String body = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
            HttpEntity<String> requestEntity = new HttpEntity<>(body, headers);
            return restTemplate.exchange(url, method, requestEntity, String.class);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Gateway Error: " + e.getMessage());
        }
    }

    private ResponseEntity<?> handleMultipartRequest(HttpServletRequest request, String url,
                                                     HttpMethod method, HttpHeaders headers) throws IOException {
        // Parse multipart content
        if (!(request instanceof MultipartHttpServletRequest)) {
            request = new StandardServletMultipartResolver().resolveMultipart(request);
        }

        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        MultiValueMap<String, Object> multipartMap = new LinkedMultiValueMap<>();

        // Add form fields
        for (Map.Entry<String, String[]> entry : multipartRequest.getParameterMap().entrySet()) {
            for (String value : entry.getValue()) {
                multipartMap.add(entry.getKey(), value);
            }
        }

        // Add files with proper resource handling
        for (Map.Entry<String, List<MultipartFile>> entry : multipartRequest.getMultiFileMap().entrySet()) {
            for (MultipartFile file : entry.getValue()) {
                if (!file.isEmpty()) {
                    // Create ByteArrayResource to avoid stream issues
                    ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
                        @Override
                        public String getFilename() {
                            return file.getOriginalFilename();
                        }
                    };

                    HttpHeaders fileHeaders = new HttpHeaders();
                    if (file.getContentType() != null) {
                        fileHeaders.setContentType(MediaType.parseMediaType(file.getContentType()));
                    }
                    fileHeaders.setContentLength(file.getBytes().length);

                    HttpEntity<Resource> fileEntity = new HttpEntity<>(fileResource, fileHeaders);
                    multipartMap.add(entry.getKey(), fileEntity);
                }
            }
        }

        // Set content type without boundary (let RestTemplate handle it)
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(multipartMap, headers);

        return restTemplate.exchange(url, method, requestEntity, String.class);
    }
}