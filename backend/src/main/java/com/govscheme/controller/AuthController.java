package com.govscheme.controller;

import com.govscheme.dto.AuthResponse;
import com.govscheme.dto.LoginRequest;
import com.govscheme.dto.RegisterRequest;
import com.govscheme.dto.UserSummaryDto;
import com.govscheme.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private com.govscheme.service.OtpService otpService;

    @PostMapping("/otp/send")
    public ResponseEntity<com.govscheme.dto.OtpResponse> sendOtp(@Valid @RequestBody com.govscheme.dto.OtpSendRequest request) {
        return ResponseEntity.ok(otpService.sendAadhaarOtp(request));
    }

    @PostMapping("/otp/verify")
    public ResponseEntity<com.govscheme.dto.OtpVerifyResponse> verifyOtp(@Valid @RequestBody com.govscheme.dto.OtpVerifyRequest request) {
        return ResponseEntity.ok(otpService.verifyAadhaarOtp(request));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(userService.registerUser(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.loginUser(request));
    }

    @GetMapping("/security-status")
    public ResponseEntity<java.util.Map<String, Object>> getSecurityStatus(jakarta.servlet.http.HttpServletRequest request) {
        java.util.Map<String, Object> status = new java.util.LinkedHashMap<>();
        status.put("status", "UP");
        status.put("passwordHashing", "BCryptPasswordEncoder (Spring Security)");
        status.put("passwordAlgorithm", "BCrypt (10 Salt Rounds)");
        status.put("plainTextPasswordStorage", false);
        
        boolean isHttps = request.isSecure() || "https".equalsIgnoreCase(request.getHeader("X-Forwarded-Proto"));
        status.put("isHttpsConnection", isHttps);
        status.put("transportProtocol", isHttps ? "HTTPS (TLS Encrypted)" : "HTTP (Local Development)");
        status.put("liveHttpsPolicy", "ENFORCED - Live backend connections require HTTPS for credential protection");
        status.put("requestScheme", request.getScheme());
        status.put("serverPort", request.getServerPort());
        return ResponseEntity.ok(status);
    }

    @GetMapping("/me")
    public ResponseEntity<UserSummaryDto> getCurrentUser(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(userService.getCurrentUserSummary(authentication.getName()));
    }
}
