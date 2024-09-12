package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.constant.EventType;
import ca.bc.gov.educ.api.trax.exception.ServiceException;
import ca.bc.gov.educ.api.trax.service.institute.SchoolService;
import ca.bc.gov.educ.api.trax.support.TestUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;

public class SchoolCreatedServiceTest extends BaseReplicationServiceTest {

    @Autowired
    private SchoolCreatedService schoolCreatedService;

    @MockBean
    private SchoolService schoolServiceMock;

    @Test
    public void testProcessEvent_givenCREATE_SCHOOL_Event_shouldProcessEvent() throws JsonProcessingException {
        final var request = TestUtils.createSchool();
        final var event = TestUtils.createEvent(EventType.CREATE_SCHOOL.toString(), request, this.replicationTestUtils.getEventRepository());
        this.schoolCreatedService.processEvent(request, event);
        var result = this.replicationTestUtils.getEventRepository().findById(event.getReplicationEventId());
        if(result.isPresent()){
            Assert.assertEquals("PROCESSED", result.get().getEventStatus());
        } else {
            fail("CREATE_SCHOOL failed to process");
        }
    }

    @Test
    public void testProcessEvent_givenCREATE_SCHOOL_Event_ServiceUnavailable_triggerError() throws JsonProcessingException {
        final String ERROR_MSG = "Test Exception";
        doThrow(new ServiceException(ERROR_MSG)).when(schoolServiceMock).updateSchoolCache(anyString());
        final var request = TestUtils.createSchool();
        final var event = TestUtils.createEvent(EventType.CREATE_SCHOOL.toString(), request, this.replicationTestUtils.getEventRepository());
        this.schoolCreatedService.processEvent(request, event);
        var result = this.replicationTestUtils.getEventRepository().findById(event.getReplicationEventId());
        if(result.isPresent()){
            Assert.assertEquals("DB_COMMITTED", result.get().getEventStatus());
        } else {
            fail("CREATE_SCHOOL failed to process");
        }
    }

    @Test
    public void testProcessEvent_givenCREATE_SCHOOL_EventWithPassingHistoryCriteria_shouldStoreInHistoryTable() throws JsonProcessingException {
        final var request = TestUtils.createSchool();
        final var event = TestUtils.createEvent(EventType.CREATE_SCHOOL.toString(), request, this.replicationTestUtils.getEventRepository());
        this.schoolCreatedService.processEvent(request, event);
        var result = this.replicationTestUtils.getEventHistoryRepository().findByEvent_ReplicationEventId(event.getReplicationEventId());
        Assert.assertTrue(result.isPresent());
    }

    @Test
    public void testProcessEvent_givenCREATE_SCHOOL_EventWithFailingHistoryCriteria_shouldNotStoreInHistoryTable() throws JsonProcessingException {
        final var request = TestUtils.createSchool();
        request.setCanIssueTranscripts(false);
        final var event = TestUtils.createEvent(EventType.CREATE_SCHOOL.toString(), request, this.replicationTestUtils.getEventRepository());
        this.schoolCreatedService.processEvent(request, event);
        var result = this.replicationTestUtils.getEventHistoryRepository().findByEvent_ReplicationEventId(event.getReplicationEventId());
        Assert.assertFalse(result.isPresent());
    }
}
