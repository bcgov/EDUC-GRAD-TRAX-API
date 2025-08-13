package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.model.dto.GradSchool;
import ca.bc.gov.educ.api.trax.model.dto.institute.School;
import ca.bc.gov.educ.api.trax.model.dto.institute.SchoolDetail;
import ca.bc.gov.educ.api.trax.service.institute.GradSchoolService;
import ca.bc.gov.educ.api.trax.service.institute.SchoolService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@ExtendWith(MockitoExtension.class)
class SchoolEventBaseServiceTest {

    private static final String SCHOOL_ID = UUID.randomUUID().toString();
    
    @Mock
    private SchoolService schoolService;
    @Mock
    private GradSchoolService gradSchoolService;
    private AutoCloseable closeable;

    @InjectMocks
    private SchoolEventBaseService<School> schoolEventBaseService = new SchoolCreatedService(schoolService, gradSchoolService) {};

    @BeforeEach
    public void setUp() {
        closeable = openMocks(this);
    }

    @AfterEach
    public void finish() throws Exception {
        closeable.close();
    }

    @Test
    void shouldCreateHistory_whenSchoolCanIssueTranscriptsChanged() {
        School school = new School();
        school.setSchoolId(SCHOOL_ID);
        school.setCanIssueTranscripts(false);
        SchoolDetail schoolDetail = new SchoolDetail();
        schoolDetail.setCanIssueTranscripts(true);
        when(gradSchoolService.isGradSchoolTranscriptIssuer(SCHOOL_ID)).thenReturn(false);
        when(schoolService.getSchoolDetailBySchoolIdFromRedisCache(UUID.fromString(SCHOOL_ID))).thenReturn(schoolDetail);
        assertTrue(schoolEventBaseService.shouldCreateHistory(school));
    }

    @Test
    void shouldCreateHistory_whenSchoolCanIssueTranscriptsIsTrue() {
        School school = new School();
        school.setSchoolId(SCHOOL_ID);
        school.setCanIssueTranscripts(true);
        SchoolDetail schoolDetail = new SchoolDetail();
        schoolDetail.setCanIssueTranscripts(true);
        when(gradSchoolService.isGradSchoolTranscriptIssuer(SCHOOL_ID)).thenReturn(false);
        when(schoolService.getSchoolDetailBySchoolIdFromRedisCache(UUID.fromString(SCHOOL_ID))).thenReturn(schoolDetail);
        assertTrue(schoolEventBaseService.shouldCreateHistory(school));
    }

    @Test
    void shouldNotCreateHistory_whenSchoolCanIssueTranscriptsIsFalse() {
        School school = new School();
        school.setSchoolId(SCHOOL_ID);
        school.setCanIssueTranscripts(false);
        SchoolDetail schoolDetail = new SchoolDetail();
        schoolDetail.setCanIssueTranscripts(false);
        when(schoolService.getSchoolDetailBySchoolIdFromRedisCache(UUID.fromString(SCHOOL_ID))).thenReturn(schoolDetail);
        assertFalse(schoolEventBaseService.shouldCreateHistory(school));
    }
}