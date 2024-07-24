package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.service.institute.SchoolService;
import ca.bc.gov.educ.api.trax.support.TestUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.assertj.core.api.Assertions.fail;

public class SchoolContactUpdatedServiceTest extends BaseReplicationServiceTest {

    @Autowired
    private SchoolContactUpdatedService schoolContactUpdatedService;

    @MockBean
    private SchoolService schoolServiceMock;

    @Test
    public void testProcessEvent_givenUPDATE_SCHOOL_CONTACT_Event_shouldProcessEvent() throws JsonProcessingException {
        final var request = TestUtils.createSchoolContact();
        final var event = TestUtils.createEvent("UPDATE_SCHOOL_CONTACT", request, this.replicationTestUtils.getEventRepository());
        this.schoolContactUpdatedService.processEvent(request, event);
        var result = this.replicationTestUtils.getEventRepository().findById(event.getReplicationEventId());
        if(result.isPresent()){
            Assert.assertEquals("PROCESSED", result.get().getEventStatus());
        } else {
            fail("UPDATE_SCHOOL_CONTACT failed to process");
        }
    }
}
