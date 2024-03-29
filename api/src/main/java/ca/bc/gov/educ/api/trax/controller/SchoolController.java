package ca.bc.gov.educ.api.trax.controller;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping(EducGradTraxApiConstants.GRAD_SCHOOL_URL_MAPPING)
@OpenAPIDefinition(info = @Info(title = "API for School Data.", description = "This Read API is for Reading school data.", version = "1"),
		security = {@SecurityRequirement(name = "OAUTH2", scopes = {"READ_GRAD_SCHOOL_DATA"})})
public class SchoolController {

    private static Logger logger = LoggerFactory.getLogger(SchoolController.class);

    private static final String BEARER = "Bearer ";

    @Autowired
    SchoolService schoolService;
    
    @Autowired
    GradValidation validation;
    
    @Autowired
	ResponseHelper response;

    @GetMapping
    @PreAuthorize(PermissionsConstants.READ_SCHOOL_DATA)
    @Operation(summary = "Find All Schools", description = "Get All Schools", tags = { "School" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public List<School> getAllSchools() {
    	logger.debug("getAllSchools : ");
        return schoolService.getSchoolList();
    }
    
    
    @GetMapping(EducGradTraxApiConstants.GET_SCHOOL_BY_CODE_MAPPING)
    @PreAuthorize(PermissionsConstants.READ_SCHOOL_DATA)
    @Operation(summary = "Find a School by Mincode", description = "Get a School by Mincode", tags = { "School" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "204", description = "NO CONTENT")})
    public ResponseEntity<School> getSchoolDetails(@PathVariable String minCode, @RequestHeader(name="Authorization") String accessToken) {
    	logger.debug("getSchoolDetails : ");
    	School schoolResponse = schoolService.getSchoolDetails(minCode, accessToken.replace(BEARER, ""));
    	if(schoolResponse != null) {
    		return response.GET(schoolResponse);
    	}else {
    		return response.NOT_FOUND();
    	}
    }
    
    @GetMapping(EducGradTraxApiConstants.GET_SCHOOL_SEARCH_MAPPING)
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

    @GetMapping(EducGradTraxApiConstants.CHECK_SCHOOL_BY_CODE_MAPPING)
    @PreAuthorize(PermissionsConstants.READ_SCHOOL_DATA)
    @Operation(summary = "Check school existence by Mincode", description = "Check school existence by Mincode", tags = { "School" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "204", description = "NO CONTENT")})
    public ResponseEntity<Boolean> checkSchoolExists(@PathVariable String minCode) {
        return response.GET(schoolService.existsSchool(minCode));
    }

    @GetMapping(EducGradTraxApiConstants.GET_SCHOOLS_BY_SCHOOL_CATEGORY_MAPPING)
    @PreAuthorize(PermissionsConstants.READ_SCHOOL_DATA)
    @Operation(summary = "Check school existence by Mincode", description = "Check school existence by Mincode", tags = { "School" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "204", description = "NO CONTENT")})
    public ResponseEntity<List<School>> getSchoolsBySchoolCategory(@RequestParam(required = false) String schoolCategory, @RequestHeader(name="Authorization") String accessToken) {
        return response.GET(schoolService.getSchoolsBySchoolCategory(schoolCategory, accessToken.replace(BEARER, "")));
    }
}
