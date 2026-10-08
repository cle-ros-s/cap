package com.govscheme.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminDashboardStatsDto {

    private long totalUsers;
    private long activeUsers;
    private long totalSchemes;
    private long activeSchemes;
    private long totalCategories;
    private long totalEligibilityRules;
    private long totalSavedSchemesCount;
}
