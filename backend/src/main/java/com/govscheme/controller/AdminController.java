package com.govscheme.controller;

import com.govscheme.dto.*;
import com.govscheme.service.AdminService;
import com.govscheme.service.SchemeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private SchemeService schemeService;

    // --- Statistics ---
    @GetMapping("/stats")
    public ResponseEntity<AdminDashboardStatsDto> getStats() {
        return ResponseEntity.ok(adminService.getAdminDashboardStats());
    }

    // --- User Management ---
    @GetMapping("/users")
    public ResponseEntity<List<UserSummaryDto>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PutMapping("/users/{userId}/status")
    public ResponseEntity<UserSummaryDto> toggleUserStatus(@PathVariable Long userId, @RequestParam String status) {
        return ResponseEntity.ok(adminService.toggleUserStatus(userId, status));
    }

    // --- Scheme Management ---
    @GetMapping("/schemes")
    public ResponseEntity<List<SchemeDto>> getAllSchemesAdmin() {
        return ResponseEntity.ok(schemeService.getAllSchemesAdmin());
    }

    @PostMapping("/schemes")
    public ResponseEntity<SchemeDto> createScheme(@Valid @RequestBody SchemeDto schemeDto) {
        return ResponseEntity.ok(schemeService.createScheme(schemeDto));
    }

    @PutMapping("/schemes/{id}")
    public ResponseEntity<SchemeDto> updateScheme(@PathVariable Long id, @Valid @RequestBody SchemeDto schemeDto) {
        return ResponseEntity.ok(schemeService.updateScheme(id, schemeDto));
    }

    @DeleteMapping("/schemes/{id}")
    public ResponseEntity<Void> deleteScheme(@PathVariable Long id) {
        schemeService.deleteScheme(id);
        return ResponseEntity.noContent().build();
    }

    // --- Category Management ---
    @PostMapping("/categories")
    public ResponseEntity<SchemeCategoryDto> createCategory(@Valid @RequestBody SchemeCategoryDto categoryDto) {
        return ResponseEntity.ok(schemeService.createCategory(categoryDto));
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<SchemeCategoryDto> updateCategory(@PathVariable Long id, @Valid @RequestBody SchemeCategoryDto categoryDto) {
        return ResponseEntity.ok(schemeService.updateCategory(id, categoryDto));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        schemeService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    // --- Eligibility Rules ---
    @PostMapping("/schemes/{schemeId}/rule")
    public ResponseEntity<EligibilityRuleDto> saveOrUpdateRule(@PathVariable Long schemeId, @RequestBody EligibilityRuleDto ruleDto) {
        return ResponseEntity.ok(schemeService.saveOrUpdateEligibilityRule(schemeId, ruleDto));
    }

    // --- Required Documents ---
    @PostMapping("/schemes/{schemeId}/documents")
    public ResponseEntity<RequiredDocumentDto> addDocument(@PathVariable Long schemeId, @RequestBody RequiredDocumentDto docDto) {
        return ResponseEntity.ok(schemeService.addRequiredDocument(schemeId, docDto));
    }

    @DeleteMapping("/documents/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        schemeService.deleteRequiredDocument(id);
        return ResponseEntity.noContent().build();
    }

    // --- Application Procedures ---
    @PostMapping("/schemes/{schemeId}/procedures")
    public ResponseEntity<ApplicationProcedureDto> addProcedure(@PathVariable Long schemeId, @RequestBody ApplicationProcedureDto procedureDto) {
        return ResponseEntity.ok(schemeService.addApplicationProcedure(schemeId, procedureDto));
    }

    @DeleteMapping("/procedures/{id}")
    public ResponseEntity<Void> deleteProcedure(@PathVariable Long id) {
        schemeService.deleteApplicationProcedure(id);
        return ResponseEntity.noContent().build();
    }
}
