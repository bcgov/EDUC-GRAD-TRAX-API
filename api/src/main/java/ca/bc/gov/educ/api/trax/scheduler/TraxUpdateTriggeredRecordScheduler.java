package ca.bc.gov.educ.api.trax.scheduler;

import ca.bc.gov.educ.api.trax.service.TraxUpdateService;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockAssert;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TraxUpdateTriggeredRecordScheduler {
    private final TraxUpdateService traxUpdateService;

    public TraxUpdateTriggeredRecordScheduler(final TraxUpdateService traxUpdateService) {
        this.traxUpdateService = traxUpdateService;
    }

    @Scheduled(cron = "${cron.scheduled.process.jobs.stan.run}") // every 5 minute
    @SchedulerLock(name = "PROCESS_TRAX_UPDATE_IN_GRAD_RECORDS", lockAtLeastFor = "${cron.scheduled.process.events.stan.lockAtLeastFor}", lockAtMostFor = "${cron.scheduled.process.events.stan.lockAtMostFor}")
    public void scheduledRunForTraxUpdates() {
        LockAssert.assertLocked();
        final var results = traxUpdateService.getOutstandingList();
        if (!results.isEmpty()) {
            results.stream()
                .forEach(ts -> {
                    try {
                        traxUpdateService.publishTraxUpdatedEvent(ts);
                        traxUpdateService.updateStatus(ts);
                    } catch (final Exception ex) {
                        log.error("Exception while trying to handle update_in_grad records", ex);
                    }
                });
        }
    }
}
