package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.exception.ServiceException;
import ca.bc.gov.educ.api.trax.model.dto.GradSchool;
import ca.bc.gov.educ.api.trax.service.institute.GradSchoolService;
import ca.bc.gov.educ.api.trax.support.TestUtils;
import ca.bc.gov.educ.api.trax.util.EducGradTraxApiConstants;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.reactive.function.client.WebClient;
import static org.mockito.Mockito.when;

public class GradSchoolServiceTest extends BaseReplicationServiceTest {

    @Autowired
    private GradSchoolService gradSchoolService;
    @Autowired
    private EducGradTraxApiConstants constants;
    @MockBean
    private RESTService restService;
    @MockBean
    private @Qualifier("gradSchoolApiClient") WebClient gradSchoolWebClient;

    @Test
    public void testGetGradSchoolBySchoolId_givenValidSchoolId_shouldReturnGradSchool() throws ServiceException {
        final String schoolId = "12345";
        final var gradSchool = TestUtils.createGradSchool();
        when(this.restService.get(String.format(constants.getSchoolGradDetailsByIdFromGradSchoolApiUrl(), schoolId), GradSchool.class, gradSchoolWebClient)).thenReturn(gradSchool);
        var result = this.gradSchoolService.getGradSchoolBySchoolId(schoolId);
        Assert.assertNotNull(result);
        Assert.assertEquals(gradSchool.getSchoolID(), result.getSchoolID());
    }

    @Test
    public void testIsGradSchoolTranscriptIssuer_givenCanIssueTranscriptsY_shouldReturnTrue() throws ServiceException {
        final var gradSchool = TestUtils.createGradSchool();
        gradSchool.setCanIssueTranscripts("Y");
        when(this.restService.get(String.format(constants.getSchoolGradDetailsByIdFromGradSchoolApiUrl(), gradSchool.getSchoolID()), GradSchool.class, gradSchoolWebClient)).thenReturn(gradSchool);
        var result = this.gradSchoolService.isGradSchoolTranscriptIssuer(gradSchool.getSchoolID());
        Assert.assertTrue(result);
    }
}
