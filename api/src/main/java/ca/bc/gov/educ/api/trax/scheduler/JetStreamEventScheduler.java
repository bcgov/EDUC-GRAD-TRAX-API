package ca.bc.gov.educ.api.trax.scheduler;

import ca.bc.gov.educ.api.trax.choreographer.ChoreographEventHandler;
import ca.bc.gov.educ.api.trax.exception.TraxAPIRuntimeException;
import ca.bc.gov.educ.api.trax.messaging.jetstream.Publisher;
import ca.bc.gov.educ.api.trax.model.entity.EventEntity;
import ca.bc.gov.educ.api.trax.model.entity.TraxUpdatedPubEvent;
import ca.bc.gov.educ.api.trax.repository.TraxUpdatedPubEventRepository;
import ca.bc.gov.educ.api.trax.repository.EventRepository;
import ca.bc.gov.educ.api.trax.util.EducGradTraxApiConstants;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockAssert;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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
    /**
     * The TraxUpdatedPubEvent repository
     */
    private final TraxUpdatedPubEventRepository traxUpdatedPubEventRepository;

    private final ChoreographEventHandler choreographer;

    private final Publisher publisher;

    private final EducGradTraxApiConstants constants;

    /**
     * Instantiates a new Stan event scheduler.
     *
     * @param eventRepository the event repository
     * @param choreographer   the choreographer
     */
    public JetStreamEventScheduler(final EventRepository eventRepository,
                                   final TraxUpdatedPubEventRepository traxUpdatedPubEventRepository,
                                   final ChoreographEventHandler choreographer, Publisher publisher,
                                   final EducGradTraxApiConstants constants) {
        this.eventRepository = eventRepository;
        this.traxUpdatedPubEventRepository = traxUpdatedPubEventRepository;
        this.choreographer = choreographer;
        this.publisher = publisher;
        this.constants = constants;
    }
    
    @Scheduled(cron = "${cron.scheduled.process.events.grad-to-trax.run}")
    @SchedulerLock(name = "PROCESS_CHOREOGRAPHED_EVENTS_FROM_JET_STREAM", lockAtLeastFor = "${cron.scheduled.process.events.grad-to-trax.lockAtLeastFor}", lockAtMostFor = "${cron.scheduled.process.events.grad-to-trax.lockAtMostFor}")
    public void findAndProcessEvents() {
        LockAssert.assertLocked();
        log.debug("PROCESS_CHOREOGRAPHED_EVENTS_FROM_JET_STREAM: started - cron {}, lockAtMostFor {}", constants.getGradToTraxCronRun(), constants.getGradToTraxLockAtMostFor());
        final var results = this.eventRepository.fetchByEventStatus(List.of(DB_COMMITTED.toString()), constants.getGradToTraxProcessingThreshold());
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

    @Scheduled(cron = "${cron.scheduled.process.events.trax-to-grad.run}")
    @SchedulerLock(name = "PUBLISH_TRAX_UPDATED_EVENTS_TO_JET_STREAM", lockAtLeastFor = "${cron.scheduled.process.events.trax-to-grad.lockAtLeastFor}", lockAtMostFor = "${cron.scheduled.process.events.trax-to-grad.lockAtMostFor}")
    public void findAndPublishGradStatusEventsToJetStream() {
        LockAssert.assertLocked();
        log.debug("PUBLISH_TRAX_UPDATED_EVENTS_TO_JET_STREAM: started - cron {}, lockAtMostFor {}", constants.getTraxToGradCronRun(), constants.getTraxToGradLockAtMostFor());
        final var results = this.traxUpdatedPubEventRepository.fetchByEventStatus(List.of(DB_COMMITTED.toString()), constants.getTraxToGradProcessingThreshold());
        if (!results.isEmpty()) {
            var filteredList = results.stream().filter(el -> el.getUpdateDate().isBefore(LocalDateTime.now().minusMinutes(5))).toList();
            for (TraxUpdatedPubEvent el : filteredList) {
                try {
                    publisher.dispatchChoreographyEvent(el);
                } catch (final Exception ex) {
                    log.error("Exception while trying to handle TRAX updated message", ex);
                    throw new TraxAPIRuntimeException("Exception while trying to handle TRAX updated message: " + ex.getMessage());
                }
            }
            log.debug("PUBLISH_TRAX_UPDATED_EVENTS_TO_JET_STREAM: processing is completed");
        }
    }

}

