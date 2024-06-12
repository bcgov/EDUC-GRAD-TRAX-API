package ca.bc.gov.educ.api.trax.service.institute;

import ca.bc.gov.educ.api.trax.model.dto.institute.District;
import ca.bc.gov.educ.api.trax.model.entity.institute.DistrictEntity;
import ca.bc.gov.educ.api.trax.model.transformer.institute.DistrictTransformer;
import ca.bc.gov.educ.api.trax.repository.redis.DistrictRedisRepository;
import ca.bc.gov.educ.api.trax.util.EducGradTraxApiConstants;
import ca.bc.gov.educ.api.trax.util.RestUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
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
    private RestUtils restUtils;
    @Autowired
    private WebClient webClient;
    @Autowired
    DistrictRedisRepository districtRedisRepository;
    @Autowired
    DistrictTransformer districtTransformer;

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
        districtRedisRepository.saveAll(districtTransformer.transformToEntity(districts));
    }

}
