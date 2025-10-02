package ca.bc.gov.educ.api.trax.scheduler;

import ca.bc.gov.educ.api.trax.model.entity.TraxUpdateInGradEntity;
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

import java.util.List;

import static ca.bc.gov.educ.api.trax.constant.EventStatus.DB_COMMITTED;

@Component
@Slf4j
public class TraxUpdateTriggeredRecordScheduler {
    private final TraxUpdateService traxUpdateService;
    private final  TraxUpdatedPubEventRepository traxUpdatedPubEventRepository;

    private final EducGradTraxApiConstants constants;

    public TraxUpdateTriggeredRecordScheduler(final TraxUpdateService traxUpdateService, TraxUpdatedPubEventRepository traxUpdatedPubEventRepository,
                                              final EducGradTraxApiConstants constants) {
        this.traxUpdateService = traxUpdateService;
        this.traxUpdatedPubEventRepository = traxUpdatedPubEventRepository;
        this.constants = constants;
    }

    @Scheduled(cron = "${cron.scheduled.process.trigger-jobs.read-trax-update.run}") // every 5 minute
    @SchedulerLock(name = "PROCESS_TRAX_UPDATE_IN_GRAD_RECORDS", lockAtLeastFor = "${cron.scheduled.process.trigger-jobs.read-trax-update.lockAtLeastFor}", lockAtMostFor = "${cron.scheduled.process.trigger-jobs.read-trax-update.lockAtMostFor}")
    public void scheduledRunForTraxUpdates() {
        LockAssert.assertLocked();

        log.info("Querying for TRAX records to process");
        if (this.traxUpdatedPubEventRepository.findByStatusIn(List.of(DB_COMMITTED.toString()), 101).size() > 100) { // at max there will be 100 parallel sagas.
            log.info("Event count is greater than 100, so not processing any TRAX records");
            return;
        }
        
        log.debug("PROCESS_TRAX_UPDATE_IN_GRAD_RECORDS: started - cron {}, lockAtMostFor {}", constants.getTraxTriggersCronRun(), constants.getTraxTriggersLockAtMostFor());
        final var results = traxUpdateService.getOutstandingList(constants.getTraxTriggersProcessingThreshold());
        log.info("Number of records found to process from TRAX: {}", results.size());
        if (!results.isEmpty()) {
            try {
                var events = traxUpdateService.writeTraxUpdatedEvent(results);
                if(!events.isEmpty()) {
                    events.forEach(traxUpdateService::publishToJetStream);
                }
            } catch (final Exception ex) {
                log.error("Exception while trying to handle update_in_grad records", ex);
            }
            log.debug("PROCESS_TRAX_UPDATE_IN_GRAD_RECORDS: processing is completed");
        }
    }
}
