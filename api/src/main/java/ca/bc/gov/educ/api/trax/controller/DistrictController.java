package ca.bc.gov.educ.api.trax.controller;

import ca.bc.gov.educ.api.trax.model.dto.District;
import ca.bc.gov.educ.api.trax.service.DistrictService;
import ca.bc.gov.educ.api.trax.util.EducGradTraxApiConstants;
import ca.bc.gov.educ.api.trax.util.PermissionsConstants;
import ca.bc.gov.educ.api.trax.util.ResponseHelper;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
//@RequestMapping(EducGradTraxApiConstants.GRAD_DISTRICT_URL_MAPPING)
@OpenAPIDefinition(info = @Info(title = "API for School Data.", description = "This Read API is for Reading school data.", version = "1"),
		security = {@SecurityRequirement(name = "OAUTH2", scopes = {"READ_GRAD_SCHOOL_DATA"})})
public class DistrictController {

    @Autowired
    DistrictService districtService;
    @Autowired
	ResponseHelper response;

    
    @GetMapping(EducGradTraxApiConstants.GRAD_DISTRICT_URL_MAPPING_V1 + EducGradTraxApiConstants.GET_DISTRICT_BY_DISTNO_MAPPING)
    @PreAuthorize(PermissionsConstants.READ_SCHOOL_DATA)
    @Operation(summary = "Find a District by District Number", description = "Get District by District Number", tags = { "District" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<District> getDistrictDetails(@PathVariable String distCode) {
    	if(distCode.length() <=3) {
            District distResponse = districtService.getDistrictDetails(distCode);
            if (distResponse != null) {
                return response.GET(distResponse);
            }
        }
        return null;
    }

    @GetMapping(EducGradTraxApiConstants.GRAD_DISTRICT_URL_MAPPING_V1 + EducGradTraxApiConstants.GET_DISTRICTS_BY_SCHOOL_CATEGORY_MAPPING)
    @PreAuthorize(PermissionsConstants.READ_SCHOOL_DATA)
    @Operation(summary = "Check school existence by Mincode", description = "Check school existence by Mincode", tags = { "School" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "204", description = "NO CONTENT")})
    public ResponseEntity<List<District>> getDistrictBySchoolCategory(@RequestParam(required = false) String schoolCategory) {
        return response.GET(districtService.getDistrictBySchoolCategory(schoolCategory));
    }
}
