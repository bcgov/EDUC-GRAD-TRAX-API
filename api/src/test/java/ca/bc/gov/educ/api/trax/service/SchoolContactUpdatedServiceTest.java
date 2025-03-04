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
import static org.mockito.Mockito.when;

public class SchoolContactUpdatedServiceTest extends BaseReplicationServiceTest {

    @Autowired
    private SchoolContactUpdatedService schoolContactUpdatedService;

    @MockBean
    private SchoolService schoolServiceMock;

    @Test
    public void testProcessEvent_givenUPDATE_SCHOOL_CONTACT_Event_shouldProcessEvent() throws JsonProcessingException {
        final var request = TestUtils.createSchoolContact();
        final var event = TestUtils.createEvent("UPDATE_SCHOOL_CONTACT", request, this.replicationTestUtils.getEventRepository());
        var schoolDetails = TestUtils.createSchoolDetail();
        schoolDetails.setSchoolId(request.getSchoolId());
        when(schoolServiceMock.getSchoolDetailByIdFromInstituteApi(request.getSchoolId())).thenReturn(schoolDetails);
        this.schoolContactUpdatedService.processEvent(request, event);
        var result = this.replicationTestUtils.getEventRepository().findById(event.getReplicationEventId());
        if(result.isPresent()){
            Assert.assertEquals("PROCESSED", result.get().getEventStatus());
        } else {
            fail("UPDATE_SCHOOL_CONTACT failed to process");
        }
    }

    @Test
    public void testProcessEvent_givenUPDATE_SCHOOL_CONTACT_Event_ServiceUnavailable_triggerError() throws JsonProcessingException {
        final String ERROR_MSG = "Test Exception";
        final var request = TestUtils.createSchoolContact();
        final var event = TestUtils.createEvent("UPDATE_SCHOOL_CONTACT", request, this.replicationTestUtils.getEventRepository());
        doThrow(new ServiceException(ERROR_MSG)).when(schoolServiceMock).getSchoolDetailByIdFromInstituteApi(anyString());
        this.schoolContactUpdatedService.processEvent(request, event);
        var result = this.replicationTestUtils.getEventRepository().findById(event.getReplicationEventId());
        if(result.isPresent()){
            Assert.assertEquals("DB_COMMITTED", result.get().getEventStatus());
        } else {
            fail("UPDATE_SCHOOL_CONTACT failed to process");
        }
    }

    @Test
    public void testProcessEvent_givenUPDATE_SCHOOL_CONTACT_EventWithPassingHistoryCriteria_shouldStoreInHistoryTable() throws JsonProcessingException {
        final var request = TestUtils.createSchoolContact();
        final var event = TestUtils.createEvent(EventType.CREATE_SCHOOL_CONTACT.toString(), request, this.replicationTestUtils.getEventRepository());
        var schoolDetails = TestUtils.createSchoolDetail();
        when(schoolServiceMock.getSchoolDetailByIdFromInstituteApi(request.getSchoolId())).thenReturn(schoolDetails);
        this.schoolContactUpdatedService.processEvent(request, event);
        var result = this.replicationTestUtils.getEventHistoryRepository().findByEvent_ReplicationEventId(event.getReplicationEventId());
        Assert.assertFalse(result.isPresent());
    }

    @Test
    public void testProcessEvent_givenUPDATE_SCHOOL_CONTACT_EventWithFailingHistoryCriteria_shouldNotStoreInHistoryTable() throws JsonProcessingException {
        final var request = TestUtils.createSchoolContact();
        final var event = TestUtils.createEvent(EventType.CREATE_SCHOOL_CONTACT.toString(), request, this.replicationTestUtils.getEventRepository());
        var schoolDetails = TestUtils.createSchoolDetail();
        schoolDetails.setCanIssueTranscripts(false);
        when(schoolServiceMock.getSchoolDetailByIdFromInstituteApi(request.getSchoolId())).thenReturn(schoolDetails);
        this.schoolContactUpdatedService.processEvent(request, event);
        var result = this.replicationTestUtils.getEventHistoryRepository().findByEvent_ReplicationEventId(event.getReplicationEventId());
        Assert.assertFalse(result.isPresent());
    }
}
