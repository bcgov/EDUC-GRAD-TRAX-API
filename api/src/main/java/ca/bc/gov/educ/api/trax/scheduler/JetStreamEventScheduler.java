package ca.bc.gov.educ.api.trax.scheduler;

import ca.bc.gov.educ.api.trax.choreographer.ChoreographEventHandler;
import ca.bc.gov.educ.api.trax.messaging.jetstream.Publisher;
import ca.bc.gov.educ.api.trax.model.entity.Event;
import ca.bc.gov.educ.api.trax.model.entity.TraxUpdatedPubEvent;
import ca.bc.gov.educ.api.trax.repository.TraxUpdatedPubEventRepository;
import ca.bc.gov.educ.api.trax.repository.EventRepository;
import ca.bc.gov.educ.api.trax.util.EducGradTraxApiConstants;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockAssert;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static ca.bc.gov.educ.api.trax.constant.EventStatus.DB_COMMITTED;

@Component
@Slf4j
public class JetStreamEventScheduler {

    /**
     * The Event repository.
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
        log.debug("PROCESS_CHOREOGRAPHED_EVENTS_FROM_JET_STREAM: started - cron {}, lockAtMostFor {}", constants.getGradToTraxCronRun(), constants.getGradToTraxLockAtMostFor());
        LockAssert.assertLocked();
        final var results = this.eventRepository.findAllByEventStatusOrderByCreateDate(DB_COMMITTED.toString());
        if (!results.isEmpty()) {
            int cnt = 0;
            for (Event e : results) {
                if (cnt++ >= constants.getGradToTraxProcessingThreshold()) {
                    log.info(" ==> Reached the processing threshold of {}", constants.getGradToTraxProcessingThreshold());
                    break;
                }
                try {
                    choreographer.handleEvent(e);
                } catch (final Exception ex) {
                    log.error("Exception while trying to handle GRAD updated message", ex);
                }
            }
            log.debug("PROCESS_CHOREOGRAPHED_EVENTS_FROM_JET_STREAM: processing is completed");
        }
    }

    @Scheduled(cron = "${cron.scheduled.process.events.trax-to-grad.run}")
    @SchedulerLock(name = "PUBLISH_TRAX_UPDATED_EVENTS_TO_JET_STREAM", lockAtLeastFor = "${cron.scheduled.process.events.trax-to-grad.lockAtLeastFor}", lockAtMostFor = "${cron.scheduled.process.events.trax-to-grad.lockAtMostFor}")
    public void findAndPublishGradStatusEventsToJetStream() {
        log.debug("PUBLISH_TRAX_UPDATED_EVENTS_TO_JET_STREAM: started - cron {}, lockAtMostFor {}", constants.getTraxToGradCronRun(), constants.getTraxToGradLockAtMostFor());
        LockAssert.assertLocked();
        final var results = this.traxUpdatedPubEventRepository.findByEventStatusOrderByCreateDate(DB_COMMITTED.toString());
        if (!results.isEmpty()) {
            int cnt = 0;
            for (TraxUpdatedPubEvent el : results) {
                if (cnt++ >= constants.getTraxToGradProcessingThreshold()) {
                    log.info(" ==> Reached the processing threshold of {}", constants.getTraxToGradProcessingThreshold());
                    break;
                }
                try {
                    publisher.dispatchChoreographyEvent(el);
                } catch (final Exception ex) {
                    log.error("Exception while trying to handle TRAX updated message", ex);
                }
            }
            log.debug("PUBLISH_TRAX_UPDATED_EVENTS_TO_JET_STREAM: processing is completed");
        }
    }

}

