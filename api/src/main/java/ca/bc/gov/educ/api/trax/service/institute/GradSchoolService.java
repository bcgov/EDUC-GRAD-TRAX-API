package ca.bc.gov.educ.api.trax.service.institute;

import ca.bc.gov.educ.api.trax.exception.ServiceException;
import ca.bc.gov.educ.api.trax.model.dto.GradSchool;
import ca.bc.gov.educ.api.trax.service.RESTService;
import ca.bc.gov.educ.api.trax.util.EducGradTraxApiConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Service for handling operations related to Grad Schools.
 */
@Slf4j
@Service
public class GradSchoolService {

    private final RESTService restService;
    private final EducGradTraxApiConstants constants;
    private final WebClient gradSchoolWebClient;

    public GradSchoolService(RESTService restService, EducGradTraxApiConstants constants, @Qualifier("gradSchoolApiClient") WebClient gradSchoolWebClient) {
        this.restService = restService;
        this.constants = constants;
        this.gradSchoolWebClient = gradSchoolWebClient;
    }

    public GradSchool getGradSchoolBySchoolId(String schoolId) throws ServiceException {
        log.trace("getGradSchoolBySchoolId() called with schoolId: {}", schoolId);
        return this.restService.get(String.format(constants.getSchoolGradDetailsByIdFromGradSchoolApiUrl(), schoolId),
                GradSchool.class, gradSchoolWebClient);
    }

    public boolean isGradSchoolTranscriptIssuer(String schoolId) throws ServiceException {
        log.trace("isGradSchoolTranscriptIssuer() called with schoolId: {}", schoolId);
        GradSchool gradSchool = this.getGradSchoolBySchoolId(schoolId);
        return gradSchool.getCanIssueTranscripts().equalsIgnoreCase("Y");
    }
}
