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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController("SchoolControllerV2")
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

    @GetMapping(EducGradTraxApiConstants.GRAD_SCHOOL_URL_MAPPING_V2)
    @PreAuthorize(PermissionsConstants.READ_SCHOOL_DATA)
    @Operation(summary = "Find All Schools from Cache", description = "Get All Schools from Cache", tags = { "School" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public List<School> getAllSchools() {
    	log.debug("getAllSchools : ");
        return schoolService.getSchoolsFromRedisCache();
    }

    @GetMapping(EducGradTraxApiConstants.GRAD_SCHOOL_DETAIL_URL_MAPPING_V2)
    @PreAuthorize(PermissionsConstants.READ_SCHOOL_DATA)
    @Operation(summary = "Find All School details from Cache", description = "Get All School details from Cache", tags = { "School" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public List<SchoolDetail> getAllSchoolDetails() {
        log.debug("getAllSchoolDetails : ");
        return schoolService.getSchoolDetailsFromRedisCache();
    }

    @GetMapping(EducGradTraxApiConstants.GRAD_SCHOOL_URL_MAPPING_V2 + "/inst")
    @PreAuthorize(PermissionsConstants.READ_SCHOOL_DATA)
    @Operation(summary = "Find All Schools from institute api", description = "Get All Schools from institute api", tags = { "School" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public List<School> getAllSchoolsFromInstituteApi() {
        log.debug("getAllSchools : ");
        return schoolService.getSchoolsFromInstituteApi();
    }

}
