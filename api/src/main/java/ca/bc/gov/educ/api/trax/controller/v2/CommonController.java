package ca.bc.gov.educ.api.trax.controller.v2;

import ca.bc.gov.educ.api.trax.model.dto.School;
import ca.bc.gov.educ.api.trax.service.institute.CommonService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@CrossOrigin
@RestController
@Slf4j
@OpenAPIDefinition(info = @Info(title = "API for School Clob for Graduation Algorithm Data.", description = "This Common API is for retrieving School Clob for Graduation Algorithm Data from Redis Cache.", version = "2"),
        security = {@SecurityRequirement(name = "OAUTH2", scopes = {"READ_GRAD_SCHOOL_DATA"})})
public class CommonController {
    CommonService commonService;
    GradValidation validation;
    ResponseHelper response;

    @Autowired
    public CommonController(CommonService commonService, GradValidation validation, ResponseHelper response) {
        this.commonService = commonService;
        this.validation = validation;
        this.response = response;
    }

    @GetMapping(EducGradTraxApiConstants.GRAD_SCHOOL_CLOB_URL_MAPPING_V2)
    @PreAuthorize(PermissionsConstants.READ_SCHOOL_DATA)
    @Operation(summary = "Find All Schools Clob data for GRAD Algorithm Data from cache", description = "Get All Schools Clob data for GRAD Algorithm Data from cache", tags = { "Algorithm Data" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<List<School>> getAllSchoolsForClobData() {
        log.debug("getAllSchoolsClob : ");
        return response.GET(commonService.getSchoolsForClobDataFromRedisCache());
    }

    @GetMapping(EducGradTraxApiConstants.GRAD_SCHOOL_CLOB_URL_MAPPING_V2 + EducGradTraxApiConstants.GET_SCHOOL_BY_SCHOOL_ID)
    @PreAuthorize(PermissionsConstants.READ_SCHOOL_DATA)
    @Operation(summary = "Find a School Clob data by SchoolId for GRAD Algorithm Data from cache", description = "Get a School Clob data by SchoolId for GRAD Algorithm Data from cache", tags = { "Algorithm Data" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "422", description = "UNPROCESSABLE CONTENT"),
            @ApiResponse(responseCode = "204", description = "NO CONTENT")})
    public ResponseEntity<School> getSchoolForClobDataBySchoolId(@PathVariable UUID schoolId) {
        log.debug("getSchoolClobData by schoolId: {}", schoolId);
        validation.requiredField(schoolId, "schoolId");
        if (validation.hasErrors()) {
            validation.stopOnErrors();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        School schoolResponse = commonService.getSchoolForClobDataBySchoolIdFromRedisCache(schoolId);
        if (schoolResponse != null) {
            return response.GET(schoolResponse);
        } else {
            return response.NOT_FOUND();
        }
    }

    @GetMapping(EducGradTraxApiConstants.GRAD_SCHOOL_CLOB_URL_MAPPING_V2 + EducGradTraxApiConstants.GET_SCHOOL_SEARCH_MAPPING)
    @PreAuthorize(PermissionsConstants.READ_SCHOOL_DATA)
    @Operation(summary = "Find a School Clob data by MinCode for GRAD Algorithm Data from cache", description = "Get a School Clob data by MinCode for GRAD Algorithm Data from cache", tags = { "Algorithm Data" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "422", description = "UNPROCESSABLE CONTENT"),
            @ApiResponse(responseCode = "204", description = "NO CONTENT")})
    public ResponseEntity<School> getSchoolForClobDataByMinCode(@RequestParam(value = "mincode", required = false) String mincode) {
        log.debug("getSchoolClobData by minCode: {}", mincode);
        return response.GET(commonService.getSchoolForClobDataByMinCodeFromRedisCache(mincode));

    }

    @GetMapping(EducGradTraxApiConstants.GRAD_SCHOOLS_BY_DISTRICT_URL_MAPPING_V2 + EducGradTraxApiConstants.GET_DISTRICT_BY_DISTNO_MAPPING)
    @PreAuthorize(PermissionsConstants.READ_SCHOOL_DATA)
    @Operation(summary = "Find Schools Clob data by District Number for GRAD Algorithm Data from cache", description = "Get Schools Clob data by District Number for GRAD Algorithm Data from cache", tags = { "Algorithm Data" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<List<School>> getSchoolsForClobDataByDistrictNumber(@PathVariable String distNo) {
        log.debug("getSchoolsClob by districtNumber: {}", distNo);
        return response.GET(commonService.getSchoolsByDistrictNumberFromRedisCache(distNo));
    }

}
