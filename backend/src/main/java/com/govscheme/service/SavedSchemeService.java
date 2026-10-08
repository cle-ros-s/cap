package com.govscheme.service;

import com.govscheme.dto.SchemeDto;

import java.util.List;

public interface SavedSchemeService {
    SchemeDto saveScheme(String userEmail, Long schemeId);
    void removeSavedScheme(String userEmail, Long schemeId);
    List<SchemeDto> getUserSavedSchemes(String userEmail);
    boolean isSchemeSaved(String userEmail, Long schemeId);
}
