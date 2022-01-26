package ca.bc.gov.educ.api.trax.controller;

import ca.bc.gov.educ.api.trax.model.dto.District;
import ca.bc.gov.educ.api.trax.model.dto.School;
import ca.bc.gov.educ.api.trax.service.DistrictService;
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
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@EnableResourceServer
@RequestMapping(EducGradTraxApiConstants.GRAD_DISTRICT_URL_MAPPING)
@OpenAPIDefinition(info = @Info(title = "API for School Data.", description = "This Read API is for Reading school data.", version = "1"),
		security = {@SecurityRequirement(name = "OAUTH2", scopes = {"READ_GRAD_SCHOOL_DATA"})})
public class DistrictController {

    private static Logger logger = LoggerFactory.getLogger(DistrictController.class);

    @Autowired
    DistrictService districtService;
    
    @Autowired
    GradValidation validation;
    
    @Autowired
	ResponseHelper response;

    
    @GetMapping(EducGradTraxApiConstants.GET_DISTRICT_BY_DISTNO_MAPPING)
    @PreAuthorize(PermissionsConstants.READ_SCHOOL_DATA)
    @Operation(summary = "Find a District by District Number", description = "Get District by District Number", tags = { "District" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "204", description = "NO CONTENT")})
    public ResponseEntity<District> getDistrictDetails(@PathVariable String distCode) {
    	District distResponse = districtService.getDistrictDetails(distCode);
    	if(distResponse != null) {
    		return response.GET(distResponse);
    	}else {
    		return response.NOT_FOUND();
    	}
    }
}
