package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.support.TestUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import static org.assertj.core.api.Assertions.fail;

public class DistrictContactUpdatedServiceTest extends BaseReplicationServiceTest {

    @Autowired
    private DistrictContactUpdatedService districtContactUpdatedService;

    @Test
    public void testProcessEvent_givenUPDATE_DISTRICT_CONTACT_Event_shouldProcessEvent() throws JsonProcessingException {
        final var request = TestUtils.createDistrictContact();
        final var event = TestUtils.createEvent("UPDATE_DISTRICT_CONTACT", request, this.replicationTestUtils.getEventRepository());
        this.districtContactUpdatedService.processEvent(request, event);
        var result = this.replicationTestUtils.getEventRepository().findById(event.getReplicationEventId());
        if(result.isPresent()){
            Assert.assertEquals("PROCESSED", result.get().getEventStatus());
        } else {
            fail("UPDATE_DISTRICT_CONTACT failed to process");
        }
    }

}
