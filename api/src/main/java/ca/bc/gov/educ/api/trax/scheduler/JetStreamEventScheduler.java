package ca.bc.gov.educ.api.trax.scheduler;

import ca.bc.gov.educ.api.trax.choreographer.ChoreographEventHandler;
import ca.bc.gov.educ.api.trax.messaging.jetstream.Publisher;
import ca.bc.gov.educ.api.trax.model.entity.Event;
import ca.bc.gov.educ.api.trax.model.entity.TraxUpdatedPubEvent;
import ca.bc.gov.educ.api.trax.repository.TraxUpdatedPubEventRepository;
import ca.bc.gov.educ.api.trax.repository.EventRepository;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockAssert;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static ca.bc.gov.educ.api.trax.constant.EventStatus.DB_COMMITTED;

/**
 * This class is responsible to check the PEN_MATCH_EVENT table periodically and publish messages to Jet Stream, if some them are not yet published
 * this is a very edge case scenario which will occur.
 */
@Component
@Slf4j
public class JetStreamEventScheduler {

    /**
     * The number of sql records to process at a time.
     */
    private static final int PAGE_OFFSET = 100;

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

    /**
     * Instantiates a new Stan event scheduler.
     *
     * @param eventRepository the event repository
     * @param choreographer   the choreographer
     */
    public JetStreamEventScheduler(final EventRepository eventRepository,
                                   final TraxUpdatedPubEventRepository traxUpdatedPubEventRepository,
                                   final ChoreographEventHandler choreographer, Publisher publisher) {
        this.eventRepository = eventRepository;
        this.traxUpdatedPubEventRepository = traxUpdatedPubEventRepository;
        this.choreographer = choreographer;
        this.publisher = publisher;
    }
    @Scheduled(cron = "${cron.scheduled.process.events.stan.run}") // every 5 minutes
    @SchedulerLock(name = "PROCESS_CHOREOGRAPHED_EVENTS_FROM_JET_STREAM", lockAtLeastFor = "${cron.scheduled.process.events.stan.lockAtLeastFor}", lockAtMostFor = "${cron.scheduled.process.events.stan.lockAtMostFor}")
    public void findAndProcessEvents() {
        LockAssert.assertLocked();
        Pageable sortedByCreationDate = PageRequest.of(0, PAGE_OFFSET, Sort.by("createDate"));
        Slice<Event> events = this.eventRepository.findAllByEventStatus(DB_COMMITTED.toString(), sortedByCreationDate);
        while(events.hasContent()){
            events.getContent()
                    .stream()
                    .filter(el -> el.getUpdateDate().isBefore(LocalDateTime.now().minusMinutes(5)))
                    .forEach(el -> {
                        try {
                            choreographer.handleEvent(el);
                        } catch (final Exception ex) {
                            log.error("Exception while trying to handle message", ex);
                        }
                    });
            if(events.hasNext()){
                events = this.eventRepository.findAllByEventStatus(DB_COMMITTED.toString(), PageRequest.of(events.getPageable().getPageNumber()+1, PAGE_OFFSET, Sort.by("createDate")));
            }
        }
    }


    @Scheduled(cron = "${cron.scheduled.process.events.stan.run}") // every 5 minutes
    @SchedulerLock(name = "PUBLISH_TRAX_UPDATED_EVENTS_TO_JET_STREAM", lockAtLeastFor = "${cron.scheduled.process.events.stan.lockAtLeastFor}", lockAtMostFor = "${cron.scheduled.process.events.stan.lockAtMostFor}")
    public void findAndPublishGradStatusEventsToJetStream() {
        LockAssert.assertLocked();
        Pageable pageable = PageRequest.of(0, PAGE_OFFSET);
        Slice<TraxUpdatedPubEvent> events = this.traxUpdatedPubEventRepository.findByEventStatus(DB_COMMITTED.toString(), pageable);
        while(events.hasContent()){
            events.getContent()
                    .stream()
                    .filter(el -> el.getUpdateDate().isBefore(LocalDateTime.now().minusMinutes(5)))
                    .forEach(el -> {
                        try {
                            publisher.dispatchChoreographyEvent(el);
                        } catch (final Exception ex) {
                            log.error("Exception while trying to handle message", ex);
                        }
                    });
            if(events.hasNext()){
                events = this.traxUpdatedPubEventRepository.findByEventStatus(DB_COMMITTED.toString(), PageRequest.of(events.getPageable().getPageNumber()+1, PAGE_OFFSET));
            }
        }
    }

}

