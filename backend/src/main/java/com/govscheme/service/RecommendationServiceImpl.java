package com.govscheme.service;

import com.govscheme.dto.*;
import com.govscheme.entity.*;
import com.govscheme.exception.ResourceNotFoundException;
import com.govscheme.repository.SchemeRepository;
import com.govscheme.repository.UserRepository;
import com.govscheme.repository.SavedSchemeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecommendationServiceImpl implements RecommendationService {

    @Autowired
    private SchemeRepository schemeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SavedSchemeRepository savedSchemeRepository;

    @Autowired
    private SchemeService schemeService;

    @Override
    @Transactional(readOnly = true)
    public List<EligibilityEvaluationResult> evaluateProfile(RecommendationRequestDto profile, String currentUserEmail) {
        List<Scheme> activeSchemes = schemeRepository.findByStatus(Status.ACTIVE);
        
        Long userId = null;
        if (currentUserEmail != null) {
            User user = userRepository.findByEmail(currentUserEmail).orElse(null);
            if (user != null) {
                userId = user.getId();
            }
        }

        final Long currentUserId = userId;
        return activeSchemes.stream()
                .map(scheme -> evaluateSchemeForProfileInternal(scheme, profile, currentUserId))
                .sorted(Comparator.comparing(EligibilityEvaluationResult::getEligible).reversed()
                        .thenComparing(Comparator.comparing(EligibilityEvaluationResult::getMatchPercentage).reversed()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EligibilityEvaluationResult> evaluateCurrentUserProfile(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        UserProfile profile = user.getUserProfile();
        if (profile == null) {
            // Empty profile
            return evaluateProfile(new RecommendationRequestDto(), userEmail);
        }

        RecommendationRequestDto requestDto = convertProfileToRequest(profile);
        return evaluateProfile(requestDto, userEmail);
    }

    @Override
    @Transactional(readOnly = true)
    public EligibilityEvaluationResult evaluateSchemeForProfile(Scheme scheme, RecommendationRequestDto profileRequest, String userEmail) {
        Long userId = null;
        if (userEmail != null) {
            User user = userRepository.findByEmail(userEmail).orElse(null);
            if (user != null) {
                userId = user.getId();
            }
        }
        return evaluateSchemeForProfileInternal(scheme, profileRequest, userId);
    }

    private EligibilityEvaluationResult evaluateSchemeForProfileInternal(Scheme scheme, RecommendationRequestDto profile, Long userId) {
        SchemeDto schemeDto = schemeService.mapToSchemeDto(scheme, userId);
        EligibilityRule rule = scheme.getEligibilityRule();

        List<RuleCriterionResult> breakdown = new ArrayList<>();

        if (rule == null) {
            // Scheme has no specific rules set, open to all
            return EligibilityEvaluationResult.builder()
                    .scheme(schemeDto)
                    .eligible(true)
                    .matchPercentage(100)
                    .satisfiedCriteriaCount(1)
                    .totalEvaluatedCriteriaCount(1)
                    .criteriaBreakdown(List.of(RuleCriterionResult.builder()
                            .criterionName("General Eligibility")
                            .expectedValue("Open to all citizens")
                            .userValue("N/A")
                            .satisfied(true)
                            .explanation("✓ Scheme has no restriction criteria. Open to all citizens.")
                            .build()))
                    .summaryText("Eligible (Open to all citizens)")
                    .build();
        }

        int totalEvaluated = 0;
        int satisfiedCount = 0;

        // 1. Age Rule Evaluation
        if (rule.getMinAge() != null || rule.getMaxAge() != null) {
            totalEvaluated++;
            int minAge = rule.getMinAge() != null ? rule.getMinAge() : 0;
            int maxAge = rule.getMaxAge() != null ? rule.getMaxAge() : 120;
            Integer userAge = profile.getAge();

            boolean satisfied = userAge != null && userAge >= minAge && userAge <= maxAge;
            if (satisfied) satisfiedCount++;

            String expectedStr = (rule.getMinAge() != null ? rule.getMinAge() + " yrs" : "0 yrs") + " to " +
                                 (rule.getMaxAge() != null ? rule.getMaxAge() + " yrs" : "120 yrs");
            String userStr = userAge != null ? userAge + " yrs" : "Not Provided";
            String explanation = satisfied ? 
                    String.format("✓ Age requirement satisfied (%s falls between %s)", userStr, expectedStr) :
                    (userAge == null ? "✗ Age not provided in user profile" :
                    String.format("✗ Age requirement not met (%s is outside %s)", userStr, expectedStr));

            breakdown.add(RuleCriterionResult.builder()
                    .criterionName("Age Range")
                    .expectedValue(expectedStr)
                    .userValue(userStr)
                    .satisfied(satisfied)
                    .explanation(explanation)
                    .build());
        }

        // 2. Annual Income Rule Evaluation
        if (rule.getMinIncome() != null || rule.getMaxIncome() != null) {
            totalEvaluated++;
            double minIncome = rule.getMinIncome() != null ? rule.getMinIncome() : 0.0;
            double maxIncome = rule.getMaxIncome() != null ? rule.getMaxIncome() : Double.MAX_VALUE;
            Double userIncome = profile.getAnnualIncome();

            boolean satisfied = userIncome != null && userIncome >= minIncome && userIncome <= maxIncome;
            if (satisfied) satisfiedCount++;

            String expectedStr = rule.getMaxIncome() != null ? 
                    (rule.getMinIncome() != null && rule.getMinIncome() > 0 ? "₹" + String.format("%.0f", rule.getMinIncome()) + " - " : "Up to ") + "₹" + String.format("%.0f", rule.getMaxIncome()) :
                    "Above ₹" + String.format("%.0f", minIncome);
            String userStr = userIncome != null ? "₹" + String.format("%.0f", userIncome) : "Not Provided";

            String explanation = satisfied ?
                    String.format("✓ Income requirement satisfied (%s is within limit %s)", userStr, expectedStr) :
                    (userIncome == null ? "✗ Annual income not specified in profile" :
                    String.format("✗ Income limit exceeded (%s exceeds limit %s)", userStr, expectedStr));

            breakdown.add(RuleCriterionResult.builder()
                    .criterionName("Annual Family Income")
                    .expectedValue(expectedStr)
                    .userValue(userStr)
                    .satisfied(satisfied)
                    .explanation(explanation)
                    .build());
        }

        // 3. Gender Evaluation
        if (rule.getGender() != null && !"ALL".equalsIgnoreCase(rule.getGender())) {
            totalEvaluated++;
            String userGender = profile.getGender();
            boolean satisfied = userGender != null && (rule.getGender().equalsIgnoreCase(userGender) || "ALL".equalsIgnoreCase(userGender));
            if (satisfied) satisfiedCount++;

            breakdown.add(RuleCriterionResult.builder()
                    .criterionName("Gender Requirement")
                    .expectedValue(rule.getGender())
                    .userValue(userGender != null ? userGender : "Not Provided")
                    .satisfied(satisfied)
                    .explanation(satisfied ? 
                            String.format("✓ Gender matched (%s)", userGender) : 
                            String.format("✗ Gender mismatch (Required: %s, Profile: %s)", rule.getGender(), userGender != null ? userGender : "Not Provided"))
                    .build());
        }

        // 4. State Evaluation
        if (rule.getState() != null && !"ALL".equalsIgnoreCase(rule.getState())) {
            totalEvaluated++;
            String userState = profile.getState();
            boolean satisfied = userState != null && (rule.getState().equalsIgnoreCase(userState) || "ALL".equalsIgnoreCase(userState));
            if (satisfied) satisfiedCount++;

            breakdown.add(RuleCriterionResult.builder()
                    .criterionName("State Residence")
                    .expectedValue(rule.getState())
                    .userValue(userState != null ? userState : "Not Provided")
                    .satisfied(satisfied)
                    .explanation(satisfied ? 
                            String.format("✓ State matched (%s)", userState) : 
                            String.format("✗ Scheme applicable only to residents of %s", rule.getState()))
                    .build());
        }

        // 5. Category Evaluation
        if (rule.getCategory() != null && !"ALL".equalsIgnoreCase(rule.getCategory())) {
            totalEvaluated++;
            String userCat = profile.getCategory();
            boolean satisfied = userCat != null && (rule.getCategory().equalsIgnoreCase(userCat) || "ALL".equalsIgnoreCase(userCat));
            if (satisfied) satisfiedCount++;

            breakdown.add(RuleCriterionResult.builder()
                    .criterionName("Social Category")
                    .expectedValue(rule.getCategory())
                    .userValue(userCat != null ? userCat : "Not Provided")
                    .satisfied(satisfied)
                    .explanation(satisfied ? 
                            String.format("✓ Social category matched (%s)", userCat) : 
                            String.format("✗ Category mismatch (Required: %s)", rule.getCategory()))
                    .build());
        }

        // 6. Occupation Evaluation
        if (rule.getOccupation() != null && !"ALL".equalsIgnoreCase(rule.getOccupation())) {
            totalEvaluated++;
            String userOcc = profile.getOccupation();
            boolean satisfied = userOcc != null && (rule.getOccupation().equalsIgnoreCase(userOcc) || "ALL".equalsIgnoreCase(userOcc));
            if (satisfied) satisfiedCount++;

            breakdown.add(RuleCriterionResult.builder()
                    .criterionName("Occupation")
                    .expectedValue(rule.getOccupation())
                    .userValue(userOcc != null ? userOcc : "Not Provided")
                    .satisfied(satisfied)
                    .explanation(satisfied ? 
                            String.format("✓ Occupation matched (%s)", userOcc) : 
                            String.format("✗ Occupation mismatch (Required: %s)", rule.getOccupation()))
                    .build());
        }

        // 7. Student Status Evaluation
        if (Boolean.TRUE.equals(rule.getStudentStatus())) {
            totalEvaluated++;
            Boolean userStudent = profile.getStudentStatus();
            boolean satisfied = Boolean.TRUE.equals(userStudent);
            if (satisfied) satisfiedCount++;

            breakdown.add(RuleCriterionResult.builder()
                    .criterionName("Student Status")
                    .expectedValue("Active Student")
                    .userValue(Boolean.TRUE.equals(userStudent) ? "Active Student" : "Non-Student")
                    .satisfied(satisfied)
                    .explanation(satisfied ? "✓ Verified Active Student" : "✗ Scheme is exclusively for active students")
                    .build());
        }

        // 8. Disability Status Evaluation
        if (Boolean.TRUE.equals(rule.getDisabilityStatus())) {
            totalEvaluated++;
            Boolean userDisabled = profile.getDisabilityStatus();
            boolean satisfied = Boolean.TRUE.equals(userDisabled);
            if (satisfied) satisfiedCount++;

            breakdown.add(RuleCriterionResult.builder()
                    .criterionName("Disability Status (PwD)")
                    .expectedValue("Person with Disability (PwD)")
                    .userValue(Boolean.TRUE.equals(userDisabled) ? "Yes" : "No")
                    .satisfied(satisfied)
                    .explanation(satisfied ? "✓ Verified Person with Disability" : "✗ Scheme is specifically reserved for Persons with Disabilities (PwD)")
                    .build());
        }

        // 9. Farmer Status Evaluation
        if (Boolean.TRUE.equals(rule.getFarmerStatus())) {
            totalEvaluated++;
            Boolean userFarmer = profile.getFarmerStatus();
            boolean satisfied = Boolean.TRUE.equals(userFarmer);
            if (satisfied) satisfiedCount++;

            breakdown.add(RuleCriterionResult.builder()
                    .criterionName("Farmer / Agricultural Status")
                    .expectedValue("Farmer / Agricultural Worker")
                    .userValue(Boolean.TRUE.equals(userFarmer) ? "Yes" : "No")
                    .satisfied(satisfied)
                    .explanation(satisfied ? "✓ Verified Farmer/Agricultural status" : "✗ Scheme is reserved for farmers or agricultural workers")
                    .build());
        }

        // 10. Entrepreneur Status Evaluation
        if (Boolean.TRUE.equals(rule.getEntrepreneurStatus())) {
            totalEvaluated++;
            Boolean userEnt = profile.getEntrepreneurStatus();
            boolean satisfied = Boolean.TRUE.equals(userEnt);
            if (satisfied) satisfiedCount++;

            breakdown.add(RuleCriterionResult.builder()
                    .criterionName("Entrepreneur / Business Status")
                    .expectedValue("Entrepreneur / Small Business Owner")
                    .userValue(Boolean.TRUE.equals(userEnt) ? "Yes" : "No")
                    .satisfied(satisfied)
                    .explanation(satisfied ? "✓ Verified Entrepreneur/Business Owner status" : "✗ Scheme is intended for entrepreneurs and business founders")
                    .build());
        }

        // If no criteria evaluated (open to all)
        if (totalEvaluated == 0) {
            totalEvaluated = 1;
            satisfiedCount = 1;
            breakdown.add(RuleCriterionResult.builder()
                    .criterionName("Universal Eligibility")
                    .expectedValue("All Profiles")
                    .userValue("Any")
                    .satisfied(true)
                    .explanation("✓ Scheme open to all eligible citizens with no restrictive conditions")
                    .build());
        }

        boolean isFullyEligible = (satisfiedCount == totalEvaluated);
        int percentage = (int) Math.round(((double) satisfiedCount / totalEvaluated) * 100);

        String summaryText = isFullyEligible ? 
                "✓ FULLY ELIGIBLE - All " + satisfiedCount + " mandatory conditions satisfied" :
                "✗ INELIGIBLE - Satisfied " + satisfiedCount + " of " + totalEvaluated + " conditions";

        return EligibilityEvaluationResult.builder()
                .scheme(schemeDto)
                .eligible(isFullyEligible)
                .matchPercentage(percentage)
                .satisfiedCriteriaCount(satisfiedCount)
                .totalEvaluatedCriteriaCount(totalEvaluated)
                .criteriaBreakdown(breakdown)
                .summaryText(summaryText)
                .build();
    }

    @Override
    public RecommendationRequestDto convertProfileToRequest(UserProfile profile) {
        return RecommendationRequestDto.builder()
                .age(profile.getAge())
                .gender(profile.getGender())
                .state(profile.getState())
                .district(profile.getDistrict())
                .annualIncome(profile.getAnnualIncome())
                .occupation(profile.getOccupation())
                .employmentStatus(profile.getEmploymentStatus())
                .category(profile.getCategory())
                .studentStatus(profile.getStudentStatus())
                .disabilityStatus(profile.getDisabilityStatus())
                .farmerStatus(profile.getFarmerStatus())
                .entrepreneurStatus(profile.getEntrepreneurStatus())
                .build();
    }
}
