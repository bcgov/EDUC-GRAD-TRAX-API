package ca.bc.gov.educ.api.trax.controller;

import ca.bc.gov.educ.api.trax.model.dto.*;
import ca.bc.gov.educ.api.trax.service.TraxCommonService;
import ca.bc.gov.educ.api.trax.util.*;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@CrossOrigin
@RestController
//@RequestMapping(EducGradTraxApiConstants.GRAD_TRAX_COMMON_URL_MAPPING)
@OpenAPIDefinition(info = @Info(title = "API for TRAX Data.", description = "This API is for TRAX.", version = "1"))
public class TraxCommonController {

    @Autowired
    TraxCommonService traxCommonService;

    @Autowired
    GradValidation validation;

    @Autowired
    ResponseHelper response;

    @GetMapping(EducGradTraxApiConstants.GRAD_TRAX_COMMON_URL_MAPPING_V1 + EducGradTraxApiConstants.GET_TRAX_STUDENT_DEMOG_MAPPING)
    @PreAuthorize(PermissionsConstants.READ_GRAD_TRAX_STUDENT_DATA)
    @Operation(summary = "Find Student Demographics from TRAX", description = "Find Student Demographics from TRAX", tags = {"Student"})
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),  @ApiResponse(responseCode = "400", description = "BAD REQUEST")})
    public ResponseEntity<List<Student>> getStudentDemographicsDataFromTrax(@PathVariable String pen) {
        log.debug("getStudentDemographicsDataFromTrax : ");
        validation.requiredField(pen, "Pen #");
        if (validation.hasErrors()) {
            validation.stopOnErrors();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return response.GET(traxCommonService.getStudentDemographicsDataFromTrax(pen));
    }

    @GetMapping(EducGradTraxApiConstants.GRAD_TRAX_COMMON_URL_MAPPING_V1 + EducGradTraxApiConstants.GET_TRAX_STUDENT_MASTER_MAPPING)
    @PreAuthorize(PermissionsConstants.READ_GRAD_TRAX_STUDENT_DATA)
    @Operation(summary = "Find Student Master Record from TRAX", description = "Find Student Master Record from TRAX", tags = {"Student"})
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),  @ApiResponse(responseCode = "400", description = "BAD REQUEST")})
    public ResponseEntity<List<ConvGradStudent>> getStudentMasterDataFromTrax(@PathVariable String pen) {
        log.debug("getStudentMasterDataFromTrax : ");
        validation.requiredField(pen, "Pen #");
        if (validation.hasErrors()) {
            validation.stopOnErrors();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return response.GET(traxCommonService.getStudentMasterDataFromTrax(pen));
    }

    @GetMapping(EducGradTraxApiConstants.GRAD_TRAX_COMMON_URL_MAPPING_V1 + EducGradTraxApiConstants.GET_TRAX_STUDENT_NO_LIST_BY_PAGING_MAPPING)
    @PreAuthorize(PermissionsConstants.READ_GRAD_TRAX_STUDENT_DATA)
    @Operation(summary = "Find Student Number List from TRAX", description = "Find Student Number List from TRAX", tags = {"Student"})
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<List<TraxStudentNo>> getTraxStudentNoListByPaging(
            @RequestParam(name = "pageNumber", defaultValue = "0") Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        log.debug("getTraxStudentNoListByPaging : pageNumber = {}, pageSize = {} ", pageNumber, pageSize);
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("studNo").ascending());
        return response.GET(traxCommonService.loadTraxStudentNoByPage(pageable));
    }

    @GetMapping(EducGradTraxApiConstants.GRAD_TRAX_COMMON_URL_MAPPING_V1 + EducGradTraxApiConstants.GET_TOTAL_NUMBER_OF_TRAX_STUDENT_NO_LIST_MAPPING)
    @PreAuthorize(PermissionsConstants.READ_GRAD_TRAX_STUDENT_DATA)
    @Operation(summary = "Find Student Master Record from TRAX", description = "Find Student Master Record from TRAX", tags = {"Student"})
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<Integer> getTotalNumberOfTraxStudentNoList() {
        log.debug("getTotalNumberOfTraxStudentNoList : ");
        return response.GET(traxCommonService.getTotalNumberOfTraxStudentNo());
    }

    @GetMapping(EducGradTraxApiConstants.GRAD_TRAX_COMMON_URL_MAPPING_V1 + EducGradTraxApiConstants.GET_COURSE_RESTRICTION_LIST_MAPPING)
    @PreAuthorize(PermissionsConstants.READ_GRAD_TRAX_COURSE_DATA)
    @Operation(summary = "Find Course Restrictions from TRAX", description = "Find Course Restrictions from TRAX", tags = {"Course"})
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<List<CourseRestriction>> getCourseRestrictions() {
        log.debug("getCourseRestrictions : ");
        return response.GET(traxCommonService.loadGradCourseRestrictionsDataFromTrax());
    }

    @GetMapping(EducGradTraxApiConstants.GRAD_TRAX_COMMON_URL_MAPPING_V1 + EducGradTraxApiConstants.GET_COURSE_REQUIREMENT_LIST_MAPPING)
    @PreAuthorize(PermissionsConstants.READ_GRAD_TRAX_COURSE_DATA)
    @Operation(summary = "Find Course Restrictions from TRAX", description = "Find Course Restrictions from TRAX", tags = {"Course"})
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<List<GradCourse>> getCourseRequirements() {
        log.debug("getCourseRequirements : ");
        return response.GET(traxCommonService.loadGradCourseRequirementsDataFromTrax());
    }

    @PostMapping(EducGradTraxApiConstants.GRAD_TRAX_COMMON_URL_MAPPING_V1 + EducGradTraxApiConstants.POST_SAVE_TRAX_STUDENT_NO_MAPPING)
    @PreAuthorize(PermissionsConstants.UPDATE_GRAD_TRAX_STUDENT_DATA)
    @Operation(summary = "Save TraxStudentNo", description = "Save TraxStudentNo", tags = {"Student"})
    public ResponseEntity<TraxStudentNo> saveTraxStudentNo(@RequestBody TraxStudentNo traxStudentNo) {
        log.debug("saveTraxStudentNo : ");
        return response.GET(traxCommonService.saveTraxStudentNo(traxStudentNo));
    }

    @DeleteMapping(EducGradTraxApiConstants.GRAD_TRAX_COMMON_URL_MAPPING_V1 + EducGradTraxApiConstants.DELETE_TRAX_STUDENT_NO_MAPPING)
    @PreAuthorize(PermissionsConstants.UPDATE_GRAD_TRAX_STUDENT_DATA)
    @Operation(summary = "Delete TraxStudentNo", description = "Delete TraxStudentNo ", tags = {"Student"})
    public ResponseEntity<TraxStudentNo> deleteTraxStudentNo(@PathVariable String pen) {
        log.debug("deleteTraxStudentNo : ");
        return response.GET(traxCommonService.deleteTraxStudentNo(pen));
    }
}
