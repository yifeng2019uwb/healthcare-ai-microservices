package com.healthcare.controller;

import com.healthcare.dto.ImportResult;
import com.healthcare.service.AdminImportService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Admin-only import endpoints. ADMIN role enforced at gateway (/api/admin/**).
 * Call endpoints in FK order: organizations → patients → providers → encounters → conditions → allergies
 */
@RestController
@RequestMapping("/api/admin/import")
public class AdminImportController {

    private final AdminImportService adminImportService;

    public AdminImportController(AdminImportService adminImportService) {
        this.adminImportService = adminImportService;
    }

    @PostMapping(value = "/organizations", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportResult> importOrganizations(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(adminImportService.importOrganizations(file));
    }

    @PostMapping(value = "/patients", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportResult> importPatients(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(adminImportService.importPatients(file));
    }

    @PostMapping(value = "/providers", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportResult> importProviders(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(adminImportService.importProviders(file));
    }

    @PostMapping(value = "/encounters", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportResult> importEncounters(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(adminImportService.importEncounters(file));
    }

    @PostMapping(value = "/conditions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportResult> importConditions(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(adminImportService.importConditions(file));
    }

    @PostMapping(value = "/allergies", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportResult> importAllergies(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(adminImportService.importAllergies(file));
    }
}
