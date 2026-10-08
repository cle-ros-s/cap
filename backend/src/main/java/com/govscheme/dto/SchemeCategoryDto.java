package com.govscheme.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SchemeCategoryDto {

    private Long id;

    @NotBlank(message = "Category name is required")
    private String name;

    private String description;
    private String iconName;
    private String status;
    private Long schemeCount;
}
