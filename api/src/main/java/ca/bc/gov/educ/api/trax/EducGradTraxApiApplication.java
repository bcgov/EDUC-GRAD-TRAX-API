package ca.bc.gov.educ.api.trax;

import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableScheduling
@EnableRetry
@EnableSchedulerLock(defaultLockAtMostFor = "1s")
@EnableJpaRepositories
public class EducGradTraxApiApplication {

    private static Logger logger = LoggerFactory.getLogger(EducGradTraxApiApplication.class);

    public static void main(String[] args) {
        logger.debug("########Starting API");
        SpringApplication.run(EducGradTraxApiApplication.class, args);
        logger.debug("########Started API");
    }

}