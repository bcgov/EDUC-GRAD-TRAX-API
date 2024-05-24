package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.support.TestUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import static org.assertj.core.api.Assertions.fail;

public class AuthorityContactDeletedServiceTest extends BaseReplicationServiceTest {

    @Autowired
    private AuthorityContactDeletedService authorityContactDeletedService;

    @Test
    public void testProcessEvent_givenUPDATE_AUTHORITY_CONTACT_Event_shouldProcessEvent() throws JsonProcessingException {
        final var request = TestUtils.createAuthorityContact();
        final var event = TestUtils.createEvent("DELETE_AUTHORITY_CONTACT", request, this.replicationTestUtils.getEventRepository());
        this.authorityContactDeletedService.processEvent(request, event);
        var result = this.replicationTestUtils.getEventRepository().findById(event.getReplicationEventId());
        if(result.isPresent()){
            Assert.assertEquals(result.get().getEventStatus(), "PROCESSED");
        } else {
            fail("DELETE_AUTHORITY_CONTACT failed to process");
        }
    }

    @Test
    public void testGetEntityManager_expectNull() {
        Assert.assertNull(this.authorityContactDeletedService.getEntityManager());
    }

}
