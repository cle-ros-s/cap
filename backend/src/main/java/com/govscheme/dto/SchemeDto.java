package com.govscheme.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SchemeDto {

    private Long id;
    private Long categoryId;
    private String categoryName;
    private String schemeName;
    private String shortDescription;
    private String description;
    private String department;
    private String schemeType;
    private String state;
    private String benefits;
    private String applicationUrl;
    private String contactInformation;
    private String status;
    private EligibilityRuleDto eligibilityRule;
    private List<RequiredDocumentDto> requiredDocuments;
    private List<ApplicationProcedureDto> applicationProcedures;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isSaved;
}
