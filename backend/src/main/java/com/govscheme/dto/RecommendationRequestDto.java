package com.govscheme.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecommendationRequestDto {
    private Integer age;
    private String gender;
    private String state;
    private String district;
    private Double annualIncome;
    private String occupation;
    private String employmentStatus;
    private String category;
    private Boolean studentStatus;
    private Boolean disabilityStatus;
    private Boolean farmerStatus;
    private Boolean entrepreneurStatus;
}
