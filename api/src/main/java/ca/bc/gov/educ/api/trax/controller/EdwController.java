package ca.bc.gov.educ.api.trax.controller;

import ca.bc.gov.educ.api.trax.model.dto.SnapshotResponse;
import ca.bc.gov.educ.api.trax.service.EdwService;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@CrossOrigin
@RestController
//@RequestMapping(EducGradTraxApiConstants.GRAD_EDW_URL_MAPPING)
@OpenAPIDefinition(info = @Info(title = "API for EDW Snapshot.", description = "This Read API is for Reading EDW Snapshot.", version = "1"),
		security = {@SecurityRequirement(name = "OAUTH2", scopes = {"READ_GRAD_TRAX_STUDENT_DATA"})})
public class EdwController {

    private static final String GRAD_YEAR_PARAM = "GradYear";
    private static final String SCHOOL_PARAM = "MinCode";

    @Autowired
    EdwService edwService;
    
    @Autowired
    GradValidation validation;
    
    @Autowired
	ResponseHelper response;

    @GetMapping(EducGradTraxApiConstants.GRAD_EDW_URL_MAPPING_V1 + EducGradTraxApiConstants.GET_SCHOOLS_BY_GRAD_YEAR_MAPPING)
    @PreAuthorize(PermissionsConstants.READ_GRAD_TRAX_STUDENT_DATA)
    @Operation(summary = "Get unique schools from snapshot by gradYear", description = "Find unique schools from snapshot by gradYear", tags = { "EDW" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST")})
    public ResponseEntity<List<String>> getSchoolListFromSnapshotByGradYear(@PathVariable Integer gradYear) {
        log.debug("getSchoolListFromSnapshotByGradYear : ");
        validation.requiredField(gradYear, GRAD_YEAR_PARAM);
        if (validation.hasErrors()) {
            validation.stopOnErrors();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return response.GET(edwService.getUniqueSchoolList(gradYear));
    }

    @GetMapping(EducGradTraxApiConstants.GRAD_EDW_URL_MAPPING_V1 + EducGradTraxApiConstants.GET_STUDENTS_BY_GRAD_YEAR_MAPPING)
    @PreAuthorize(PermissionsConstants.READ_GRAD_TRAX_STUDENT_DATA)
    @Operation(summary = "Get all students from snapshot by gradYear", description = "Find all students from snapshot by gradYear", tags = { "EDW" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST")})
    public ResponseEntity<List<SnapshotResponse>> getStudentsFromSnapshotByGradYear(@PathVariable Integer gradYear) {
        log.debug("getStudentsFromSnapshotByGradYear : ");
        validation.requiredField(gradYear, GRAD_YEAR_PARAM);
        if (validation.hasErrors()) {
            validation.stopOnErrors();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return response.GET(edwService.getStudents(gradYear));
    }

    @GetMapping(EducGradTraxApiConstants.GRAD_EDW_URL_MAPPING_V1 + EducGradTraxApiConstants.GET_STUDENTS_BY_GRAD_YEAR_AND_SCHOOL_MAPPING)
    @PreAuthorize(PermissionsConstants.READ_GRAD_TRAX_STUDENT_DATA)
    @Operation(summary = "Get students from snapshot by gradYear & minCode", description = "Find students from snapshot by gradYear & minCode", tags = { "EDW" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST")})
    public ResponseEntity<List<SnapshotResponse>> getStudentsFromSnapshotByGradYearAndSchool(@PathVariable Integer gradYear, @PathVariable String minCode) {
        log.debug("getStudentsFromSnapshotByGradYearAndSchool : ");
        validation.requiredField(gradYear, GRAD_YEAR_PARAM);
        validation.requiredField(minCode, SCHOOL_PARAM);
        if (validation.hasErrors()) {
            validation.stopOnErrors();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return response.GET(edwService.getStudents(gradYear, minCode));
    }

}
