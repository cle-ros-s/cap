package com.govscheme.controller;

import com.govscheme.dto.EligibilityEvaluationResult;
import com.govscheme.dto.RecommendationRequestDto;
import com.govscheme.entity.Scheme;
import com.govscheme.repository.SchemeRepository;
import com.govscheme.service.RecommendationService;
import com.govscheme.service.SchemeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private SchemeRepository schemeRepository;

    @PostMapping("/recommendations")
    public ResponseEntity<List<EligibilityEvaluationResult>> getRecommendations(
            @RequestBody RecommendationRequestDto requestDto,
            Authentication authentication) {
        String email = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(recommendationService.evaluateProfile(requestDto, email));
    }

    @GetMapping("/recommendations")
    public ResponseEntity<List<EligibilityEvaluationResult>> getLoggedInRecommendations(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(recommendationService.evaluateCurrentUserProfile(authentication.getName()));
    }

    @GetMapping("/eligibility/{schemeId}")
    public ResponseEntity<EligibilityEvaluationResult> checkSchemeEligibility(
            @PathVariable Long schemeId,
            @RequestParam(required = false) Integer age,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) Double annualIncome,
            @RequestParam(required = false) String occupation,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean studentStatus,
            @RequestParam(required = false) Boolean disabilityStatus,
            @RequestParam(required = false) Boolean farmerStatus,
            @RequestParam(required = false) Boolean entrepreneurStatus,
            Authentication authentication) {

        Scheme scheme = schemeRepository.findById(schemeId).orElseThrow(() -> new RuntimeException("Scheme not found"));
        String email = authentication != null ? authentication.getName() : null;

        RecommendationRequestDto dto;
        if (age != null || annualIncome != null || state != null) {
            dto = RecommendationRequestDto.builder()
                    .age(age)
                    .gender(gender)
                    .state(state)
                    .district(district)
                    .annualIncome(annualIncome)
                    .occupation(occupation)
                    .category(category)
                    .studentStatus(studentStatus)
                    .disabilityStatus(disabilityStatus)
                    .farmerStatus(farmerStatus)
                    .entrepreneurStatus(entrepreneurStatus)
                    .build();
        } else if (email != null) {
            return ResponseEntity.ok(recommendationService.evaluateCurrentUserProfile(email).stream()
                    .filter(res -> res.getScheme().getId().equals(schemeId))
                    .findFirst()
                    .orElseGet(() -> recommendationService.evaluateSchemeForProfile(scheme, new RecommendationRequestDto(), email)));
        } else {
            dto = new RecommendationRequestDto();
        }

        return ResponseEntity.ok(recommendationService.evaluateSchemeForProfile(scheme, dto, email));
    }
}
