package ca.bc.gov.educ.api.trax.controller;

import ca.bc.gov.educ.api.trax.model.dto.GradCountry;
import ca.bc.gov.educ.api.trax.model.dto.GradProvince;
import ca.bc.gov.educ.api.trax.service.CodeService;
import ca.bc.gov.educ.api.trax.util.EducGradTraxApiConstants;
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

@Slf4j
@RestController
@CrossOrigin
@OpenAPIDefinition(info = @Info(title = "API for TRAX Code Tables Data.",
        description = "This API is for Reading TRAX Code Tables data.", version = "1"),
        security = {@SecurityRequirement(name = "OAUTH2",
                scopes = {"READ_GRAD_COUNTRY_CODE_DATA",
                        "READ_GRAD_PROVINCE_CODE_DATA",
                })})
public class CodeController {

    @Autowired
    CodeService codeService;
    @Autowired
    ResponseHelper response;

    @GetMapping(EducGradTraxApiConstants.GRAD_TRAX_CODE_URL_MAPPING_V1 + EducGradTraxApiConstants.GET_ALL_COUNTRY_MAPPING)
    @PreAuthorize(PermissionsConstants.READ_GRAD_COUNTRY)
    @Operation(summary = "Find All Countries", description = "Get All Countries", tags = {"Country"})
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<List<GradCountry>> getAllCountryCodeList() {
        log.debug("getAllCountryCodeList : ");
        return response.GET(codeService.getAllCountryCodeList());
    }

    @GetMapping(EducGradTraxApiConstants.GRAD_TRAX_CODE_URL_MAPPING_V1 + EducGradTraxApiConstants.GET_ALL_COUNTRY_BY_CODE_MAPPING)
    @PreAuthorize(PermissionsConstants.READ_GRAD_COUNTRY)
    @Operation(summary = "Find a Country by Code", description = "Get a Country by Country Code", tags = {"Country"})
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "204", description = "NO CONTENT.")})
    public ResponseEntity<GradCountry> getSpecificCountryCode(@PathVariable String countryCode) {
        log.debug("getSpecificCountryCode : ");
        GradCountry gradResponse = codeService.getSpecificCountryCode(countryCode);
        if (gradResponse != null) {
            return response.GET(gradResponse);
        } else {
            return response.NO_CONTENT();
        }
    }

    @GetMapping(EducGradTraxApiConstants.GRAD_TRAX_CODE_URL_MAPPING_V1 + EducGradTraxApiConstants.GET_ALL_PROVINCE_MAPPING)
    @PreAuthorize(PermissionsConstants.READ_GRAD_PROVINCE)
    @Operation(summary = "Find All Provinces", description = "Get All Provinces", tags = {"Province"})
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<List<GradProvince>> getAllProvinceCodeList() {
        log.debug("getAllProvinceCodeList : ");
        return response.GET(codeService.getAllProvinceCodeList());
    }

    @GetMapping(EducGradTraxApiConstants.GRAD_TRAX_CODE_URL_MAPPING_V1 + EducGradTraxApiConstants.GET_ALL_PROVINCE_BY_CODE_MAPPING)
    @PreAuthorize(PermissionsConstants.READ_GRAD_PROVINCE)
    @Operation(summary = "Find a Province by Province Code", description = "Get a Province by Province Code", tags = {"Province"})
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "204", description = "NO CONTENT.")})
    public ResponseEntity<GradProvince> getSpecificProvinceCode(@PathVariable String provinceCode) {
        log.debug("getSpecificProvinceCode : ");
        GradProvince gradResponse = codeService.getSpecificProvinceCode(provinceCode);
        if (gradResponse != null) {
            return response.GET(gradResponse);
        } else {
            return response.NO_CONTENT();
        }
    }

}
