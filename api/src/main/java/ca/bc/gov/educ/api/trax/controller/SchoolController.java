package ca.bc.gov.educ.api.trax.controller;

import ca.bc.gov.educ.api.trax.model.dto.CommonSchool;
import ca.bc.gov.educ.api.trax.model.dto.School;
import ca.bc.gov.educ.api.trax.service.SchoolService;
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

import java.util.List;

@CrossOrigin
@RestController
@Slf4j
//@RequestMapping(EducGradTraxApiConstants.GRAD_SCHOOL_URL_MAPPING)
@OpenAPIDefinition(info = @Info(title = "API for School Data.", description = "This Read API is for Reading school data.", version = "1"),
		security = {@SecurityRequirement(name = "OAUTH2", scopes = {"READ_GRAD_SCHOOL_DATA"})})
public class SchoolController {

    private static final String BEARER = "Bearer ";


    SchoolService schoolService;
    GradValidation validation;
	ResponseHelper response;

    @Autowired
    public SchoolController(SchoolService schoolService, GradValidation validation, ResponseHelper response) {
        this.schoolService = schoolService;
        this.validation = validation;
        this.response = response;
    }

    @GetMapping(EducGradTraxApiConstants.GRAD_SCHOOL_URL_MAPPING_V1)
    @PreAuthorize(PermissionsConstants.READ_SCHOOL_DATA)
    @Operation(summary = "Find All Schools", description = "Get All Schools", tags = { "School" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public List<School> getAllSchools() {
    	log.debug("getAllSchools : ");
        return schoolService.getSchoolList();
    }

    
    @GetMapping(EducGradTraxApiConstants.GRAD_SCHOOL_URL_MAPPING_V1 + EducGradTraxApiConstants.GET_SCHOOL_BY_CODE_MAPPING)
    @PreAuthorize(PermissionsConstants.READ_SCHOOL_DATA)
    @Operation(summary = "Find a School by Mincode", description = "Get a School by Mincode", tags = { "School" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "204", description = "NO CONTENT")})
    public ResponseEntity<School> getSchoolDetails(@PathVariable String minCode, @RequestHeader(name="Authorization") String accessToken) {
    	log.debug("getSchoolDetails : ");
    	School schoolResponse = schoolService.getSchoolDetails(minCode, accessToken.replace(BEARER, ""));
    	if(schoolResponse != null) {
    		return response.GET(schoolResponse);
    	}else {
    		return response.NOT_FOUND();
    	}
    }

    @GetMapping(EducGradTraxApiConstants.GRAD_SCHOOL_URL_MAPPING_V1 + EducGradTraxApiConstants.GET_COMMON_SCHOOLS)
    @PreAuthorize(PermissionsConstants.READ_SCHOOL_DATA)
    @Operation(summary = "Get all common schools", description = "Get a list of all common schools", tags = { "School" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<List<CommonSchool>> getAllCommonSchool() {
        return response.GET(schoolService.getCommonSchools());
    }

    @GetMapping(EducGradTraxApiConstants.GRAD_SCHOOL_URL_MAPPING_V1 + EducGradTraxApiConstants.GET_COMMON_SCHOOL_BY_CODE_MAPPING)
    @PreAuthorize(PermissionsConstants.READ_SCHOOL_DATA)
    @Operation(summary = "Find a Common School by Mincode", description = "Find a Common School by Mincode", tags = { "School" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND")})
    public ResponseEntity<CommonSchool> getCommonSchool(@PathVariable String minCode) {
        CommonSchool schoolResponse = schoolService.getCommonSchool(minCode);
        if(schoolResponse != null) {
            return response.GET(schoolResponse);
        }else {
            return response.NOT_FOUND();
        }
    }
    
    @GetMapping(EducGradTraxApiConstants.GRAD_SCHOOL_URL_MAPPING_V1 + EducGradTraxApiConstants.GET_SCHOOL_SEARCH_MAPPING)
    @PreAuthorize(PermissionsConstants.READ_SCHOOL_DATA)
    @Operation(summary = "Search for a school", description = "Search for a School", tags = { "School" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "400", description = "BAD REQUEST")})
    public ResponseEntity<List<School>> getSchoolsByParams(
    		@RequestParam(value = "schoolName", required = false) String schoolName,
    		@RequestParam(value = "mincode", required = false) String mincode,
            @RequestParam(value = "district", required = false) String district,
            @RequestHeader(name="Authorization") String accessToken) {
		return response.GET(schoolService.getSchoolsByParams(schoolName, mincode, district, accessToken.replace(BEARER, "")));
    }

    @GetMapping(EducGradTraxApiConstants.GRAD_SCHOOL_URL_MAPPING_V1 + EducGradTraxApiConstants.CHECK_SCHOOL_BY_CODE_MAPPING)
    @PreAuthorize(PermissionsConstants.READ_SCHOOL_DATA)
    @Operation(summary = "Check school existence by Mincode", description = "Check school existence by Mincode", tags = { "School" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "204", description = "NO CONTENT")})
    public ResponseEntity<Boolean> checkSchoolExists(@PathVariable String minCode) {
        return response.GET(schoolService.existsSchool(minCode));
    }

    @GetMapping(EducGradTraxApiConstants.GRAD_SCHOOL_URL_MAPPING_V1 + EducGradTraxApiConstants.GET_SCHOOLS_BY_SCHOOL_CATEGORY_MAPPING)
    @PreAuthorize(PermissionsConstants.READ_SCHOOL_DATA)
    @Operation(summary = "Get schools by SchoolCategory", description = "Get schools by SchoolCategory", tags = { "School" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "204", description = "NO CONTENT")})
    public ResponseEntity<List<School>> getSchoolsBySchoolCategory(@RequestParam(required = false) String schoolCategory, @RequestHeader(name="Authorization") String accessToken) {
        return response.GET(schoolService.getSchoolsBySchoolCategory(schoolCategory));
    }
}
