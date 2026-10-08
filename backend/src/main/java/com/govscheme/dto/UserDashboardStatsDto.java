package com.govscheme.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDashboardStatsDto {

    private String userName;
    private Integer profileCompletionPercentage;
    private long eligibleSchemesCount;
    private long savedSchemesCount;
    private UserProfileDto profile;
    private List<EligibilityEvaluationResult> topRecommendedSchemes;
    private List<SchemeDto> savedSchemes;
}
