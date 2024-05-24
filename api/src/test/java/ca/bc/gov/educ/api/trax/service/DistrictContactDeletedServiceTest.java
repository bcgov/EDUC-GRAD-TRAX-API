package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.support.TestUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import static org.assertj.core.api.Assertions.fail;

public class DistrictContactDeletedServiceTest extends BaseReplicationServiceTest {

    @Autowired
    private DistrictContactDeletedService districtContactDeletedService;

    @Test
    public void testProcessEvent_givenDELETE_DISTRICT_CONTACT_Event_shouldProcessEvent() throws JsonProcessingException {
        final var request = TestUtils.createDistrictContact();
        final var event = TestUtils.createEvent("DELETE_DISTRICT_CONTACT", request, this.replicationTestUtils.getEventRepository());
        this.districtContactDeletedService.processEvent(request, event);
        var result = this.replicationTestUtils.getEventRepository().findById(event.getReplicationEventId());
        if(result.isPresent()){
            Assert.assertEquals(result.get().getEventStatus(), "PROCESSED");
        } else {
            fail("DELETE_DISTRICT_CONTACT failed to process");
        }
    }

}
