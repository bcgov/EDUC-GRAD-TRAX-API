package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.support.TestUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import static org.assertj.core.api.Assertions.fail;

public class SchoolContactDeletedServiceTest extends BaseReplicationServiceTest {

    @Autowired
    private SchoolContactDeletedService schoolContactDeletedService;

    @Test
    public void testProcessEvent_givenDELETE_SCHOOL_CONTACT_Event_shouldProcessEvent() throws JsonProcessingException {
        final var request = TestUtils.createSchoolContact();
        final var event = TestUtils.createEvent("DELETE_SCHOOL_CONTACT", request, this.replicationTestUtils.getEventRepository());
        this.schoolContactDeletedService.processEvent(request, event);
        var result = this.replicationTestUtils.getEventRepository().findById(event.getReplicationEventId());
        if(result.isPresent()){
            Assert.assertEquals(result.get().getEventStatus(), "PROCESSED");
        } else {
            fail("DELETE_SCHOOL_CONTACT failed to process");
        }
    }

    @Test
    public void testGetEntityManager_expectNull() {
        Assert.assertNull(this.schoolContactDeletedService.getEntityManager());
    }

}
