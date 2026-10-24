package com.govscheme.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserSummaryDto {
    private Long id;
    private String name;
    private String email;
    private String mobile;
    private String role;
    private String status;
    private String aadhaarNumber;
    private Boolean aadhaarVerified;
    private LocalDateTime createdAt;
}
