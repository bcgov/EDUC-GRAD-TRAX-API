package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.constant.EventType;
import ca.bc.gov.educ.api.trax.messaging.NatsConnection;
import ca.bc.gov.educ.api.trax.messaging.jetstream.Publisher;
import ca.bc.gov.educ.api.trax.messaging.jetstream.Subscriber;
import ca.bc.gov.educ.api.trax.repository.EventRepository;
import ca.bc.gov.educ.api.trax.repository.TraxStudentRepository;
import ca.bc.gov.educ.api.trax.support.TestUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.StringUtils;
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
public class GradStudentUndoCompletionServiceTest {
    @Autowired
    private GradStudentUndoCompletionService gradStudentUndoCompletionService;

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
    public void testProcessEvent_givenGRAD_STUDENT_UNDO_COMPLETION_Event_for_2018_program() throws JsonProcessingException {
        createTraxStudent("2018-EN", "CUR", null, null, null, true);
        testProcessEvent("2018-EN", "CUR");
    }

    @Test
    public void testProcessEvent_givenGRAD_STUDENT_UNDO_COMPLETION_Event_for_2004_program() throws JsonProcessingException {
        createTraxStudent("2004-EN", "CUR", null, null, null, true);
        testProcessEvent("2004-EN", "CUR");
    }

    @Test
    public void testProcessEvent_givenGRAD_STUDENT_UNDO_COMPLETION_Event_for_1996_program() throws JsonProcessingException {
        createTraxStudent("1996-EN", "CUR", null, null, null, true);
        testProcessEvent("1996-EN", "CUR");
    }

    @Test
    public void testProcessEvent_givenGRAD_STUDENT_UNDO_COMPLETION_Event_for_1986_program() throws JsonProcessingException {
        createTraxStudent("1986-EN", "CUR", null, null, null, true);
        testProcessEvent("1986-EN", "CUR");
    }

    @Test
    public void testProcessEvent_givenGRAD_STUDENT_UNDO_COMPLETION_Event_for_1950_program() throws JsonProcessingException {
        createTraxStudent("1950", "CUR", null, null, null, true);
        testProcessEvent("1950", "CUR");
    }

    @Test
    public void testProcessEvent_givenGRAD_STUDENT_UNDO_COMPLETION_Event_for_SCCP_program() throws JsonProcessingException {
        createTraxStudent("SCCP", "CUR", null, null, null, true);
        testProcessEvent("SCCP", "CUR");
    }

    @Test
    public void testProcessEvent_givenGRAD_STUDENT_UNDO_COMPLETION_Event_for_2018_program_withArchivedStatus() throws JsonProcessingException {
        createTraxStudent("2018-EN", "ARC", null, null, null, true);
        testProcessEvent("2018-EN", "ARC");
    }

    @Test
    public void testProcessEvent_givenGRAD_STUDENT_UNDO_COMPLETION_Event_for_2018_program_withDeceasedStatus() throws JsonProcessingException {
        createTraxStudent("2018-EN", "DEC", null, null, null, true);
        testProcessEvent("2018-EN", "DEC");
    }

    @Test
    public void testProcessEvent_givenGRAD_STUDENT_UNDO_COMPLETION_Event_for_2018_program_withTerminatedStatus() throws JsonProcessingException {
        createTraxStudent("2018-EN", "TER", null, null, null, true);
        testProcessEvent("2018-EN", "TER");
    }

    @Test
    public void testProcessEvent_givenGRAD_STUDENT_UNDO_COMPLETION_Event_for_2004_program_withArchivedStatus() throws JsonProcessingException {
        createTraxStudent("2004-EN", "ARC", null, null, null, true);
        testProcessEvent("2004-EN", "ARC");
    }

    @Test
    public void testProcessEvent_givenGRAD_STUDENT_UNDO_COMPLETION_Event_for_2004_program_withDeceasedStatus() throws JsonProcessingException {
        createTraxStudent("2004-EN", "DEC", null, null, null, true);
        testProcessEvent("2004-EN", "DEC");
    }

    @Test
    public void testProcessEvent_givenGRAD_STUDENT_UNDO_COMPLETION_Event_for_1996_program_withMergedStatus() throws JsonProcessingException {
        createTraxStudent("1996-EN", "MER", null, null, null, true);
        testProcessEvent("1996-EN", "MER");
    }

    private void testProcessEvent(String program, String studentStatus) throws JsonProcessingException {
        final var request = TestUtils.createGraduationStatus(true);
        request.setProgram(program);
        request.setStudentStatus(studentStatus);
        final var event = TestUtils.createEvent(EventType.GRAD_STUDENT_UNDO_COMPLETION.name(), request, eventRepository);
        this.gradStudentUndoCompletionService.processEvent(request, event);
    }

    private void createTraxStudent(String program, String studentStatus, String grade, String mincode, Long gradDate, boolean isGraduated) {
        final var traxStudent = TestUtils.createTraxStudent(program, studentStatus, isGraduated);
        if (grade != null) {
            traxStudent.setStudGrade(grade);
            if (isGraduated) {
                traxStudent.setStudGradeAtGrad(grade);
            }
        }
        if (mincode != null) {
            traxStudent.setMincode(mincode);
            if (isGraduated) {
                traxStudent.setMincodeGrad(mincode);
            }
        }
        if (gradDate != null) {
            traxStudent.setGradDate(gradDate);
            if (StringUtils.equals(program, "SCCP")) {
                traxStudent.setSlpDate(gradDate);
            }
        }
        this.traxStudentRepository.save(traxStudent);
    }

}
