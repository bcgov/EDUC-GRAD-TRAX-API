package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.support.TestUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import static org.assertj.core.api.Assertions.fail;

public class AuthorityContactUpdatedServiceTest extends BaseReplicationServiceTest {

    @Autowired
    private AuthorityContactUpdatedService authorityContactUpdatedService;

    @Test
    public void testProcessEvent_givenUPDATE_AUTHORITY_CONTACT_Event_shouldProcessEvent() throws JsonProcessingException {
        final var request = TestUtils.createAuthorityContact();
        final var event = TestUtils.createEvent("UPDATE_AUTHORITY_CONTACT", request, this.replicationTestUtils.getEventRepository());
        this.authorityContactUpdatedService.processEvent(request, event);
        var result = this.replicationTestUtils.getEventRepository().findById(event.getReplicationEventId());
        if(result.isPresent()){
            Assert.assertEquals(result.get().getEventStatus(), "PROCESSED");
        } else {
            fail("UPDATE_AUTHORITY_CONTACT failed to process");
        }
    }

    @Test
    public void testGetEntityManager_expectNull() {
        Assert.assertNull(this.authorityContactUpdatedService.getEntityManager());
    }

}
