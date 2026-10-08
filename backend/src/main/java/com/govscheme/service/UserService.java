package com.govscheme.service;

import com.govscheme.dto.*;

public interface UserService {
    AuthResponse registerUser(RegisterRequest request);
    AuthResponse loginUser(LoginRequest request);
    UserProfileDto getUserProfile(String email);
    UserProfileDto updateUserProfile(String email, UserProfileDto profileDto);
    UserSummaryDto getCurrentUserSummary(String email);
    UserDashboardStatsDto getUserDashboardData(String email);
}
