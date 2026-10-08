package com.govscheme.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EligibilityRuleDto {

    private Long id;
    private Long schemeId;
    private Integer minAge;
    private Integer maxAge;
    private Double minIncome;
    private Double maxIncome;
    private String gender;
    private String state;
    private String district;
    private String category;
    private String occupation;
    private String employmentStatus;
    private Boolean studentStatus;
    private Boolean disabilityStatus;
    private Boolean farmerStatus;
    private Boolean entrepreneurStatus;
}
