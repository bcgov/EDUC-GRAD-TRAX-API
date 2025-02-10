package ca.bc.gov.educ.api.trax.controller.v2;

import ca.bc.gov.educ.api.trax.model.dto.institute.School;
import ca.bc.gov.educ.api.trax.model.dto.institute.SchoolDetail;
import ca.bc.gov.educ.api.trax.service.institute.SchoolService;
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
import java.util.Optional;
import java.util.UUID;

@CrossOrigin
@RestController("schoolControllerV2")
@Slf4j
@OpenAPIDefinition(info = @Info(title = "API for School Data.", description = "This Read API is for Reading school data from Redis Cache.", version = "2"),
		security = {@SecurityRequirement(name = "OAUTH2", scopes = {"READ_GRAD_SCHOOL_DATA"})})
public class SchoolController {

    SchoolService schoolService;
    GradValidation validation;
	ResponseHelper response;

    @Autowired
    public SchoolController(SchoolService schoolService, GradValidation validation, ResponseHelper response) {
        this.schoolService = schoolService;
        this.validation = validation;
        this.response = response;
    }

    @PutMapping(EducGradTraxApiConstants.GRAD_SCHOOL_URL_MAPPING_V2 + EducGradTraxApiConstants.PUT_SCHOOLS_MAPPING)
    @PreAuthorize(PermissionsConstants.UPDATE_GRAD_TRAX_CACHE)
    @Operation(summary = "Reload Schools in the cache", description = "Reload Schools in the cache", tags = {"Cache"})
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "422", description = "UNPROCESSABLE CONTENT")})
    public ResponseEntity<String> reloadSchoolsIntoCache() {
        log.debug("reloadSchoolsIntoCache : ");
        try {
            schoolService.initializeSchoolCache(true);
        } catch (Exception e) {
            return ResponseEntity.unprocessableEntity().body("Error loading Schools into cache");
        }
        return ResponseEntity.ok("Schools loaded into cache!");
    }

    @GetMapping(EducGradTraxApiConstants.GRAD_SCHOOL_URL_MAPPING_V2)
    @PreAuthorize(PermissionsConstants.READ_SCHOOL_DATA)
    @Operation(summary = "Find All Schools from Cache", description = "Get All Schools from Cache", tags = { "School" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public List<School> getAllSchools() {
    	log.debug("getAllSchools V2 : ");
        return schoolService.getSchoolsFromRedisCache();
    }

    @GetMapping(EducGradTraxApiConstants.GRAD_SCHOOL_URL_MAPPING_V2 + EducGradTraxApiConstants.GET_SCHOOL_BY_SCHOOL_ID)
    @PreAuthorize(PermissionsConstants.READ_SCHOOL_DATA)
    @Operation(summary = "Find a School by schoolId from cache", description = "Get a School by schoolId from cache", tags = { "School" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "204", description = "NO CONTENT")})
    public ResponseEntity<School> getSchoolBySchoolId(@PathVariable UUID schoolId) {
        log.debug("getSchoolBySchoolId V2 : ");
        Optional<School> schoolResponse = schoolService.getSchoolBySchoolId(schoolId);
        if(schoolResponse.isPresent()) {
            return response.GET(schoolResponse.get());
        }else {
            return response.NOT_FOUND();
        }
    }

    @PutMapping(EducGradTraxApiConstants.GRAD_SCHOOL_URL_MAPPING_V2 + EducGradTraxApiConstants.PUT_SCHOOL_DETAILS_MAPPING)
    @PreAuthorize(PermissionsConstants.UPDATE_GRAD_TRAX_CACHE)
    @Operation(summary = "Reload School Details in the cache", description = "Reload School Details in the cache", tags = {"Cache"})
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "422", description = "UNPROCESSABLE CONTENT")})
    public ResponseEntity<String> reloadSchoolDetailsIntoCache() {
        log.debug("reloadSchoolDetailsIntoCache : ");
        try {
            schoolService.initializeSchoolDetailCache(true);
        } catch (Exception e) {
            return ResponseEntity.unprocessableEntity().body("Error loading School Details into cache");
        }
        return ResponseEntity.ok("School Details loaded into cache!");
    }

    @GetMapping(EducGradTraxApiConstants.GRAD_SCHOOL_DETAIL_URL_MAPPING_V2)
    @PreAuthorize(PermissionsConstants.READ_SCHOOL_DATA)
    @Operation(summary = "Find All School details from Cache", description = "Get All School details from Cache", tags = { "School" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public List<SchoolDetail> getAllSchoolDetails() {
        log.debug("getAllSchoolDetails V2: ");
        return schoolService.getSchoolDetailsFromRedisCache();
    }

    @GetMapping(EducGradTraxApiConstants.GRAD_SCHOOL_URL_MAPPING_V2 + EducGradTraxApiConstants.CHECK_SCHOOL_BY_CODE_MAPPING)
    @PreAuthorize(PermissionsConstants.READ_SCHOOL_DATA)
    @Operation(summary = "Check school existence by Mincode V2", description = "Check school existence by Mincode V2", tags = { "School" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "204", description = "NO CONTENT")})
    public Boolean checkIfSchoolExists(@PathVariable String minCode) {
        return schoolService.checkIfSchoolExists(minCode);
    }

    @GetMapping(EducGradTraxApiConstants.GRAD_SCHOOL_URL_MAPPING_V2 + EducGradTraxApiConstants.GET_SCHOOLS_BY_SCHOOL_CATEGORY_MAPPING)
    @PreAuthorize(PermissionsConstants.READ_SCHOOL_DATA)
    @Operation(summary = "Get Schools by School Category Code V2", description = "Get Schools by School Category Code V2", tags = { "School" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "204", description = "NO CONTENT")})
    public ResponseEntity<List<SchoolDetail>> getSchoolsBySchoolCategory(@RequestParam(required = false) String schoolCategoryCode) {
        return response.GET(schoolService.getSchoolDetailsBySchoolCategoryCode(schoolCategoryCode));
    }

    @GetMapping(EducGradTraxApiConstants.GRAD_SCHOOL_DETAIL_URL_MAPPING_V2 + EducGradTraxApiConstants.GET_SCHOOL_BY_SCHOOL_ID)
    @PreAuthorize(PermissionsConstants.READ_SCHOOL_DATA)
    @Operation(summary = "Find School Details by ID from cache", description = "Get School Details by ID from cache", tags = { "School" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "204", description = "NO CONTENT")})
    public ResponseEntity<SchoolDetail> getSchoolDetailsById(@PathVariable UUID schoolId) {
        log.debug("getSchoolDetailsById V2 : ");
        SchoolDetail schoolDetailResponse = schoolService.getSchoolDetailBySchoolId(schoolId);
        if(schoolDetailResponse != null) {
            return response.GET(schoolDetailResponse);
        }else {
            return response.NOT_FOUND();
        }
    }

    @GetMapping(EducGradTraxApiConstants.GRAD_SCHOOL_DETAIL_URL_MAPPING_V2 + EducGradTraxApiConstants.GET_SCHOOL_DETAIL_SEARCH_MAPPING)
    @PreAuthorize(PermissionsConstants.READ_SCHOOL_DATA)
    @Operation(summary = "Find School Details by Mincode from cache", description = "Get School Details by Mincode from cache", tags = { "School" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "204", description = "NO CONTENT")})
    public ResponseEntity<SchoolDetail> getSchoolDetailsByParams(@RequestParam(required = false) String minCode) {
        log.debug("getSchoolDetailsByParams V2 : ");
        SchoolDetail schoolDetailResponse = schoolService.getSchoolDetailByMincodeFromRedisCache(minCode);
        if(schoolDetailResponse != null) {
            return response.GET(schoolDetailResponse);
        }else {
            return response.NOT_FOUND();
        }
    }

    /**
     * School wildcard Search with given params
     * @param districtId
     * @param mincode
     * @param displayName
     * @param distNo
     * @return
     */
    @GetMapping(EducGradTraxApiConstants.GRAD_SCHOOL_URL_MAPPING_V2 + EducGradTraxApiConstants.GET_SCHOOL_SEARCH_MAPPING)
    @PreAuthorize(PermissionsConstants.READ_SCHOOL_DATA)
    @Operation(summary = "Search for a school v2", description = "Search for a School v2", tags = { "School" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST")})
    public ResponseEntity<List<School>> getSchoolsByParams(
            @RequestParam(value = "districtId", required = false) String districtId,
            @RequestParam(value = "mincode", required = false) String mincode,
            @RequestParam(value = "displayName", required = false) String displayName,
            @RequestParam(value = "distNo", required = false) String distNo,
            @RequestParam(value = "schoolCategoryCodes", required = false) List<String> schoolCategoryCodes)
    {
        log.debug("getSchoolsByParams V2 : ");
        return response.GET(schoolService.getSchoolsByParams(districtId, mincode, displayName, distNo, schoolCategoryCodes));
    }

}
