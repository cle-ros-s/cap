package com.govscheme.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RuleCriterionResult {

    private String criterionName;
    private String expectedValue;
    private String userValue;
    private Boolean satisfied;
    private String explanation;
}
