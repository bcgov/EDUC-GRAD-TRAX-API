package ca.bc.gov.educ.api.trax.scheduler;

import ca.bc.gov.educ.api.trax.repository.TraxUpdatedPubEventRepository;
import ca.bc.gov.educ.api.trax.service.EventHistoryService;
import ca.bc.gov.educ.api.trax.util.EducGradTraxApiConstants;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockAssert;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class PurgeOldRecordsScheduler {

    private final EventHistoryService eventHistoryService;
    private final TraxUpdatedPubEventRepository traxUpdatedPubEventRepository;
    private final EducGradTraxApiConstants constants;

    @Autowired
    public PurgeOldRecordsScheduler(EventHistoryService eventHistoryService, TraxUpdatedPubEventRepository traxUpdatedPubEventRepository, EducGradTraxApiConstants constants) {
        this.eventHistoryService = eventHistoryService;
        this.traxUpdatedPubEventRepository = traxUpdatedPubEventRepository;
        this.constants = constants;
    }

    @Scheduled(cron = "${cron.scheduled.process.purge-old-records.run}")
    @SchedulerLock(name = "PurgeOldRecordsLock",
            lockAtLeastFor = "PT1H", lockAtMostFor = "PT1H") //midnight job so lock for an hour
    @Transactional
    public void purgeOldRecords() {
        try {
            LockAssert.assertLocked();
            final LocalDateTime createDateToCompare = this.calculateCreateDateBasedOnStaleEventInDays();
            this.eventHistoryService.purgeOldEventAndEventHistoryRecords(createDateToCompare);
            this.traxUpdatedPubEventRepository.deleteByCreateDateBefore(createDateToCompare);
        } catch (Exception e) {
            log.error(e.getMessage());
        }

    }

    private LocalDateTime calculateCreateDateBasedOnStaleEventInDays() {
        final LocalDateTime currentTime = LocalDateTime.now();
        return currentTime.minusDays(this.constants.getRecordsStaleInDays());
    }
}
