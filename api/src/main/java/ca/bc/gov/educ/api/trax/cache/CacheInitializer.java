package ca.bc.gov.educ.api.trax.cache;

import ca.bc.gov.educ.api.trax.service.institute.CodeService;
import ca.bc.gov.educ.api.trax.service.institute.DistrictService;
import ca.bc.gov.educ.api.trax.service.institute.SchoolService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

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
        schoolService.loadSchoolsIntoRedisCache(
                schoolService.getSchoolsFromInstituteApi()
        );
        log.info("Institutes loaded into Redis");

        districtService.loadDistrictsIntoRedisCache(
                districtService.getDistrictsFromInstituteApi()
        );
        log.info("Districts loaded into Redis");

        codeService.loadSchoolCategoryCodesIntoRedisCache(
                codeService.getSchoolCategoryCodesFromInstituteApi()
        );
        log.info("School Category Codes loaded into Redis");
        codeService.loadSchoolFundingGroupCodesIntoRedisCache(
                codeService.getSchoolFundingGroupCodesFromInstituteApi()
        );
        log.info("School Funding Group Codes loaded into Redis");

        log.info("Redis Cache initialized!");
    }
}