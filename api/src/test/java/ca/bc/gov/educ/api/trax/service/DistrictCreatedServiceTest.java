package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.constant.EventType;
import ca.bc.gov.educ.api.trax.exception.ServiceException;
import ca.bc.gov.educ.api.trax.service.institute.DistrictService;
import ca.bc.gov.educ.api.trax.support.TestUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;

public class DistrictCreatedServiceTest extends BaseReplicationServiceTest {

    @Autowired
    private DistrictCreatedService districtCreatedService;

    @MockBean
    private DistrictService districtServiceMock;

    @Test
    public void testProcessEvent_givenCREATE_DISTRICT_Event_shouldProcessEvent() throws JsonProcessingException {
        final var request = TestUtils.createDistrict();
        final var event = TestUtils.createEvent(EventType.CREATE_DISTRICT.toString(), request, this.replicationTestUtils.getEventRepository());
        this.districtCreatedService.processEvent(request, event);
        var result = this.replicationTestUtils.getEventRepository().findById(event.getReplicationEventId());
        if(result.isPresent()){
            Assert.assertEquals("PROCESSED", result.get().getEventStatus());
        } else {
            fail("CREATE_DISTRICT failed to process");
        }
    }

    @Test
    public void testProcessEvent_givenCREATE_DISTRICT_Event_ServiceUnavailable_triggerError() throws JsonProcessingException {
        final String ERROR_MSG = "Test Exception";
        doThrow(new ServiceException(ERROR_MSG)).when(districtServiceMock).updateDistrictCache(anyString());
        final var request = TestUtils.createDistrict();
        final var event = TestUtils.createEvent(EventType.CREATE_DISTRICT.toString(), request, this.replicationTestUtils.getEventRepository());
        this.districtCreatedService.processEvent(request, event);
        var result = this.replicationTestUtils.getEventRepository().findById(event.getReplicationEventId());
        if(result.isPresent()){
            Assert.assertEquals("DB_COMMITTED", result.get().getEventStatus());
        } else {
            fail("CREATE_DISTRICT failed to process");
        }
    }
}
