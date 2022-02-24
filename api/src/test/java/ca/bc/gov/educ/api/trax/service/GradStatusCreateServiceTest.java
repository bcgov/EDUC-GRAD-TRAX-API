package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.messaging.NatsConnection;
import ca.bc.gov.educ.api.trax.messaging.jetstream.Publisher;
import ca.bc.gov.educ.api.trax.messaging.jetstream.Subscriber;
import ca.bc.gov.educ.api.trax.repository.EventRepository;
import ca.bc.gov.educ.api.trax.repository.TraxStudentRepository;
import ca.bc.gov.educ.api.trax.support.TestUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.MockitoAnnotations.openMocks;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class GradStatusCreateServiceTest {
    @Autowired
    private GradStatusCreateService gradStatusCreateService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private TraxStudentRepository traxStudentRepository;

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
        this.traxStudentRepository.deleteAll();
        this.eventRepository.deleteAll();
    }

    @Test
    public void testProcessEvent_givenCREATE_GRAD_STATUS_Event_for_2018_program() throws JsonProcessingException {
        testProcessEvent("2018-EN", "CUR");
    }

    @Test
    public void testProcessEvent_givenCREATE_GRAD_STATUS_Event_for_2004_program() throws JsonProcessingException {
        testProcessEvent("2004-EN", "CUR");
    }

    @Test
    public void testProcessEvent_givenCREATE_GRAD_STATUS_Event_for_1996_program() throws JsonProcessingException {
        testProcessEvent("1996-EN", "CUR");
    }

    @Test
    public void testProcessEvent_givenCREATE_GRAD_STATUS_Event_for_1986_program() throws JsonProcessingException {
        testProcessEvent("1986-EN", "CUR");
    }

    @Test
    public void testProcessEvent_givenCREATE_GRAD_STATUS_Event_for_1950_program() throws JsonProcessingException {
        testProcessEvent("1950", "CUR");
    }

    @Test
    public void testProcessEvent_givenCREATE_GRAD_STATUS_Event_for_SCCP_program() throws JsonProcessingException {
        testProcessEvent("SCCP", "CUR");
    }

    @Test
    public void testProcessEvent_givenCREATE_GRAD_STATUS_Event_for_2018_program_withArchivedStatus() throws JsonProcessingException {
        testProcessEvent("2018-EN", "ARC");
    }

    @Test
    public void testProcessEvent_givenCREATE_GRAD_STATUS_Event_for_2018_program_withDeceasedStatus() throws JsonProcessingException {
        testProcessEvent("2018-EN", "DEC");
    }

    @Test
    public void testProcessEvent_givenCREATE_GRAD_STATUS_Event_for_2018_program_withTerminatedStatus() throws JsonProcessingException {
        testProcessEvent("2018-EN", "TER");
    }

    @Test
    public void testProcessEvent_givenCREATE_GRAD_STATUS_Event_for_2004_program_withArchivedStatus() throws JsonProcessingException {
        testProcessEvent("2004-EN", "ARC");
    }

    @Test
    public void testProcessEvent_givenCREATE_GRAD_STATUS_Event_for_2004_program_withDeceasedStatus() throws JsonProcessingException {
        testProcessEvent("2004-EN", "DEC");
    }

    @Test
    public void testProcessEvent_givenCREATE_GRAD_STATUS_Event_for_1996_program_withMergedStatus() throws JsonProcessingException {
        testProcessEvent("1996-EN", "MER");
    }

    private void testProcessEvent(String program, String studentStatus) throws JsonProcessingException {
        final var request = TestUtils.createGraduationStatus();
        request.setProgram(program);
        request.setStudentStatus(studentStatus);
        final var event = TestUtils.createEvent("CREATE_GRAD_STATUS", request, eventRepository);
        this.gradStatusCreateService.processEvent(request, event);
    }

}
