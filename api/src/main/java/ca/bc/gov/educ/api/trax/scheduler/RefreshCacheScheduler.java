package ca.bc.gov.educ.api.trax.scheduler;

import ca.bc.gov.educ.api.trax.service.institute.CodeService;
import ca.bc.gov.educ.api.trax.service.institute.DistrictService;
import ca.bc.gov.educ.api.trax.service.institute.SchoolService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockAssert;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RefreshCacheScheduler {

    SchoolService schoolService;
    DistrictService districtService;
    CodeService codeService;

    @Autowired
    public RefreshCacheScheduler(SchoolService schoolService, DistrictService districtService, CodeService codeService) {
        this.schoolService = schoolService;
        this.districtService = districtService;
        this.codeService = codeService;
    }

    @Scheduled(cron = "${cron.scheduled.process.update-cache.run}")
    @SchedulerLock(name = "RefreshCacheLock",
            lockAtLeastFor = "${cron.scheduled.process.update-cache.lockAtLeastFor}", lockAtMostFor = "${cron.scheduled.process.update-cache.lockAtMostFor}")
    @Transactional
    public void refreshCache() {
        try {
            log.debug("Refreshing cache on scheduled process.");
            LockAssert.assertLocked();
            schoolService.initializeSchoolCache(true);
            districtService.initializeDistrictCache(true);
            codeService.initializeSchoolCategoryCodeCache(true);
            codeService.initializeSchoolFundingGroupCodeCache(true);
        } catch (Exception e) {
            log.error(e.getMessage());
        }

    }

}
