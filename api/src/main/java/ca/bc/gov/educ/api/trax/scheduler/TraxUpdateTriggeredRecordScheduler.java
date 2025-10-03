package ca.bc.gov.educ.api.trax.scheduler;

import ca.bc.gov.educ.api.trax.choreographer.ChoreographEventHandler;
import ca.bc.gov.educ.api.trax.exception.TraxAPIRuntimeException;
import ca.bc.gov.educ.api.trax.model.entity.EventEntity;
import ca.bc.gov.educ.api.trax.model.entity.TraxUpdateInGradEntity;
import ca.bc.gov.educ.api.trax.model.entity.TraxUpdatedPubEvent;
import ca.bc.gov.educ.api.trax.repository.EventRepository;
import ca.bc.gov.educ.api.trax.repository.TraxUpdateInGradRepository;
import ca.bc.gov.educ.api.trax.repository.TraxUpdatedPubEventRepository;
import ca.bc.gov.educ.api.trax.service.TraxUpdateService;
import ca.bc.gov.educ.api.trax.util.EducGradTraxApiConstants;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockAssert;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

import static ca.bc.gov.educ.api.trax.constant.EventStatus.DB_COMMITTED;

@Component
@Slf4j
public class TraxUpdateTriggeredRecordScheduler {
    private final TraxUpdateService traxUpdateService;
    private final  TraxUpdatedPubEventRepository traxUpdatedPubEventRepository;
    private final EventRepository eventRepository;
    private final EducGradTraxApiConstants constants;
    private final ChoreographEventHandler choreographer;

    public TraxUpdateTriggeredRecordScheduler(final TraxUpdateService traxUpdateService, TraxUpdatedPubEventRepository traxUpdatedPubEventRepository, EventRepository eventRepository,
                                              final EducGradTraxApiConstants constants, ChoreographEventHandler choreographer) {
        this.traxUpdateService = traxUpdateService;
        this.traxUpdatedPubEventRepository = traxUpdatedPubEventRepository;
        this.eventRepository = eventRepository;
        this.constants = constants;
        this.choreographer = choreographer;
    }

    @Scheduled(cron = "${cron.scheduled.process.trigger-jobs.read-trax-update.run}") // every 5 minute
    @SchedulerLock(name = "PROCESS_TRAX_UPDATE_IN_GRAD_RECORDS", lockAtLeastFor = "${cron.scheduled.process.trigger-jobs.read-trax-update.lockAtLeastFor}", lockAtMostFor = "${cron.scheduled.process.trigger-jobs.read-trax-update.lockAtMostFor}")
    public void scheduledRunForTraxUpdates() {
        LockAssert.assertLocked();

        log.info("Querying for TRAX records to process");
        if (this.traxUpdatedPubEventRepository.findByStatusIn(List.of(DB_COMMITTED.toString()), 102).size() > 100) { // at max there will be 100 parallel sagas.
            log.info("Event count is greater than 100, so not processing any TRAX records");
            return;
        }

        final var results = this.eventRepository.fetchByEventStatus(List.of(DB_COMMITTED.toString()), constants.getGradToTraxProcessingThreshold());
        log.info("Number of records found to process from GRAD to TRAX: {}", results.size());
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

        log.info("Running query for TRAX to GRAD updates");
        final var resultsPubEvent = this.traxUpdatedPubEventRepository.fetchByEventStatus(List.of(DB_COMMITTED.toString()), constants.getTraxToGradProcessingThreshold());
        log.info("Number of records found to process from TRAX to GRAD: {}", results.size());
        if (!resultsPubEvent.isEmpty()) {
            var filteredList = resultsPubEvent.stream().filter(el -> el.getUpdateDate().isBefore(LocalDateTime.now().minusMinutes(5))).toList();
            filteredList.forEach(traxUpdateService::publishToJetStream);
            log.debug("PUBLISH_TRAX_UPDATED_EVENTS_TO_JET_STREAM: processing is completed");
        }
        
        log.debug("PROCESS_TRAX_UPDATE_IN_GRAD_RECORDS: started - cron {}, lockAtMostFor {}", constants.getTraxTriggersCronRun(), constants.getTraxTriggersLockAtMostFor());
        final var resultsOutstanding = traxUpdateService.getOutstandingList(constants.getTraxTriggersProcessingThreshold());
        log.info("Number of records found to process from TRAX: {}", resultsOutstanding.size());
        if (!resultsOutstanding.isEmpty()) {
            try {
                log.info("Saving {} events for TRAX to GRAD updates", resultsOutstanding.size());
                var events = traxUpdateService.writeTraxUpdatedEvent(resultsOutstanding);
                log.info("Saved {} events for TRAX to GRAD", resultsOutstanding.size());
                log.info("Updating {} event statuses for TRAX to GRAD", resultsOutstanding.size());
                traxUpdateService.updateStatuses(resultsOutstanding);
                log.info("Updated {} event statuses for TRAX to GRAD", resultsOutstanding.size());
                
                if(!events.isEmpty()) {
                    log.info("Publishing {} events for TRAX to GRAD to jet stream", resultsOutstanding.size());
                    events.forEach(traxUpdateService::publishToJetStream);
                    log.info("Published {} events for TRAX to GRAD to jet stream", resultsOutstanding.size());
                }
            } catch (final Exception ex) {
                log.error("Exception while trying to handle update_in_grad records", ex);
                throw new TraxAPIRuntimeException("Exception while trying to handle update_in_grad records: " + ex.getMessage());
            }
            log.debug("PROCESS_TRAX_UPDATE_IN_GRAD_RECORDS: processing is completed");
        }
    }
}
