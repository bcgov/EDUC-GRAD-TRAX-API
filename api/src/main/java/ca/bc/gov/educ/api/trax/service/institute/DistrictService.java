package ca.bc.gov.educ.api.trax.service.institute;

import ca.bc.gov.educ.api.trax.model.dto.institute.District;
import ca.bc.gov.educ.api.trax.model.dto.institute.School;
import ca.bc.gov.educ.api.trax.model.entity.institute.DistrictEntity;
import ca.bc.gov.educ.api.trax.model.transformer.institute.DistrictTransformer;
import ca.bc.gov.educ.api.trax.repository.redis.DistrictRedisRepository;
import ca.bc.gov.educ.api.trax.util.EducGradTraxApiConstants;
import ca.bc.gov.educ.api.trax.util.RestUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service("InstituteDistrictService")
public class DistrictService {

    @Autowired
    private EducGradTraxApiConstants constants;
    @Autowired
    private RestUtils restUtils;
    @Autowired
    private WebClient webClient;
    @Autowired
    DistrictRedisRepository districtRedisRepository;
    @Autowired
    DistrictTransformer districtTransformer;
    @Autowired
    RedisTemplate<String, String> redisTemplate;

    public List<District> getDistrictsFromInstituteApi() {
        try {
            log.debug("****Before Calling Institute API");
            List<DistrictEntity> districts =
                    webClient.get()
                            .uri(constants.getAllDistrictsFromInstituteApiUrl())
                            .headers(h -> h.setBearerAuth(restUtils.getTokenResponseObject(
                                    constants.getInstituteClientId(),
                                    constants.getInstituteClientSecret()
                            ).getAccess_token()))
                            .retrieve()
                            .bodyToMono(new ParameterizedTypeReference<List<DistrictEntity>>() {
                            }).block();
            //assert districts != null;
            //log.debug("# of Districts: " + districts.size());
            return districtTransformer.transformToDTO(districts);
        } catch (WebClientResponseException e) {
            log.warn(String.format("Error getting Common School List: %s", e.getMessage()));
        } catch (Exception e) {
            log.error(String.format("Error while calling school-api: %s", e.getMessage()));
        }
        return null;
    }

    public void loadDistrictsIntoRedisCache(List<District> districts) {
        districtRedisRepository
                .saveAll(districtTransformer.transformToEntity(districts));
    }

    public List<District> getSchoolsFromRedisCache() {
        log.debug("**** Getting districts from Redis Cache.");
        return  districtTransformer.transformToDTO(districtRedisRepository.findAll());
    }

    public void initializeDistrictCache(boolean force) {
        if ("READY".compareToIgnoreCase(Objects.requireNonNull(redisTemplate.opsForValue().get("DISTRICT_CACHE"))) == 0) {
            log.info("DISTRICT_CACHE status: READY");
            if (force) {
                log.info("Force Flag is true. Reloading DISTRICT_CACHE...");
                loadDistrictsIntoRedisCache(getDistrictsFromInstituteApi());
                log.info("SUCCESS! - DISTRICT_CACHE is now READY");
            } else {
                log.info("Force Flag is false. Skipping DISTRICT_CACHE reload");
            }
        } else {
            log.info("Loading DISTRICT_CACHE...");
            loadDistrictsIntoRedisCache(getDistrictsFromInstituteApi());
            redisTemplate.opsForValue().set("DISTRICT_CACHE", "READY");
            log.info("SUCCESS! - DISTRICT_CACHE is now READY");
        }
    }

}
