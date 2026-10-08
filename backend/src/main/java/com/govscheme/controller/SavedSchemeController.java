package com.govscheme.controller;

import com.govscheme.dto.SchemeDto;
import com.govscheme.service.SavedSchemeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/saved-schemes")
@CrossOrigin(origins = "*")
public class SavedSchemeController {

    @Autowired
    private SavedSchemeService savedSchemeService;

    @GetMapping
    public ResponseEntity<List<SchemeDto>> getUserSavedSchemes(Authentication authentication) {
        return ResponseEntity.ok(savedSchemeService.getUserSavedSchemes(authentication.getName()));
    }

    @PostMapping("/{schemeId}")
    public ResponseEntity<SchemeDto> saveScheme(@PathVariable Long schemeId, Authentication authentication) {
        return ResponseEntity.ok(savedSchemeService.saveScheme(authentication.getName(), schemeId));
    }

    @DeleteMapping("/{schemeId}")
    public ResponseEntity<Void> removeSavedScheme(@PathVariable Long schemeId, Authentication authentication) {
        savedSchemeService.removeSavedScheme(authentication.getName(), schemeId);
        return ResponseEntity.noContent().build();
    }
}
