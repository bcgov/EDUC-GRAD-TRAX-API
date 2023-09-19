package ca.bc.gov.educ.api.trax.scheduler;

import ca.bc.gov.educ.api.trax.model.entity.TraxUpdateInGradEntity;
import ca.bc.gov.educ.api.trax.service.TraxUpdateService;
import ca.bc.gov.educ.api.trax.util.EducGradTraxApiConstants;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockAssert;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TraxUpdateTriggeredRecordScheduler {
    private final TraxUpdateService traxUpdateService;

    private final EducGradTraxApiConstants constants;

    public TraxUpdateTriggeredRecordScheduler(final TraxUpdateService traxUpdateService,
                                              final EducGradTraxApiConstants constants) {
        this.traxUpdateService = traxUpdateService;
        this.constants = constants;
    }

    @Scheduled(cron = "${cron.scheduled.process.trigger-jobs.read-trax-update.run}") // every 5 minute
    @SchedulerLock(name = "PROCESS_TRAX_UPDATE_IN_GRAD_RECORDS", lockAtLeastFor = "${cron.scheduled.process.trigger-jobs.read-trax-update.lockAtLeastFor}", lockAtMostFor = "${cron.scheduled.process.trigger-jobs.read-trax-update.lockAtMostFor}")
    public void scheduledRunForTraxUpdates() {
        log.debug("PROCESS_TRAX_UPDATE_IN_GRAD_RECORDS: started - cron {}, lockAtMostFor {}", constants.getTraxTriggersCronRun(), constants.getTraxTriggersLockAtMostFor());
        LockAssert.assertLocked();
        final var results = traxUpdateService.getOutstandingList();
        if (!results.isEmpty()) {
            int cnt = 0;
            for (TraxUpdateInGradEntity ts : results) {
                if (cnt++ >= constants.getTraxTriggersProcessingThreshold()) {
                    log.info(" ==> Reached the processing threshold of {}", constants.getTraxTriggersProcessingThreshold());
                    break;
                }
                try {
                    traxUpdateService.publishTraxUpdatedEvent(ts);
                    traxUpdateService.updateStatus(ts);
                } catch (final Exception ex) {
                    log.error("Exception while trying to handle update_in_grad records", ex);
                }
            }
            log.debug("PROCESS_TRAX_UPDATE_IN_GRAD_RECORDS: processing is completed");
        }
    }
}
