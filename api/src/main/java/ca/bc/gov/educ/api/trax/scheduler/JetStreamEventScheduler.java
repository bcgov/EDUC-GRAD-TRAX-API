package ca.bc.gov.educ.api.trax.scheduler;

import ca.bc.gov.educ.api.trax.choreographer.ChoreographEventHandler;
import ca.bc.gov.educ.api.trax.exception.TraxAPIRuntimeException;
import ca.bc.gov.educ.api.trax.model.entity.EventEntity;
import ca.bc.gov.educ.api.trax.repository.EventRepository;
import ca.bc.gov.educ.api.trax.util.EducGradTraxApiConstants;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockAssert;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static ca.bc.gov.educ.api.trax.constant.EventStatus.DB_COMMITTED;

@Component
@Slf4j
public class JetStreamEventScheduler {

  /**
   * The EventEntity repository.
   */
  private final EventRepository eventRepository;

  private final ChoreographEventHandler choreographer;

  private final EducGradTraxApiConstants constants;

  /**
   * Instantiates a new Stan event scheduler.
   *
   * @param eventRepository the event repository
   * @param choreographer   the choreographer
   */
  public JetStreamEventScheduler(final EventRepository eventRepository,
                                 final ChoreographEventHandler choreographer,
                                 final EducGradTraxApiConstants constants) {
    this.eventRepository = eventRepository;
    this.choreographer = choreographer;
    this.constants = constants;
  }

  @Scheduled(cron = "${cron.scheduled.process.events.grad-to-trax.run}")
  @SchedulerLock(name = "PROCESS_CHOREOGRAPHED_EVENTS_FROM_JET_STREAM", lockAtLeastFor = "${cron.scheduled.process.events.grad-to-trax.lockAtLeastFor}", lockAtMostFor = "${cron.scheduled.process.events.grad-to-trax.lockAtMostFor}")
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void findAndProcessEvents() {
    LockAssert.assertLocked();
    log.info("Running query for GRAD to TRAX updates");
    final var results = this.eventRepository.fetchByEventStatus(List.of(DB_COMMITTED.toString()), constants.getGradToTraxProcessingThreshold());
    log.info("Number of records found to process {}", results.size());
    if (!results.isEmpty()) {
      var filteredList = results.stream().filter(el -> el.getUpdateDate().isBefore(LocalDateTime.now().minusMinutes(5))).toList();
      for (EventEntity e : filteredList) {
        try {
          choreographer.handleEvent(e);
        } catch (final Exception ex) {
          log.error("Exception while trying to handle GRAD updated message", ex);
          throw new TraxAPIRuntimeException("Exception while trying to handle GRAD updated message: " + ex.getMessage());
        }
      }
      log.debug("PROCESS_CHOREOGRAPHED_EVENTS_FROM_JET_STREAM: processing is completed");
    }
  }
}
