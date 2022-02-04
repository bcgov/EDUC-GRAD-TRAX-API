package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.messaging.NatsConnection;
import ca.bc.gov.educ.api.trax.messaging.jetstream.Publisher;
import ca.bc.gov.educ.api.trax.messaging.jetstream.Subscriber;
import ca.bc.gov.educ.api.trax.repository.EventRepository;
import ca.bc.gov.educ.api.trax.repository.TraxStudentRepository;
import ca.bc.gov.educ.api.trax.support.TestUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

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

    @Test
    public void testProcessEvent_givenCREATE_GRAD_STATUS_Event_for_2018_program() throws JsonProcessingException {
        final var request = TestUtils.createGraduationStatus();
        final var event = TestUtils.createEvent("CREATE_GRAD_STATUS", request, eventRepository);
        this.gradStatusCreateService.processEvent(request, event);

        final var traxStudent = this.traxStudentRepository.findById(StringUtils.rightPad(request.getPen(), 10));
        assertThat(traxStudent).isPresent();
    }

    @Test
    public void testProcessEvent_givenCREATE_GRAD_STATUS_Event_for_2004_program() throws JsonProcessingException {
        final var request = TestUtils.createGraduationStatus();
        request.setProgram("2004-EN");
        final var event = TestUtils.createEvent("CREATE_GRAD_STATUS", request, eventRepository);
        this.gradStatusCreateService.processEvent(request, event);

        final var traxStudent = this.traxStudentRepository.findById(StringUtils.rightPad(request.getPen(), 10));
        assertThat(traxStudent).isPresent();
    }

    @Test
    public void testProcessEvent_givenCREATE_GRAD_STATUS_Event_for_1996_program() throws JsonProcessingException {
        final var request = TestUtils.createGraduationStatus();
        request.setProgram("1996-EN");
        final var event = TestUtils.createEvent("CREATE_GRAD_STATUS", request, eventRepository);
        this.gradStatusCreateService.processEvent(request, event);

        final var traxStudent = this.traxStudentRepository.findById(StringUtils.rightPad(request.getPen(), 10));
        assertThat(traxStudent).isPresent();
    }

    @Test
    public void testProcessEvent_givenCREATE_GRAD_STATUS_Event_for_1986_program() throws JsonProcessingException {
        final var request = TestUtils.createGraduationStatus();
        request.setProgram("1986-EN");
        final var event = TestUtils.createEvent("CREATE_GRAD_STATUS", request, eventRepository);
        this.gradStatusCreateService.processEvent(request, event);

        final var traxStudent = this.traxStudentRepository.findById(StringUtils.rightPad(request.getPen(), 10));
        assertThat(traxStudent).isPresent();
    }

    @Test
    public void testProcessEvent_givenCREATE_GRAD_STATUS_Event_for_1950_program() throws JsonProcessingException {
        final var request = TestUtils.createGraduationStatus();
        request.setProgram("1950");
        final var event = TestUtils.createEvent("CREATE_GRAD_STATUS", request, eventRepository);
        this.gradStatusCreateService.processEvent(request, event);

        final var traxStudent = this.traxStudentRepository.findById(StringUtils.rightPad(request.getPen(), 10));
        assertThat(traxStudent).isPresent();
    }

    @Test
    public void testProcessEvent_givenCREATE_GRAD_STATUS_Event_for_SCCP_program() throws JsonProcessingException {
        final var request = TestUtils.createGraduationStatus();
        request.setProgram("SCCP");
        final var event = TestUtils.createEvent("CREATE_GRAD_STATUS", request, eventRepository);
        this.gradStatusCreateService.processEvent(request, event);

        final var traxStudent = this.traxStudentRepository.findById(StringUtils.rightPad(request.getPen(), 10));
        assertThat(traxStudent).isPresent();
    }

    @Test
    public void testProcessEvent_givenCREATE_GRAD_STATUS_Event_for_2018_program_withArchivedStatus() throws JsonProcessingException {
        final var request = TestUtils.createGraduationStatus();
        request.setStudentStatus("ARC");
        final var event = TestUtils.createEvent("CREATE_GRAD_STATUS", request, eventRepository);
        this.gradStatusCreateService.processEvent(request, event);

        final var traxStudent = this.traxStudentRepository.findById(StringUtils.rightPad(request.getPen(), 10));
        assertThat(traxStudent).isPresent();
    }

    @Test
    public void testProcessEvent_givenCREATE_GRAD_STATUS_Event_for_2018_program_withDeceasedStatus() throws JsonProcessingException {
        final var request = TestUtils.createGraduationStatus();
        request.setStudentStatus("DEC");
        final var event = TestUtils.createEvent("CREATE_GRAD_STATUS", request, eventRepository);
        this.gradStatusCreateService.processEvent(request, event);

        final var traxStudent = this.traxStudentRepository.findById(StringUtils.rightPad(request.getPen(), 10));
        assertThat(traxStudent).isPresent();
    }

    @Test
    public void testProcessEvent_givenCREATE_GRAD_STATUS_Event_for_2018_program_withTerminatedStatus() throws JsonProcessingException {
        final var request = TestUtils.createGraduationStatus();
        request.setStudentStatus("TER");
        final var event = TestUtils.createEvent("CREATE_GRAD_STATUS", request, eventRepository);
        this.gradStatusCreateService.processEvent(request, event);

        final var traxStudent = this.traxStudentRepository.findById(StringUtils.rightPad(request.getPen(), 10));
        assertThat(traxStudent).isPresent();
    }

    @Test
    public void testProcessEvent_givenCREATE_GRAD_STATUS_Event_for_2004_program_withArchivedStatus() throws JsonProcessingException {
        final var request = TestUtils.createGraduationStatus();
        request.setProgram("2004-EN");
        request.setStudentStatus("ARC");
        final var event = TestUtils.createEvent("CREATE_GRAD_STATUS", request, eventRepository);
        this.gradStatusCreateService.processEvent(request, event);

        final var traxStudent = this.traxStudentRepository.findById(StringUtils.rightPad(request.getPen(), 10));
        assertThat(traxStudent).isPresent();
    }

    @Test
    public void testProcessEvent_givenCREATE_GRAD_STATUS_Event_for_2004_program_withDeceasedStatus() throws JsonProcessingException {
        final var request = TestUtils.createGraduationStatus();
        request.setProgram("2004-EN");
        request.setStudentStatus("DEC");
        final var event = TestUtils.createEvent("CREATE_GRAD_STATUS", request, eventRepository);
        this.gradStatusCreateService.processEvent(request, event);

        final var traxStudent = this.traxStudentRepository.findById(StringUtils.rightPad(request.getPen(), 10));
        assertThat(traxStudent).isPresent();
    }

    @Test
    public void testProcessEvent_givenCREATE_GRAD_STATUS_Event_for_1996_program_withMergedStatus() throws JsonProcessingException {
        final var request = TestUtils.createGraduationStatus();
        request.setProgram("1996-EN");
        request.setStudentStatus("MER");
        final var event = TestUtils.createEvent("CREATE_GRAD_STATUS", request, eventRepository);
        this.gradStatusCreateService.processEvent(request, event);

        final var traxStudent = this.traxStudentRepository.findById(StringUtils.rightPad(request.getPen(), 10));
        assertThat(traxStudent).isPresent();
    }

}
