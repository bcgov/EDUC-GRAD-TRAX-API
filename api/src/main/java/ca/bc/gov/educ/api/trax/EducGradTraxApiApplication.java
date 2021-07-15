package ca.bc.gov.educ.api.trax;

import ca.bc.gov.educ.api.trax.model.dto.District;
import ca.bc.gov.educ.api.trax.model.dto.Psi;
import ca.bc.gov.educ.api.trax.model.dto.School;
import ca.bc.gov.educ.api.trax.model.entity.DistrictEntity;
import ca.bc.gov.educ.api.trax.model.entity.PsiEntity;
import ca.bc.gov.educ.api.trax.model.entity.SchoolEntity;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@SpringBootApplication
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableCaching
public class EducGradTraxApiApplication {

    private static Logger logger = LoggerFactory.getLogger(EducGradTraxApiApplication.class);

    public static void main(String[] args) {
        logger.debug("########Starting API");
        SpringApplication.run(EducGradTraxApiApplication.class, args);
        logger.debug("########Started API");
    }

    @Bean
    public ModelMapper modelMapper() {

        ModelMapper modelMapper = new ModelMapper();
        modelMapper.typeMap(SchoolEntity.class, School.class);
        modelMapper.typeMap(School.class, SchoolEntity.class);

        modelMapper.typeMap(DistrictEntity.class, District.class);
        modelMapper.typeMap(District.class, DistrictEntity.class);

        modelMapper.typeMap(PsiEntity.class, Psi.class);
        modelMapper.typeMap(Psi.class, PsiEntity.class);
        return modelMapper;
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean
    public WebClient webClient() {
        HttpClient client = HttpClient.create();
        client.warmup().block();
        return WebClient.builder().build();
    }

    @Configuration
    static
    class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {
        /**
         * Instantiates a new Web security configuration.
         * This makes sure that security context is propagated to async threads as well.
         */
        public WebSecurityConfiguration() {
            super();
            SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
        }

        @Override
        public void configure(WebSecurity web) {
            web.ignoring().antMatchers("/api/v1/api-docs-ui.html",
                    "/api/v1/swagger-ui/**", "/api/v1/api-docs/**",
                    "/actuator/health", "/actuator/prometheus", "/health");
        }
    }
}