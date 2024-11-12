package ca.bc.gov.educ.api.trax.controller.v2;

import ca.bc.gov.educ.api.trax.service.institute.CodeService;
import ca.bc.gov.educ.api.trax.util.EducGradTraxApiConstants;
import ca.bc.gov.educ.api.trax.util.GradValidation;
import ca.bc.gov.educ.api.trax.util.PermissionsConstants;
import ca.bc.gov.educ.api.trax.util.ResponseHelper;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController("codeControllerV2")
@CrossOrigin
@OpenAPIDefinition(info = @Info(title = "API for TRAX Code Tables Data.",
        description = "This API is for Reading TRAX Code Tables data.", version = "1"),
        security = {@SecurityRequirement(name = "OAUTH2",
                scopes = {"READ_GRAD_COUNTRY_CODE_DATA",
                        "READ_GRAD_PROVINCE_CODE_DATA",
                })})
public class CodeController {

    CodeService codeService;
    GradValidation validation;
    ResponseHelper response;

    @Autowired
    public CodeController(CodeService codeService, GradValidation validation, ResponseHelper response) {
        this.codeService = codeService;
        this.validation = validation;
        this.response = response;
    }

    @PutMapping(EducGradTraxApiConstants.GRAD_TRAX_CODE_URL_MAPPING_V2 + EducGradTraxApiConstants.PUT_SCHOOL_CATEGORY_CODES_MAPPING)
    @PreAuthorize(PermissionsConstants.UPDATE_GRAD_TRAX_CACHE)
    @Operation(summary = "Reload School Category Codes in the cache", description = "Reload School Category Codes in the cache", tags = {"Cache"})
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "422", description = "UNPROCESSABLE CONTENT")})
    public ResponseEntity reloadSchoolCategoryCodesIntoCache() {
        log.debug("reloadSchoolCategoryCodesIntoCache : ");
        try {
            codeService.initializeSchoolCategoryCodeCache(true);
        } catch (Exception e) {
            return ResponseEntity.unprocessableEntity().body("Error loading School Category Codes into cache");
        }
        return ResponseEntity.ok("School Category Codes loaded into cache!");
    }

    @PutMapping(EducGradTraxApiConstants.GRAD_TRAX_CODE_URL_MAPPING_V2 + EducGradTraxApiConstants.PUT_SCHOOL_FUNDING_GROUP_CODES_MAPPING)
    @PreAuthorize(PermissionsConstants.UPDATE_GRAD_TRAX_CACHE)
    @Operation(summary = "Reload School Funding Group Codes in the cache", description = "Reload School Funding Group Codes in the cache", tags = {"Cache"})
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "422", description = "UNPROCESSABLE CONTENT")})
    public ResponseEntity reloadSchoolFundingGroupCodesIntoCache() {
        log.debug("reloadSchoolFundingGroupCodesIntoCache : ");
        try {
            codeService.initializeSchoolFundingGroupCodeCache(true);
        } catch (Exception e) {
            return ResponseEntity.unprocessableEntity().body("Error loading School Funding Group Codes into cache");
        }
        return ResponseEntity.ok("School Funding Group Codes loaded into cache!");
    }
}
