package ca.bc.gov.educ.api.trax.service.institute;

import ca.bc.gov.educ.api.trax.constant.CacheKey;
import ca.bc.gov.educ.api.trax.model.dto.institute.District;
import ca.bc.gov.educ.api.trax.model.entity.institute.DistrictEntity;
import ca.bc.gov.educ.api.trax.model.transformer.institute.DistrictTransformer;
import ca.bc.gov.educ.api.trax.repository.redis.DistrictRedisRepository;
import ca.bc.gov.educ.api.trax.service.RESTService;
import ca.bc.gov.educ.api.trax.util.EducGradTraxApiConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

@Slf4j
@Service("InstituteDistrictService")
public class DistrictService {

    @Autowired
    private EducGradTraxApiConstants constants;
    @Autowired
    @Qualifier("instituteWebClient")
    private WebClient webClient;
    @Autowired
    DistrictRedisRepository districtRedisRepository;
    @Autowired
    DistrictTransformer districtTransformer;
    @Autowired
    ServiceHelper<DistrictService> serviceHelper;
    @Autowired
    RESTService restService;

    public List<District> getDistrictsFromInstituteApi() {
        try {
            log.debug("****Before Calling Institute API");
            List<DistrictEntity> response = this.restService.get(constants.getAllDistrictsFromInstituteApiUrl(),
                    List.class, webClient);
            return districtTransformer.transformToDTO(response);
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
        log.info(String.format("%s Districts Loaded into cache.", districts.size()));
    }

    public List<District> getDistrictsFromRedisCache() {
        log.debug("**** Getting districts from Redis Cache.");
        return  districtTransformer.transformToDTO(districtRedisRepository.findAll());
    }

    public void initializeDistrictCache(boolean force) {
        serviceHelper.initializeCache(force, CacheKey.DISTRICT_CACHE, this);
    }
}
