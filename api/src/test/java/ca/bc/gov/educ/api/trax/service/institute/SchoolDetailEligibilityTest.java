package ca.bc.gov.educ.api.trax.service.institute;

import ca.bc.gov.educ.api.trax.model.dto.GradSchool;
import ca.bc.gov.educ.api.trax.model.dto.institute.SchoolDetail;
import org.junit.jupiter.api.Test;
import ca.bc.gov.educ.api.trax.model.transformer.institute.SchoolDetailTransformer;
import ca.bc.gov.educ.api.trax.model.entity.institute.SchoolDetailEntity;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SchoolDetailEligibilityTest {
    private final CacheService cache = mock(CacheService.class);
    private final SchoolDetailTransformer transformer = mock(SchoolDetailTransformer.class);
    private final SchoolService service = spy(new SchoolService(null, null, null, null, null,
            null, transformer, null, null, cache, null));

    private SchoolDetail detail(String id, boolean eligible) {
        SchoolDetail detail = new SchoolDetail();
        detail.setSchoolId(id);
        detail.setCanIssueTranscripts(eligible);
        detail.setCanIssueCertificates(eligible);
        return detail;
    }

    private GradSchool grad(String id, String eligibility) {
        GradSchool grad = new GradSchool();
        grad.setSchoolID(id);
        grad.setCanIssueTranscripts(eligibility);
        grad.setCanIssueCertificates(eligibility);
        return grad;
    }

    @Test
    void bulkDetailsUseGradEligibilityInBothDirections() {
        doReturn(List.of(detail("first", false), detail("second", true))).when(service)
                .getSchoolDetailsPaginatedFromInstituteApi(eq(0), anyList());
        doReturn(List.of(grad("first", "Y"), grad("second", "N")))
                .when(service).getSchoolGradDetailsFromSchoolApi();
        List<SchoolDetail> result = service.getSchoolDetailsFromInstituteApi();
        assertTrue(result.get(0).isCanIssueTranscripts());
        assertTrue(result.get(0).isCanIssueCertificates());
        assertFalse(result.get(1).isCanIssueTranscripts());
        assertFalse(result.get(1).isCanIssueCertificates());
    }

    @Test
    void emptyGradListRetainsInstituteEligibilityAndContinuesCacheLoad() {
        List<SchoolDetail> details = List.of(detail("first", true), detail("second", false));
        doReturn(details).when(service).getSchoolDetailsPaginatedFromInstituteApi(eq(0), anyList());
        doReturn(List.of()).when(service).getSchoolGradDetailsFromSchoolApi();
        List<SchoolDetailEntity> entities = List.of(new SchoolDetailEntity());
        when(transformer.transformToEntity(details)).thenReturn(entities);

        List<SchoolDetail> result = service.loadSchoolDetailsFromInstituteApiIntoRedisCacheAsync();

        assertTrue(result.get(0).isCanIssueTranscripts());
        assertTrue(result.get(0).isCanIssueCertificates());
        assertFalse(result.get(1).isCanIssueTranscripts());
        assertFalse(result.get(1).isCanIssueCertificates());
        verify(cache).loadSchoolDetailsIntoRedisCacheAsync(entities);
    }

    @Test
    void missingAndInvalidSchoolsRetainInstituteEligibilityWhileValidSchoolUsesGrad() {
        doReturn(List.of(detail("valid", false), detail("missing", true), detail("invalid", true)))
                .when(service).getSchoolDetailsPaginatedFromInstituteApi(eq(0), anyList());
        GradSchool invalid = grad("invalid", "N");
        invalid.setCanIssueCertificates(null);
        doReturn(List.of(grad("valid", "Y"), invalid)).when(service).getSchoolGradDetailsFromSchoolApi();

        List<SchoolDetail> result = service.getSchoolDetailsFromInstituteApi();

        for (SchoolDetail detail : result) {
            assertTrue(detail.isCanIssueTranscripts());
            assertTrue(detail.isCanIssueCertificates());
        }
    }
}
