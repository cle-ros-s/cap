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
public class EligibilityEvaluationResult {

    private SchemeDto scheme;
    private Boolean eligible; // true if all rules satisfied
    private Integer matchPercentage;
    private Integer satisfiedCriteriaCount;
    private Integer totalEvaluatedCriteriaCount;
    private List<RuleCriterionResult> criteriaBreakdown;
    private String summaryText;
}
