package com.govscheme.service;

import com.govscheme.dto.*;
import com.govscheme.entity.*;
import com.govscheme.exception.BadRequestException;
import com.govscheme.exception.ResourceNotFoundException;
import com.govscheme.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SchemeServiceImpl implements SchemeService {

    @Autowired
    private SchemeRepository schemeRepository;

    @Autowired
    private SchemeCategoryRepository categoryRepository;

    @Autowired
    private EligibilityRuleRepository ruleRepository;

    @Autowired
    private RequiredDocumentRepository documentRepository;

    @Autowired
    private ApplicationProcedureRepository procedureRepository;

    @Autowired
    private SavedSchemeRepository savedSchemeRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SchemeDto> getAllActiveSchemes(String userEmail) {
        return getAllActiveSchemes(null, userEmail);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SchemeDto> getAllActiveSchemes(String state, String userEmail) {
        Long userId = getUserIdFromEmail(userEmail);
        return schemeRepository.findByStatus(Status.ACTIVE).stream()
                .filter(scheme -> matchesState(scheme, state))
                .map(scheme -> mapToSchemeDto(scheme, userId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SchemeDto> getSchemesByState(String state, String userEmail) {
        return getAllActiveSchemes(state, userEmail);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SchemeDto> getAllSchemesAdmin() {
        return schemeRepository.findAll().stream()
                .map(scheme -> mapToSchemeDto(scheme, null))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SchemeDto getSchemeById(Long id, String userEmail) {
        Scheme scheme = schemeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Scheme not found with ID: " + id));
        Long userId = getUserIdFromEmail(userEmail);
        return mapToSchemeDto(scheme, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SchemeDto> getSchemesByCategory(Long categoryId, String userEmail) {
        return getSchemesByCategory(categoryId, null, userEmail);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SchemeDto> getSchemesByCategory(Long categoryId, String state, String userEmail) {
        Long userId = getUserIdFromEmail(userEmail);
        return schemeRepository.findByCategoryIdAndStatus(categoryId, Status.ACTIVE).stream()
                .filter(scheme -> matchesState(scheme, state))
                .map(scheme -> mapToSchemeDto(scheme, userId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SchemeDto> searchSchemes(String query, String userEmail) {
        return searchSchemes(query, null, userEmail);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SchemeDto> searchSchemes(String query, String state, String userEmail) {
        Long userId = getUserIdFromEmail(userEmail);
        if (query == null || query.trim().isEmpty()) {
            return getAllActiveSchemes(state, userEmail);
        }
        return schemeRepository.searchSchemes(query.trim(), Status.ACTIVE).stream()
                .filter(scheme -> matchesState(scheme, state))
                .map(scheme -> mapToSchemeDto(scheme, userId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAvailableStates() {
        return schemeRepository.findByStatus(Status.ACTIVE).stream()
                .map(s -> s.getEligibilityRule() != null ? s.getEligibilityRule().getState() : null)
                .filter(st -> st != null && !st.isBlank() && !"ALL".equalsIgnoreCase(st))
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    private boolean matchesState(Scheme scheme, String state) {
        if (state == null || state.isBlank() || "ALL".equalsIgnoreCase(state) || "All India".equalsIgnoreCase(state)) {
            return true;
        }
        EligibilityRule rule = scheme.getEligibilityRule();
        boolean isCentral = "CENTRAL".equalsIgnoreCase(scheme.getSchemeType()) ||
                rule == null ||
                rule.getState() == null ||
                "ALL".equalsIgnoreCase(rule.getState());
        boolean isMatchingState = rule != null && rule.getState() != null && rule.getState().equalsIgnoreCase(state.trim());
        return isCentral || isMatchingState;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SchemeCategoryDto> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(cat -> SchemeCategoryDto.builder()
                        .id(cat.getId())
                        .name(cat.getName())
                        .description(cat.getDescription())
                        .iconName(cat.getIconName())
                        .status(cat.getStatus().name())
                        .schemeCount((long) cat.getSchemes().size())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SchemeCategoryDto createCategory(SchemeCategoryDto categoryDto) {
        if (categoryRepository.findByName(categoryDto.getName()).isPresent()) {
            throw new BadRequestException("Category with this name already exists");
        }

        SchemeCategory category = SchemeCategory.builder()
                .name(categoryDto.getName())
                .description(categoryDto.getDescription())
                .iconName(categoryDto.getIconName() != null ? categoryDto.getIconName() : "bi-folder")
                .status(Status.ACTIVE)
                .build();

        category = categoryRepository.save(category);

        return SchemeCategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .iconName(category.getIconName())
                .status(category.getStatus().name())
                .schemeCount(0L)
                .build();
    }

    @Override
    @Transactional
    public SchemeCategoryDto updateCategory(Long id, SchemeCategoryDto categoryDto) {
        SchemeCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));

        category.setName(categoryDto.getName());
        category.setDescription(categoryDto.getDescription());
        if (categoryDto.getIconName() != null) category.setIconName(categoryDto.getIconName());
        if (categoryDto.getStatus() != null) category.setStatus(Status.valueOf(categoryDto.getStatus()));

        category = categoryRepository.save(category);

        return SchemeCategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .iconName(category.getIconName())
                .status(category.getStatus().name())
                .schemeCount((long) category.getSchemes().size())
                .build();
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        SchemeCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
        categoryRepository.delete(category);
    }

    @Override
    @Transactional
    public SchemeDto createScheme(SchemeDto schemeDto) {
        SchemeCategory category = categoryRepository.findById(schemeDto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + schemeDto.getCategoryId()));

        String schemeType = schemeDto.getSchemeType() != null ? schemeDto.getSchemeType().toUpperCase() : "CENTRAL";
        Scheme scheme = Scheme.builder()
                .category(category)
                .schemeName(schemeDto.getSchemeName())
                .shortDescription(schemeDto.getShortDescription())
                .description(schemeDto.getDescription() != null ? schemeDto.getDescription() : schemeDto.getShortDescription())
                .department(schemeDto.getDepartment())
                .schemeType(schemeType)
                .benefits(schemeDto.getBenefits())
                .applicationUrl(schemeDto.getApplicationUrl())
                .contactInformation(schemeDto.getContactInformation())
                .status(Status.ACTIVE)
                .build();

        scheme = schemeRepository.save(scheme);

        // Configure eligibility rule and state:
        // If CENTRAL: visible to all state persons (state = "ALL")
        // If STATE: specific to the specified state (e.g. "Tamil Nadu")
        EligibilityRuleDto ruleDto = schemeDto.getEligibilityRule();
        if (ruleDto == null) {
            ruleDto = new EligibilityRuleDto();
        }

        String stateVal = "ALL";
        if ("STATE".equalsIgnoreCase(schemeType)) {
            if (schemeDto.getState() != null && !schemeDto.getState().isBlank() && !"ALL".equalsIgnoreCase(schemeDto.getState())) {
                stateVal = schemeDto.getState().trim();
            } else if (ruleDto.getState() != null && !ruleDto.getState().isBlank()) {
                stateVal = ruleDto.getState().trim();
            }
        }
        ruleDto.setState(stateVal);
        saveOrUpdateEligibilityRule(scheme.getId(), ruleDto);

        return mapToSchemeDto(scheme, null);
    }

    @Override
    @Transactional
    public SchemeDto updateScheme(Long id, SchemeDto schemeDto) {
        Scheme scheme = schemeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Scheme not found with ID: " + id));

        if (schemeDto.getCategoryId() != null) {
            SchemeCategory category = categoryRepository.findById(schemeDto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            scheme.setCategory(category);
        }

        scheme.setSchemeName(schemeDto.getSchemeName());
        scheme.setShortDescription(schemeDto.getShortDescription());
        scheme.setDescription(schemeDto.getDescription());
        scheme.setDepartment(schemeDto.getDepartment());
        if (schemeDto.getSchemeType() != null) scheme.setSchemeType(schemeDto.getSchemeType().toUpperCase());
        scheme.setBenefits(schemeDto.getBenefits());
        scheme.setApplicationUrl(schemeDto.getApplicationUrl());
        scheme.setContactInformation(schemeDto.getContactInformation());
        if (schemeDto.getStatus() != null) scheme.setStatus(Status.valueOf(schemeDto.getStatus()));

        scheme = schemeRepository.save(scheme);

        if (schemeDto.getEligibilityRule() != null) {
            saveOrUpdateEligibilityRule(scheme.getId(), schemeDto.getEligibilityRule());
        } else if (schemeDto.getState() != null) {
            EligibilityRule rule = scheme.getEligibilityRule();
            if (rule != null) {
                String stateVal = "CENTRAL".equalsIgnoreCase(scheme.getSchemeType()) ? "ALL" : schemeDto.getState().trim();
                rule.setState(stateVal);
                ruleRepository.save(rule);
            }
        }

        return mapToSchemeDto(scheme, null);
    }

    @Override
    @Transactional
    public void deleteScheme(Long id) {
        Scheme scheme = schemeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Scheme not found with ID: " + id));
        schemeRepository.delete(scheme);
    }

    @Override
    @Transactional
    public EligibilityRuleDto saveOrUpdateEligibilityRule(Long schemeId, EligibilityRuleDto ruleDto) {
        Scheme scheme = schemeRepository.findById(schemeId)
                .orElseThrow(() -> new ResourceNotFoundException("Scheme not found with ID: " + schemeId));

        EligibilityRule rule = ruleRepository.findBySchemeId(schemeId).orElse(new EligibilityRule());
        rule.setScheme(scheme);
        rule.setMinAge(ruleDto.getMinAge());
        rule.setMaxAge(ruleDto.getMaxAge());
        rule.setMinIncome(ruleDto.getMinIncome());
        rule.setMaxIncome(ruleDto.getMaxIncome());
        rule.setGender(ruleDto.getGender() != null ? ruleDto.getGender() : "ALL");
        rule.setState(ruleDto.getState() != null ? ruleDto.getState() : "ALL");
        rule.setDistrict(ruleDto.getDistrict() != null ? ruleDto.getDistrict() : "ALL");
        rule.setCategory(ruleDto.getCategory() != null ? ruleDto.getCategory() : "ALL");
        rule.setOccupation(ruleDto.getOccupation() != null ? ruleDto.getOccupation() : "ALL");
        rule.setEmploymentStatus(ruleDto.getEmploymentStatus() != null ? ruleDto.getEmploymentStatus() : "ALL");
        rule.setStudentStatus(ruleDto.getStudentStatus());
        rule.setDisabilityStatus(ruleDto.getDisabilityStatus());
        rule.setFarmerStatus(ruleDto.getFarmerStatus());
        rule.setEntrepreneurStatus(ruleDto.getEntrepreneurStatus());

        rule = ruleRepository.save(rule);
        scheme.setEligibilityRule(rule);
        if (rule.getState() != null && !rule.getState().isBlank()) {
            if ("ALL".equalsIgnoreCase(rule.getState())) {
                scheme.setSchemeType("CENTRAL");
            } else {
                scheme.setSchemeType("STATE");
            }
            schemeRepository.save(scheme);
        }

        return mapToEligibilityRuleDto(rule);
    }

    @Override
    @Transactional
    public RequiredDocumentDto addRequiredDocument(Long schemeId, RequiredDocumentDto documentDto) {
        Scheme scheme = schemeRepository.findById(schemeId)
                .orElseThrow(() -> new ResourceNotFoundException("Scheme not found with ID: " + schemeId));

        RequiredDocument doc = RequiredDocument.builder()
                .scheme(scheme)
                .documentName(documentDto.getDocumentName())
                .description(documentDto.getDescription())
                .mandatory(documentDto.getMandatory() != null ? documentDto.getMandatory() : true)
                .build();

        doc = documentRepository.save(doc);

        return RequiredDocumentDto.builder()
                .id(doc.getId())
                .schemeId(schemeId)
                .documentName(doc.getDocumentName())
                .description(doc.getDescription())
                .mandatory(doc.getMandatory())
                .build();
    }

    @Override
    @Transactional
    public void deleteRequiredDocument(Long documentId) {
        RequiredDocument doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with ID: " + documentId));
        documentRepository.delete(doc);
    }

    @Override
    @Transactional
    public ApplicationProcedureDto addApplicationProcedure(Long schemeId, ApplicationProcedureDto procedureDto) {
        Scheme scheme = schemeRepository.findById(schemeId)
                .orElseThrow(() -> new ResourceNotFoundException("Scheme not found with ID: " + schemeId));

        ApplicationProcedure procedure = ApplicationProcedure.builder()
                .scheme(scheme)
                .stepNumber(procedureDto.getStepNumber())
                .instruction(procedureDto.getInstruction())
                .build();

        procedure = procedureRepository.save(procedure);

        return ApplicationProcedureDto.builder()
                .id(procedure.getId())
                .schemeId(schemeId)
                .stepNumber(procedure.getStepNumber())
                .instruction(procedure.getInstruction())
                .build();
    }

    @Override
    @Transactional
    public void deleteApplicationProcedure(Long procedureId) {
        ApplicationProcedure procedure = procedureRepository.findById(procedureId)
                .orElseThrow(() -> new ResourceNotFoundException("Procedure not found with ID: " + procedureId));
        procedureRepository.delete(procedure);
    }

    @Override
    public SchemeDto mapToSchemeDto(Scheme scheme, Long userId) {
        Boolean isSaved = false;
        if (userId != null) {
            isSaved = savedSchemeRepository.existsByUserIdAndSchemeId(userId, scheme.getId());
        }

        EligibilityRuleDto ruleDto = scheme.getEligibilityRule() != null ? 
                mapToEligibilityRuleDto(scheme.getEligibilityRule()) : null;

        List<RequiredDocumentDto> docDtos = scheme.getRequiredDocuments() != null ?
                scheme.getRequiredDocuments().stream().map(doc -> RequiredDocumentDto.builder()
                        .id(doc.getId())
                        .schemeId(scheme.getId())
                        .documentName(doc.getDocumentName())
                        .description(doc.getDescription())
                        .mandatory(doc.getMandatory())
                        .build()).collect(Collectors.toList()) : List.of();

        List<ApplicationProcedureDto> procDtos = scheme.getApplicationProcedures() != null ?
                scheme.getApplicationProcedures().stream().map(proc -> ApplicationProcedureDto.builder()
                        .id(proc.getId())
                        .schemeId(scheme.getId())
                        .stepNumber(proc.getStepNumber())
                        .instruction(proc.getInstruction())
                        .build()).collect(Collectors.toList()) : List.of();

        return SchemeDto.builder()
                .id(scheme.getId())
                .categoryId(scheme.getCategory().getId())
                .categoryName(scheme.getCategory().getName())
                .schemeName(scheme.getSchemeName())
                .shortDescription(scheme.getShortDescription())
                .description(scheme.getDescription())
                .department(scheme.getDepartment())
                .schemeType(scheme.getSchemeType())
                .state(scheme.getEligibilityRule() != null && scheme.getEligibilityRule().getState() != null ? scheme.getEligibilityRule().getState() : "ALL")
                .benefits(scheme.getBenefits())
                .applicationUrl(scheme.getApplicationUrl())
                .contactInformation(scheme.getContactInformation())
                .status(scheme.getStatus().name())
                .eligibilityRule(ruleDto)
                .requiredDocuments(docDtos)
                .applicationProcedures(procDtos)
                .createdAt(scheme.getCreatedAt())
                .updatedAt(scheme.getUpdatedAt())
                .isSaved(isSaved)
                .build();
    }

    private EligibilityRuleDto mapToEligibilityRuleDto(EligibilityRule rule) {
        return EligibilityRuleDto.builder()
                .id(rule.getId())
                .schemeId(rule.getScheme().getId())
                .minAge(rule.getMinAge())
                .maxAge(rule.getMaxAge())
                .minIncome(rule.getMinIncome())
                .maxIncome(rule.getMaxIncome())
                .gender(rule.getGender())
                .state(rule.getState())
                .district(rule.getDistrict())
                .category(rule.getCategory())
                .occupation(rule.getOccupation())
                .employmentStatus(rule.getEmploymentStatus())
                .studentStatus(rule.getStudentStatus())
                .disabilityStatus(rule.getDisabilityStatus())
                .farmerStatus(rule.getFarmerStatus())
                .entrepreneurStatus(rule.getEntrepreneurStatus())
                .build();
    }

    private Long getUserIdFromEmail(String email) {
        if (email == null) return null;
        return userRepository.findByEmail(email).map(User::getId).orElse(null);
    }
}
