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
    public List<School> getAllSchoolsForClobData() {
        log.debug("getAllSchoolsClob : ");
        return commonService.getSchoolsForClobDataFromRedisCache();
    }

    @GetMapping(EducGradTraxApiConstants.GRAD_SCHOOL_CLOB_URL_MAPPING_V2 + EducGradTraxApiConstants.GET_SCHOOL_BY_CODE_MAPPING)
    @PreAuthorize(PermissionsConstants.READ_SCHOOL_DATA)
    @Operation(summary = "Find a School Clob data by MinCode for GRAD Algorithm Data from cache", description = "Get a School Clob data by MinCode for GRAD Algorithm Data from cache", tags = { "Algorithm Data" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "422", description = "UNPROCESSABLE CONTENT"),
            @ApiResponse(responseCode = "204", description = "NO CONTENT")})
    public ResponseEntity<School> getSchoolForClobDataByMinCode(@PathVariable String minCode) {
        log.debug("getSchoolClobData : {}", minCode);
        validation.requiredField(minCode, "minCode");
        if (validation.hasErrors()) {
            validation.stopOnErrors();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        School schoolResponse = commonService.getSchoolForClobDataByMinCodeFromRedisCache(minCode);
        if (schoolResponse != null) {
            return response.GET(schoolResponse);
        } else {
            return response.NOT_FOUND();
        }
    }
}
