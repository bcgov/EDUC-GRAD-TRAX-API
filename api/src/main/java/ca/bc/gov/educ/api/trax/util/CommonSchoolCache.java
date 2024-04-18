package ca.bc.gov.educ.api.trax.util;

import ca.bc.gov.educ.api.trax.model.dto.CommonSchool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class CommonSchoolCache {

    private Map<String, CommonSchool> schools;
    private LocalDateTime cacheExpiry;
    private WebClient webClient;
    private EducGradTraxApiConstants constants;

    @Autowired
    public CommonSchoolCache(@Qualifier("traxClient") WebClient webClient, EducGradTraxApiConstants constants) {
        this.webClient = webClient;
        this.constants = constants;
    }

    public CommonSchool getSchoolByMincode(String mincode) {
        cacheCheck();
        return schools.get(mincode);
    }

    public List<CommonSchool> getAllCommonSchools() {
        cacheCheck();
        return new ArrayList<>(schools.values());
    }

    private void cacheCheck() {
        if (schools == null || schools.isEmpty() || cacheHasExpired()) {
            populateCache();
        }
    }

    private boolean cacheHasExpired() {
        return cacheExpiry == null || cacheExpiry.isBefore(LocalDateTime.now());
    }

    private void updateCacheExpiry() {
        this.cacheExpiry = LocalDateTime.now().plus(constants.getSchoolCacheExpiryInMins(), ChronoUnit.MINUTES);
    }

    private void populateCache() {
        // get schools
        try {
            List<CommonSchool> commonSchools = getCommonSchools();
            // create map
            this.schools = commonSchools.stream()
                    .collect(Collectors.toMap(commonSchool -> (commonSchool.getDistNo() + commonSchool.getSchlNo()), Function.identity()));
            // update cache
            updateCacheExpiry();
        } catch (Exception e) {
            log.error(String.format("Error while attempting to populate school cache: %s", e.getCause().toString()));
        }
    }

    private List<CommonSchool> getCommonSchools() {
        // avoid npe
        List<CommonSchool> commonSchools = new ArrayList<>();
        try {
            commonSchools = webClient.get().uri(constants.getAllSchoolSchoolApiUrl())
                    .headers(h -> {
                        h.set(EducGradTraxApiConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID());
                    })
                    .retrieve().bodyToMono(new ParameterizedTypeReference<List<CommonSchool>>() {
                    }).block();
        } catch (Exception e) {
            log.error(String.format("Error while attempting to populate school cache: %s", e.getCause().toString()));
        }
        return commonSchools;
    }

}
