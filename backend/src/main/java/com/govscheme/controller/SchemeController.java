package com.govscheme.controller;

import com.govscheme.dto.SchemeCategoryDto;
import com.govscheme.dto.SchemeDto;
import com.govscheme.service.SchemeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schemes")
@CrossOrigin(origins = "*")
public class SchemeController {

    @Autowired
    private SchemeService schemeService;

    @GetMapping
    public ResponseEntity<List<SchemeDto>> getAllActiveSchemes(Authentication authentication) {
        String email = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(schemeService.getAllActiveSchemes(email));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SchemeDto> getSchemeById(@PathVariable Long id, Authentication authentication) {
        String email = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(schemeService.getSchemeById(id, email));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<SchemeDto>> getSchemesByCategory(@PathVariable Long categoryId, Authentication authentication) {
        String email = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(schemeService.getSchemesByCategory(categoryId, email));
    }

    @GetMapping("/search")
    public ResponseEntity<List<SchemeDto>> searchSchemes(@RequestParam(value = "query", required = false) String query, Authentication authentication) {
        String email = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(schemeService.searchSchemes(query, email));
    }

    @GetMapping("/categories")
    public ResponseEntity<List<SchemeCategoryDto>> getAllCategories() {
        return ResponseEntity.ok(schemeService.getAllCategories());
    }
}
