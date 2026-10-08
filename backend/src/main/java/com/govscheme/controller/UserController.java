package com.govscheme.controller;

import com.govscheme.dto.UserDashboardStatsDto;
import com.govscheme.dto.UserProfileDto;
import com.govscheme.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<UserProfileDto> getProfile(Authentication authentication) {
        return ResponseEntity.ok(userService.getUserProfile(authentication.getName()));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileDto> updateProfile(Authentication authentication, @RequestBody UserProfileDto profileDto) {
        return ResponseEntity.ok(userService.updateUserProfile(authentication.getName(), profileDto));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<UserDashboardStatsDto> getDashboardData(Authentication authentication) {
        return ResponseEntity.ok(userService.getUserDashboardData(authentication.getName()));
    }
}
