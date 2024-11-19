package ca.bc.gov.educ.api.trax.controller.v2;

import ca.bc.gov.educ.api.trax.model.dto.institute.District;
import ca.bc.gov.educ.api.trax.service.institute.DistrictService;
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
@RestController("districtControllerV2")
@Slf4j
@OpenAPIDefinition(info = @Info(title = "API for School Data.", description = "This Read API is for Reading school data.", version = "2"),
        security = {@SecurityRequirement(name = "OAUTH2", scopes = {"READ_GRAD_SCHOOL_DATA"})})
public class DistrictController {

    DistrictService districtService;
    GradValidation validation;
    ResponseHelper response;

    @Autowired
    public DistrictController(DistrictService districtService, GradValidation validation, ResponseHelper response) {
        this.districtService = districtService;
        this.validation = validation;
        this.response = response;
    }

    @PutMapping(EducGradTraxApiConstants.GRAD_DISTRICT_URL_MAPPING_V2 + EducGradTraxApiConstants.PUT_DISTRICTS_MAPPING)
    @PreAuthorize(PermissionsConstants.UPDATE_GRAD_TRAX_CACHE)
    @Operation(summary = "Reload Districts in the cache", description = "Reload Districts in the cache", tags = {"Cache"})
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "422", description = "UNPROCESSABLE CONTENT")})
    public ResponseEntity<String> reloadDistrictsIntoCache() {
        log.debug("reloadDistrictsIntoCache : ");
        try {
            districtService.initializeDistrictCache(true);
        } catch (Exception e) {
            return ResponseEntity.unprocessableEntity().body("Error loading Districts into cache");
        }
        return ResponseEntity.ok("Districts loaded into cache!");
    }

    @GetMapping(EducGradTraxApiConstants.GRAD_DISTRICT_URL_MAPPING_V2)
    @PreAuthorize(PermissionsConstants.READ_SCHOOL_DATA)
    @Operation(summary = "Find a District by District Number V2", description = "Get District by District Number V2", tags = { "District" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<District> getDistrictDetailsByDistNo(@RequestParam(required = true) String distNo) {
        if(distNo.length() <=3) {
            District distResponse = districtService.getDistrictByDistNoFromRedisCache(distNo);
            if (distResponse != null) {
                return response.GET(distResponse);
            }
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping(EducGradTraxApiConstants.GRAD_DISTRICT_URL_MAPPING_V2 + EducGradTraxApiConstants.GET_DISTRICT_BY_ID_MAPPING)
    @PreAuthorize(PermissionsConstants.READ_SCHOOL_DATA)
    @Operation(summary = "Find a District by ID V2", description = "Get District by ID V2", tags = { "District" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<District> getDistrictDetailsById(@PathVariable String districtId) {
        District distResponse = districtService.getDistrictByIdFromRedisCache(districtId);
        if (distResponse != null) {
            return response.GET(distResponse);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping(EducGradTraxApiConstants.GRAD_DISTRICT_URL_MAPPING_V2 + EducGradTraxApiConstants.GET_DISTRICTS_BY_SCHOOL_CATEGORY_MAPPING)
    @PreAuthorize(PermissionsConstants.READ_SCHOOL_DATA)
    @Operation(summary = "Get District by school category code V2", description = "Get District by school category code V2", tags = { "District" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "204", description = "NO CONTENT")})
    public ResponseEntity<List<District>> getDistrictsBySchoolCategoryCode(@RequestParam(required = false) String schoolCategoryCode) {
        return response.GET(districtService.getDistrictsBySchoolCategoryCode(schoolCategoryCode));
    }

}
