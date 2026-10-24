package com.govscheme.service;

import com.govscheme.dto.*;
import com.govscheme.entity.Role;
import com.govscheme.entity.Status;
import com.govscheme.entity.User;
import com.govscheme.entity.UserProfile;
import com.govscheme.exception.BadRequestException;
import com.govscheme.exception.ResourceNotFoundException;
import com.govscheme.repository.SavedSchemeRepository;
import com.govscheme.repository.UserProfileRepository;
import com.govscheme.repository.UserRepository;
import com.govscheme.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private SavedSchemeRepository savedSchemeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private SavedSchemeService savedSchemeService;

    @Override
    @Transactional
    public AuthResponse registerUser(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email address is already registered!");
        }

        if (userRepository.existsByMobile(request.getMobile())) {
            throw new BadRequestException("Mobile number is already registered!");
        }

        String rawAadhaar = request.getAadhaarNumber();
        String cleanAadhaar = (rawAadhaar != null && !rawAadhaar.trim().isEmpty())
                ? rawAadhaar.replaceAll("\\s+", "")
                : null;

        if (cleanAadhaar != null && !cleanAadhaar.isBlank()) {
            if (userRepository.existsByAadhaarNumber(cleanAadhaar)) {
                throw new BadRequestException("Aadhaar number is already registered!");
            }
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .mobile(request.getMobile())
                .aadhaarNumber(cleanAadhaar)
                .aadhaarVerified(Boolean.TRUE.equals(request.getAadhaarVerified()))
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_USER)
                .status(Status.ACTIVE)
                .build();

        user = userRepository.save(user);

        // Create empty profile linked to user
        UserProfile profile = UserProfile.builder()
                .user(user)
                .aadhaarNumber(cleanAadhaar)
                .studentStatus(false)
                .disabilityStatus(false)
                .farmerStatus(false)
                .entrepreneurStatus(false)
                .build();

        userProfileRepository.save(profile);

        // Authenticate new user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        return AuthResponse.builder()
                .token(jwt)
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .mobile(user.getMobile())
                .role(user.getRole().name())
                .message("User registered successfully!")
                .build();
    }

    @Override
    public AuthResponse loginUser(LoginRequest request) {
        // Find user by email or mobile first to check status
        User user = userRepository.findByEmail(request.getUsername())
                .orElseGet(() -> userRepository.findByMobile(request.getUsername())
                        .orElseThrow(() -> new BadRequestException("Invalid email/mobile or password")));

        if (user.getStatus() == Status.INACTIVE) {
            throw new BadRequestException("Account is deactivated. Please contact support.");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        return AuthResponse.builder()
                .token(jwt)
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .mobile(user.getMobile())
                .role(user.getRole().name())
                .message("Login successful!")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileDto getUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        UserProfile profile = user.getUserProfile();
        if (profile == null) {
            return UserProfileDto.builder()
                    .userId(user.getId())
                    .name(user.getName())
                    .email(user.getEmail())
                    .mobile(user.getMobile())
                    .profileCompletionPercentage(0)
                    .build();
        }

        return mapToProfileDto(user, profile);
    }

    @Override
    @Transactional
    public UserProfileDto updateUserProfile(String email, UserProfileDto profileDto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        UserProfile profile = user.getUserProfile();
        if (profile == null) {
            profile = new UserProfile();
            profile.setUser(user);
        }

        profile.setDateOfBirth(profileDto.getDateOfBirth());
        profile.setAge(profileDto.getAge());
        profile.setGender(profileDto.getGender());
        profile.setState(profileDto.getState());
        profile.setDistrict(profileDto.getDistrict());
        profile.setAnnualIncome(profileDto.getAnnualIncome());
        profile.setOccupation(profileDto.getOccupation());
        profile.setEmploymentStatus(profileDto.getEmploymentStatus());
        profile.setCategory(profileDto.getCategory());
        profile.setStudentStatus(Boolean.TRUE.equals(profileDto.getStudentStatus()));
        profile.setDisabilityStatus(Boolean.TRUE.equals(profileDto.getDisabilityStatus()));
        profile.setFarmerStatus(Boolean.TRUE.equals(profileDto.getFarmerStatus()));
        profile.setEntrepreneurStatus(Boolean.TRUE.equals(profileDto.getEntrepreneurStatus()));

        if (profileDto.getAadhaarNumber() != null && !profileDto.getAadhaarNumber().trim().isEmpty()) {
            String cleanAadhaar = profileDto.getAadhaarNumber().replaceAll("\\s+", "");
            profile.setAadhaarNumber(cleanAadhaar);
            user.setAadhaarNumber(cleanAadhaar);
            if (Boolean.TRUE.equals(profileDto.getAadhaarVerified())) {
                user.setAadhaarVerified(true);
            }
            userRepository.save(user);
        }

        profile = userProfileRepository.save(profile);

        return mapToProfileDto(user, profile);
    }

    @Override
    @Transactional(readOnly = true)
    public UserSummaryDto getCurrentUserSummary(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return UserSummaryDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .mobile(user.getMobile())
                .aadhaarNumber(user.getAadhaarNumber())
                .aadhaarVerified(user.getAadhaarVerified())
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .createdAt(user.getCreatedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserDashboardStatsDto getUserDashboardData(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UserProfileDto profile = getUserProfile(email);
        List<EligibilityEvaluationResult> recommendations = recommendationService.evaluateCurrentUserProfile(email);
        long eligibleCount = recommendations.stream().filter(EligibilityEvaluationResult::getEligible).count();
        
        List<SchemeDto> savedSchemes = savedSchemeService.getUserSavedSchemes(email);

        return UserDashboardStatsDto.builder()
                .userName(user.getName())
                .profileCompletionPercentage(profile.getProfileCompletionPercentage())
                .eligibleSchemesCount(eligibleCount)
                .savedSchemesCount(savedSchemes.size())
                .profile(profile)
                .topRecommendedSchemes(recommendations.stream().limit(5).collect(Collectors.toList()))
                .savedSchemes(savedSchemes)
                .build();
    }

    private UserProfileDto mapToProfileDto(User user, UserProfile profile) {
        int filledFields = 0;
        int totalFields = 9;

        if (profile.getAge() != null) filledFields++;
        if (profile.getGender() != null && !profile.getGender().isEmpty()) filledFields++;
        if (profile.getState() != null && !profile.getState().isEmpty()) filledFields++;
        if (profile.getDistrict() != null && !profile.getDistrict().isEmpty()) filledFields++;
        if (profile.getAnnualIncome() != null) filledFields++;
        if (profile.getOccupation() != null && !profile.getOccupation().isEmpty()) filledFields++;
        if (profile.getEmploymentStatus() != null && !profile.getEmploymentStatus().isEmpty()) filledFields++;
        if (profile.getCategory() != null && !profile.getCategory().isEmpty()) filledFields++;
        if (profile.getDateOfBirth() != null) filledFields++;

        int percentage = (int) Math.round(((double) filledFields / totalFields) * 100);

        return UserProfileDto.builder()
                .id(profile.getId())
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .mobile(user.getMobile())
                .dateOfBirth(profile.getDateOfBirth())
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
                .aadhaarNumber(user.getAadhaarNumber() != null ? user.getAadhaarNumber() : profile.getAadhaarNumber())
                .aadhaarVerified(user.getAadhaarVerified())
                .profileCompletionPercentage(percentage)
                .build();
    }
}
