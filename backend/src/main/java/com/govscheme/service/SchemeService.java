package com.govscheme.service;

import com.govscheme.dto.*;
import com.govscheme.entity.Scheme;

import java.util.List;

public interface SchemeService {
    List<SchemeDto> getAllActiveSchemes(String userEmail);
    List<SchemeDto> getAllActiveSchemes(String state, String userEmail);
    List<SchemeDto> getSchemesByState(String state, String userEmail);
    List<SchemeDto> getAllSchemesAdmin();
    SchemeDto getSchemeById(Long id, String userEmail);
    List<SchemeDto> getSchemesByCategory(Long categoryId, String userEmail);
    List<SchemeDto> getSchemesByCategory(Long categoryId, String state, String userEmail);
    List<SchemeDto> searchSchemes(String query, String userEmail);
    List<SchemeDto> searchSchemes(String query, String state, String userEmail);
    List<String> getAvailableStates();

    List<SchemeCategoryDto> getAllCategories();
    SchemeCategoryDto createCategory(SchemeCategoryDto categoryDto);
    SchemeCategoryDto updateCategory(Long id, SchemeCategoryDto categoryDto);
    void deleteCategory(Long id);

    SchemeDto createScheme(SchemeDto schemeDto);
    SchemeDto updateScheme(Long id, SchemeDto schemeDto);
    void deleteScheme(Long id);

    EligibilityRuleDto saveOrUpdateEligibilityRule(Long schemeId, EligibilityRuleDto ruleDto);
    RequiredDocumentDto addRequiredDocument(Long schemeId, RequiredDocumentDto documentDto);
    void deleteRequiredDocument(Long documentId);

    ApplicationProcedureDto addApplicationProcedure(Long schemeId, ApplicationProcedureDto procedureDto);
    void deleteApplicationProcedure(Long procedureId);

    SchemeDto mapToSchemeDto(Scheme scheme, Long userId);
}
