package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.messaging.NatsConnection;
import ca.bc.gov.educ.api.trax.messaging.jetstream.Publisher;
import ca.bc.gov.educ.api.trax.messaging.jetstream.Subscriber;
import ca.bc.gov.educ.api.trax.model.dto.TranscriptStudentDemog;
import ca.bc.gov.educ.api.trax.model.entity.TranscriptStudentDemogEntity;
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
        transcriptStudentDemogEntity.setFirstName("Test");
        transcriptStudentDemogEntity.setLastName("QA");
        transcriptStudentDemogEntity.setMincode("7654321");
        transcriptStudentDemogEntity.setSchoolName("Test2 School");

        when(transcriptStudentDemogRepository.findById(eq("123456789"))).thenReturn(Optional.of(transcriptStudentDemogEntity));

        TranscriptStudentDemog result = tswService.getTranscriptStudentDemog(transcriptStudentDemogEntity.getStudNo());

        assertThat(result).isNotNull();
        assertThat(result.getSchoolName()).isEqualTo(transcriptStudentDemogEntity.getSchoolName());
        assertThat(result.getStudNo()).isEqualTo(transcriptStudentDemogEntity.getStudNo());
    }

    @Test
    public void testStudentIsNotGraduated() {
        // Student is graduated or not

        final TranscriptStudentDemogEntity transcriptStudentDemogEntity = new TranscriptStudentDemogEntity();
        transcriptStudentDemogEntity.setStudNo("123456789");
        transcriptStudentDemogEntity.setFirstName("Test");
        transcriptStudentDemogEntity.setLastName("QA");
        transcriptStudentDemogEntity.setMincode("7654321");
        transcriptStudentDemogEntity.setSchoolName("Test2 School");
        transcriptStudentDemogEntity.setGradDate(0L);

        when(transcriptStudentDemogRepository.countGradDateByPen(eq("123456789"))).thenReturn(0);

        Boolean result = tswService.isGraduated(transcriptStudentDemogEntity.getStudNo());

        assertThat(result).isNotNull();
        assertThat(result).isFalse();
    }

    @Test
    public void testStudentIsGraduated() {
        // Student is graduated or not

        final TranscriptStudentDemogEntity transcriptStudentDemogEntity = new TranscriptStudentDemogEntity();
        transcriptStudentDemogEntity.setStudNo("123456789");
        transcriptStudentDemogEntity.setFirstName("Test");
        transcriptStudentDemogEntity.setLastName("QA");
        transcriptStudentDemogEntity.setMincode("7654321");
        transcriptStudentDemogEntity.setSchoolName("Test2 School");
        transcriptStudentDemogEntity.setGradDate(20201031L);

        when(transcriptStudentDemogRepository.countGradDateByPen(eq("123456789"))).thenReturn(1);

        Boolean result = tswService.isGraduated(transcriptStudentDemogEntity.getStudNo());

        assertThat(result).isNotNull();
        assertThat(result).isTrue();
    }
}
