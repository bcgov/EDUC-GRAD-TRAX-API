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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

public class SchoolMovedServiceTest extends BaseReplicationServiceTest {

    @Autowired
    private SchoolMovedService schoolMovedService;

    @MockBean
    private SchoolService schoolServiceMock;

    @Test
    public void testProcessEvent_givenMOVE_SCHOOL_Event_shouldProcessEvent() throws JsonProcessingException {
        final var request = TestUtils.createMoveSchoolData();
        final var event = TestUtils.createEvent(EventType.MOVE_SCHOOL.toString(), request, this.replicationTestUtils.getEventRepository());
        var schoolDetail = TestUtils.createSchoolDetail();
        schoolDetail.setSchoolId(request.getFromSchoolId());
        when(schoolServiceMock.getSchoolDetailByIdFromInstituteApi(request.getFromSchoolId())).thenReturn(schoolDetail);
        this.schoolMovedService.processEvent(request, event);
        var result = this.replicationTestUtils.getEventRepository().findById(event.getReplicationEventId());
        if(result.isPresent()){
            Assert.assertEquals("PROCESSED", result.get().getEventStatus());
        } else {
            fail("MOVE_SCHOOL failed to process");
        }
    }

    @Test
    public void testProcessEvent_givenMOVE_SCHOOL_Event_ServiceUnavailable_triggerError() throws JsonProcessingException {
        final String ERROR_MSG = "Test Exception";
        doThrow(new ServiceException(ERROR_MSG)).when(schoolServiceMock).updateSchoolCache(anyList());
        final var request = TestUtils.createMoveSchoolData();
        final var event = TestUtils.createEvent(EventType.MOVE_SCHOOL.toString(), request, this.replicationTestUtils.getEventRepository());
        this.schoolMovedService.processEvent(request, event);
        var result = this.replicationTestUtils.getEventRepository().findById(event.getReplicationEventId());
        if(result.isPresent()){
            Assert.assertEquals("DB_COMMITTED", result.get().getEventStatus());
        } else {
            fail("MOVE_SCHOOL failed to process");
        }
    }

    @Test
    public void testProcessEvent_givenMOVE_SCHOOL_EventWithPassingHistoryCriteria_shouldStoreInHistoryTable() throws JsonProcessingException {
        final var request = TestUtils.createMoveSchoolData();
        final var event = TestUtils.createEvent(EventType.MOVE_SCHOOL.toString(), request, this.replicationTestUtils.getEventRepository());
        var schoolDetail = TestUtils.createSchoolDetail();
        schoolDetail.setSchoolId(request.getFromSchoolId());
        schoolDetail.setCanIssueTranscripts(false);
        when(schoolServiceMock.getSchoolDetailByIdFromInstituteApi(request.getFromSchoolId())).thenReturn(schoolDetail);
        this.schoolMovedService.processEvent(request, event);
        var result = this.replicationTestUtils.getEventHistoryRepository().findByEvent_ReplicationEventId(event.getReplicationEventId());
        Assert.assertTrue(result.isPresent());
    }

    @Test
    public void testProcessEvent_givenMOVE_SCHOOL_EventWithFailingHistoryCriteria_shouldNotStoreInHistoryTable() throws JsonProcessingException {
        final var request = TestUtils.createMoveSchoolData();
        request.getToSchool().setCanIssueTranscripts(false);
        final var event = TestUtils.createEvent(EventType.MOVE_SCHOOL.toString(), request, this.replicationTestUtils.getEventRepository());
        var schoolDetail = TestUtils.createSchoolDetail();
        schoolDetail.setSchoolId(request.getFromSchoolId());
        schoolDetail.setCanIssueTranscripts(false);
        when(schoolServiceMock.getSchoolDetailByIdFromInstituteApi(request.getFromSchoolId())).thenReturn(schoolDetail);
        this.schoolMovedService.processEvent(request, event);
        var result = this.replicationTestUtils.getEventHistoryRepository().findByEvent_ReplicationEventId(event.getReplicationEventId());
        Assert.assertFalse(result.isPresent());
    }
}
