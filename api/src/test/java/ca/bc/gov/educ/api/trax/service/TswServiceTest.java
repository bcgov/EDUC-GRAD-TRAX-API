package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.messaging.NatsConnection;
import ca.bc.gov.educ.api.trax.messaging.jetstream.Publisher;
import ca.bc.gov.educ.api.trax.messaging.jetstream.Subscriber;
import ca.bc.gov.educ.api.trax.model.dto.TranscriptStudentCourse;
import ca.bc.gov.educ.api.trax.model.dto.TranscriptStudentDemog;
import ca.bc.gov.educ.api.trax.model.entity.TranscriptStudentCourseEntity;
import ca.bc.gov.educ.api.trax.model.entity.TranscriptStudentCourseKey;
import ca.bc.gov.educ.api.trax.model.entity.TranscriptStudentDemogEntity;
import ca.bc.gov.educ.api.trax.repository.TranscriptStudentCourseRepository;
import ca.bc.gov.educ.api.trax.repository.TranscriptStudentDemogRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class TswServiceTest {

    @Autowired
    private TswService tswService;

    @MockBean
    private TranscriptStudentDemogRepository transcriptStudentDemogRepository;

    @MockBean
    private TranscriptStudentCourseRepository transcriptStudentCourseRepository;

    // NATS
    @MockBean
    private NatsConnection natsConnection;

    @MockBean
    private Publisher publisher;

    @MockBean
    private Subscriber subscriber;

    @Before
    public void setUp() {
        openMocks(this);
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testGetTranscriptStudentDemographicsData() {
        // Transcript Student Demographics data

        final TranscriptStudentDemogEntity transcriptStudentDemogEntity = new TranscriptStudentDemogEntity();
        transcriptStudentDemogEntity.setStudNo("123456789");
        transcriptStudentDemogEntity.setMincode("7654321");

        when(transcriptStudentDemogRepository.findById(eq("123456789"))).thenReturn(Optional.of(transcriptStudentDemogEntity));

        TranscriptStudentDemog result = tswService.getTranscriptStudentDemog(transcriptStudentDemogEntity.getStudNo());

        assertThat(result).isNotNull();
        assertThat(result.getMincode()).isEqualTo(transcriptStudentDemogEntity.getMincode());
        assertThat(result.getStudNo()).isEqualTo(transcriptStudentDemogEntity.getStudNo());
    }

    @Test
    public void testGetTranscriptStudentCourses() {
        // Transcript Student Courses data

        final TranscriptStudentCourseKey key = new TranscriptStudentCourseKey();
        key.setStudNo("123456789");
        key.setCourseCode("Test");
        key.setCourseLevel("12");

        final TranscriptStudentCourseEntity transcriptStudentCourseEntity = new TranscriptStudentCourseEntity();
        transcriptStudentCourseEntity.setStudentCourseKey(key);
        transcriptStudentCourseEntity.setCourseName("Test Course");

        when(transcriptStudentCourseRepository.findByPen(eq("123456789"))).thenReturn(Arrays.asList(transcriptStudentCourseEntity));

        List<TranscriptStudentCourse> results = tswService.getTranscriptStudentCourses(key.getStudNo());

        assertThat(results).isNotNull();
        assertThat(results.size()).isGreaterThan(0);
        assertThat(results.get(0).getStudNo()).isEqualTo(key.getStudNo());
        assertThat(results.get(0).getCourseCode()).isEqualTo(key.getCourseCode());
        assertThat(results.get(0).getCourseLevel()).isEqualTo(key.getCourseLevel());
    }
}
