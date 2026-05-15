package com.healthcare.service;

import com.healthcare.dto.ImportResult;
import org.springframework.web.multipart.MultipartFile;

public interface AdminImportService {
    ImportResult importOrganizations(MultipartFile file);
    ImportResult importPatients(MultipartFile file);
    ImportResult importProviders(MultipartFile file);
    ImportResult importEncounters(MultipartFile file);
    ImportResult importConditions(MultipartFile file);
    ImportResult importAllergies(MultipartFile file);
}
