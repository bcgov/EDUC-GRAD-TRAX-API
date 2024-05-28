package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.support.TestUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import static org.assertj.core.api.Assertions.fail;

public class DistrictContactCreatedServiceTest extends BaseReplicationServiceTest {

    @Autowired
    private DistrictContactCreatedService districtContactCreatedService;

    @Test
    public void testProcessEvent_givenCREATE_DISTRICT_CONTACT_Event_shouldProcessEvent() throws JsonProcessingException {
        final var request = TestUtils.createDistrictContact();
        final var event = TestUtils.createEvent("CREATE_DISTRICT_CONTACT", request, this.replicationTestUtils.getEventRepository());
        this.districtContactCreatedService.processEvent(request, event);
        var result = this.replicationTestUtils.getEventRepository().findById(event.getReplicationEventId());
        if(result.isPresent()){
            Assert.assertEquals("PROCESSED", result.get().getEventStatus());
        } else {
            fail("CREATE_DISTRICT_CONTACT failed to process");
        }
    }

}
