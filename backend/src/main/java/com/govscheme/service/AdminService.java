package com.govscheme.service;

import com.govscheme.dto.AdminDashboardStatsDto;
import com.govscheme.dto.UserSummaryDto;

import java.util.List;

public interface AdminService {
    AdminDashboardStatsDto getAdminDashboardStats();
    List<UserSummaryDto> getAllUsers();
    UserSummaryDto toggleUserStatus(Long userId, String newStatus);
}
