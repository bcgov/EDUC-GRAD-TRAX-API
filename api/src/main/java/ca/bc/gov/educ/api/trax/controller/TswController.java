package ca.bc.gov.educ.api.trax.controller;

import ca.bc.gov.educ.api.trax.model.dto.TranscriptStudentDemog;
import ca.bc.gov.educ.api.trax.service.TswService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping(EducGradTraxApiConstants.GRAD_TSW_URL_MAPPING)
@OpenAPIDefinition(info = @Info(title = "API for TSW Data.", description = "This Read API is for Reading school data.", version = "1"),
		security = {@SecurityRequirement(name = "OAUTH2", scopes = {"READ_GRAD_TRAX_STUDENT_DATA"})})
public class TswController {

    private static Logger logger = LoggerFactory.getLogger(TswController.class);

    @Autowired
    TswService tswService;
    
    @Autowired
    GradValidation validation;
    
    @Autowired
	ResponseHelper response;
    
    @GetMapping(EducGradTraxApiConstants.GET_TRANSCRIPT_DEMOG_BY_PEN_MAPPING)
    @PreAuthorize(PermissionsConstants.READ_GRAD_TRAX_STUDENT_DATA)
    @Operation(summary = "Get transcript student demographics data from TSW", description = "Find a transcript student demographics data", tags = { "TSW" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "400", description = "BAD REQUEST")})
    public ResponseEntity<TranscriptStudentDemog> getTranscriptStudentDemogByPen(@PathVariable String pen) {
        logger.debug("getTranscriptStudentDemogByPen : ");
        validation.requiredField(pen, "Pen #");
        if (validation.hasErrors()) {
            validation.stopOnErrors();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
		return response.GET(tswService.getTranscriptStudentDemog(pen));
    }

    @GetMapping(EducGradTraxApiConstants.GET_TRANSCRIPT_STUDENT_GRADUATED_BY_PEN_MAPPING)
    @PreAuthorize(PermissionsConstants.READ_GRAD_TRAX_STUDENT_DATA)
    @Operation(summary = "Get student is graduated or not from TSW", description = "Find a student graduated or not", tags = { "TSW" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST")})
    public ResponseEntity<Boolean> getTranscriptStudentGraduatedByPen(@PathVariable String pen) {
        logger.debug("getTranscriptStudentGraduatedByPen : ");
        validation.requiredField(pen, "Pen #");
        if (validation.hasErrors()) {
            validation.stopOnErrors();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return response.GET(tswService.isGraduated(pen));
    }


}
