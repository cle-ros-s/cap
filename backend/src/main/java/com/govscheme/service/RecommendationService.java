package com.govscheme.service;

import com.govscheme.dto.EligibilityEvaluationResult;
import com.govscheme.dto.RecommendationRequestDto;
import com.govscheme.entity.Scheme;
import com.govscheme.entity.UserProfile;

import java.util.List;

public interface RecommendationService {
    
    /**
     * Evaluates a user profile against all active schemes using a transparent, 
     * deterministic rule-based engine (NO AI/ML).
     */
    List<EligibilityEvaluationResult> evaluateProfile(RecommendationRequestDto profileRequest, String currentUserEmail);

    /**
     * Evaluates logged-in user profile against all active schemes.
     */
    List<EligibilityEvaluationResult> evaluateCurrentUserProfile(String userEmail);

    /**
     * Evaluates a single scheme against a given profile request.
     */
    EligibilityEvaluationResult evaluateSchemeForProfile(Scheme scheme, RecommendationRequestDto profileRequest, String userEmail);

    /**
     * Converts a UserProfile entity to a RecommendationRequestDto.
     */
    RecommendationRequestDto convertProfileToRequest(UserProfile profile);
}
