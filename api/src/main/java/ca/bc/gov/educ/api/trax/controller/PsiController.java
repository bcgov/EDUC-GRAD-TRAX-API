package ca.bc.gov.educ.api.trax.controller;

import ca.bc.gov.educ.api.trax.model.dto.Psi;
import ca.bc.gov.educ.api.trax.model.dto.StudentPsi;
import ca.bc.gov.educ.api.trax.service.PsiService;
import ca.bc.gov.educ.api.trax.util.EducGradTraxApiConstants;
import ca.bc.gov.educ.api.trax.util.GradValidation;
import ca.bc.gov.educ.api.trax.util.PermissionsConstants;
import ca.bc.gov.educ.api.trax.util.ResponseHelper;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping(EducGradTraxApiConstants.GRAD_PSI_URL_MAPPING)
@OpenAPIDefinition(info = @Info(title = "API for PSI Data.", description = "This API is for PSI.", version = "1"))
public class PsiController {

    private static Logger logger = LoggerFactory.getLogger(PsiController.class);

    @Autowired
    PsiService psiService;
    
    @Autowired
	GradValidation validation;
    
    @Autowired
	ResponseHelper response;

    @GetMapping
    @PreAuthorize(PermissionsConstants.READ_PSI_INFO)
    @Operation(summary = "Find All PSIs", description = "Get All PSIs", tags = { "PSI" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<List<Psi>> getAllPSIs() { 
    	logger.debug("getAllPSIs : ");
        return response.GET(psiService.getPSIList());
    }
    
    @GetMapping(EducGradTraxApiConstants.GET_PSI_BY_CODE_MAPPING)
    @PreAuthorize(PermissionsConstants.READ_PSI_INFO)
    @Operation(summary = "Find a PSI by Code", description = "Get a PSI by Code", tags = { "PSI" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),@ApiResponse(responseCode = "400", description = "BAD REQUEST")})
    public ResponseEntity<Psi> getPSIDetails(@PathVariable String psiCode) { 
    	logger.debug("getPSIDetails : ");
        return response.GET(psiService.getPSIDetails(psiCode));
    }
    
    @GetMapping(EducGradTraxApiConstants.GET_PSI_SEARCH_MAPPING)
    @PreAuthorize(PermissionsConstants.READ_PSI_INFO)
    @Operation(summary = "Search for PSIs", description = "Search For PSIs", tags = { "PSI" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<List<Psi>> getPSIByParams(
    		@RequestParam(value = "psiName", required = false) String psiName,
    		@RequestParam(value = "psiCode", required = false) String psiCode,
    		@RequestParam(value = "cslCode", required = false) String cslCode,
    		@RequestParam(value = "transmissionMode", required = false) String transmissionMode,
            @RequestParam(value = "openFlag", required = false) String openFlag) {
		return response.GET(psiService.getPSIByParams(psiName,psiCode,cslCode,transmissionMode,openFlag));
	}

    @GetMapping(EducGradTraxApiConstants.GET_STUDENT_PSI_BY_CODE_MAPPING)
    @PreAuthorize(PermissionsConstants.READ_PSI_INFO)
    @Operation(summary = "Find a PSI by Code", description = "Get a PSI by Code", tags = { "PSI" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),@ApiResponse(responseCode = "400", description = "BAD REQUEST")})
    public ResponseEntity<List<StudentPsi>> getStudentPSIDetails(
            @RequestParam(value = "transmissionMode") String transmissionMode,
            @RequestParam(value = "psiCode") String psiCode,
            @RequestParam(value = "psiYear") String psiYear) {
        logger.debug("getStudentPSIDetails : ");
        return response.GET(psiService.getStudentPSIDetails(transmissionMode,psiYear,psiCode));
    }
}
