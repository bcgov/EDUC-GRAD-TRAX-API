package ca.bc.gov.educ.api.trax.scheduler;

import ca.bc.gov.educ.api.trax.choreographer.ChoreographEventHandler;
import ca.bc.gov.educ.api.trax.constant.EventType;
import ca.bc.gov.educ.api.trax.service.BaseReplicationServiceTest;
import ca.bc.gov.educ.api.trax.support.TestUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import net.javacrumbs.shedlock.core.LockAssert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

public class JetStreamEventSchedulerTest extends BaseReplicationServiceTest {

  @Autowired
  private JetStreamEventScheduler jetStreamScheduler;

  @SpyBean
  private ChoreographEventHandler choreographer;

  @Test
  public void testScheduler_whenRunShouldFindDB_COMMITTEDrecords_shouldProcessEvent() throws JsonProcessingException {
    LockAssert.TestHelper.makeAllAssertsPass(true);

    var eventRepository = this.replicationTestUtils.getEventRepository();

    var eventAge = LocalDateTime.now().minusDays(3);
    var event = TestUtils.createEvent(EventType.CREATE_SCHOOL.toString(), TestUtils.createSchool(), eventAge, eventRepository);

    jetStreamScheduler.findAndProcessEvents();

//    verify that the scheduler triggers event
    verify(choreographer, times(1)).handleEvent(
        argThat(handledEvent ->
            handledEvent.getEventId().equals(event.getEventId())
        )
    );
  }
}
