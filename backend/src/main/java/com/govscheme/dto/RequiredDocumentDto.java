package com.govscheme.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequiredDocumentDto {
    private Long id;
    private Long schemeId;
    private String documentName;
    private String description;
    private Boolean mandatory;
}
