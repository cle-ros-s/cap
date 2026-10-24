package com.govscheme.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileDto {

    private Long id;
    private Long userId;
    private String name;
    private String email;
    private String mobile;
    private LocalDate dateOfBirth;
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
    private String aadhaarNumber;
    private Boolean aadhaarVerified;
    private Integer profileCompletionPercentage;
}
