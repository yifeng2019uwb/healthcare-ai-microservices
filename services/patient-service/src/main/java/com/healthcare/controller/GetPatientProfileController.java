package com.healthcare.controller;

import com.healthcare.constants.PatientServiceConstants;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST Controller for Get Patient Profile operations.
 * Handles patient profile retrieval.
 *
 * @author Healthcare AI Team
 * @version 1.0.0
 * @since 2025-01-09
 */
@RestController
@RequestMapping(PatientServiceConstants.GET_PROFILE_ENDPOINT)
@CrossOrigin(origins = "*")
public class GetPatientProfileController {

}
