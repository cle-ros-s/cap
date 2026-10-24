package com.govscheme.service;

import com.govscheme.dto.AdminDashboardStatsDto;
import com.govscheme.dto.UserSummaryDto;
import com.govscheme.entity.Status;
import com.govscheme.entity.User;
import com.govscheme.exception.ResourceNotFoundException;
import com.govscheme.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SchemeRepository schemeRepository;

    @Autowired
    private SchemeCategoryRepository categoryRepository;

    @Autowired
    private EligibilityRuleRepository ruleRepository;

    @Autowired
    private SavedSchemeRepository savedSchemeRepository;

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardStatsDto getAdminDashboardStats() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.findAll().stream().filter(u -> u.getStatus() == Status.ACTIVE).count();
        long totalSchemes = schemeRepository.count();
        long activeSchemes = schemeRepository.findByStatus(Status.ACTIVE).size();
        long totalCategories = categoryRepository.count();
        long totalEligibilityRules = ruleRepository.count();
        long totalSavedSchemesCount = savedSchemeRepository.count();

        return AdminDashboardStatsDto.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .totalSchemes(totalSchemes)
                .activeSchemes(activeSchemes)
                .totalCategories(totalCategories)
                .totalEligibilityRules(totalEligibilityRules)
                .totalSavedSchemesCount(totalSavedSchemesCount)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSummaryDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> UserSummaryDto.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .email(user.getEmail())
                        .mobile(user.getMobile())
                        .aadhaarNumber(user.getAadhaarNumber())
                        .aadhaarVerified(user.getAadhaarVerified())
                        .role(user.getRole().name())
                        .status(user.getStatus().name())
                        .createdAt(user.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserSummaryDto toggleUserStatus(Long userId, String newStatus) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        user.setStatus(Status.valueOf(newStatus.toUpperCase()));
        user = userRepository.save(user);

        return UserSummaryDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .mobile(user.getMobile())
                .aadhaarNumber(user.getAadhaarNumber())
                .aadhaarVerified(user.getAadhaarVerified())
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
