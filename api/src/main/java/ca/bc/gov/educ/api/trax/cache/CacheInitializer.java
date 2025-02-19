package ca.bc.gov.educ.api.trax.cache;

import ca.bc.gov.educ.api.trax.service.institute.CodeService;
import ca.bc.gov.educ.api.trax.service.institute.DistrictService;
import ca.bc.gov.educ.api.trax.service.institute.SchoolService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("!test")
@Slf4j
@Component
public class CacheInitializer implements CommandLineRunner {

    @Autowired
    SchoolService schoolService;
    @Autowired
    DistrictService districtService;
    @Autowired
    CodeService codeService;

    @Override
    public void run(String... args) {

        schoolService.initializeSchoolCache(false);
        districtService.initializeDistrictCache(false);
        codeService.initializeSchoolCategoryCodeCache(false);
        codeService.initializeSchoolFundingGroupCodeCache(false);

        log.info("Redis Cache initialized!");
    }
}