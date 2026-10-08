package com.govscheme.service;

import com.govscheme.dto.SchemeDto;
import com.govscheme.entity.SavedScheme;
import com.govscheme.entity.Scheme;
import com.govscheme.entity.User;
import com.govscheme.exception.BadRequestException;
import com.govscheme.exception.ResourceNotFoundException;
import com.govscheme.repository.SavedSchemeRepository;
import com.govscheme.repository.SchemeRepository;
import com.govscheme.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SavedSchemeServiceImpl implements SavedSchemeService {

    @Autowired
    private SavedSchemeRepository savedSchemeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SchemeRepository schemeRepository;

    @Autowired
    private SchemeService schemeService;

    @Override
    @Transactional
    public SchemeDto saveScheme(String userEmail, Long schemeId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Scheme scheme = schemeRepository.findById(schemeId)
                .orElseThrow(() -> new ResourceNotFoundException("Scheme not found with ID: " + schemeId));

        if (savedSchemeRepository.existsByUserIdAndSchemeId(user.getId(), schemeId)) {
            throw new BadRequestException("Scheme is already saved in your bookmarks!");
        }

        SavedScheme savedScheme = SavedScheme.builder()
                .user(user)
                .scheme(scheme)
                .build();

        savedSchemeRepository.save(savedScheme);

        return schemeService.mapToSchemeDto(scheme, user.getId());
    }

    @Override
    @Transactional
    public void removeSavedScheme(String userEmail, Long schemeId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!savedSchemeRepository.existsByUserIdAndSchemeId(user.getId(), schemeId)) {
            throw new ResourceNotFoundException("Bookmark not found for scheme ID: " + schemeId);
        }

        savedSchemeRepository.deleteByUserIdAndSchemeId(user.getId(), schemeId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SchemeDto> getUserSavedSchemes(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return savedSchemeRepository.findByUserId(user.getId()).stream()
                .map(saved -> schemeService.mapToSchemeDto(saved.getScheme(), user.getId()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isSchemeSaved(String userEmail, Long schemeId) {
        User user = userRepository.findByEmail(userEmail).orElse(null);
        if (user == null) return false;
        return savedSchemeRepository.existsByUserIdAndSchemeId(user.getId(), schemeId);
    }
}
