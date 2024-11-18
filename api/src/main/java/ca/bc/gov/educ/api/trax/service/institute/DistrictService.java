package ca.bc.gov.educ.api.trax.service.institute;

import ca.bc.gov.educ.api.trax.constant.CacheKey;
import ca.bc.gov.educ.api.trax.exception.ServiceException;
import ca.bc.gov.educ.api.trax.model.dto.institute.District;
import ca.bc.gov.educ.api.trax.model.dto.institute.SchoolDetail;
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

import java.util.ArrayList;
import java.util.Collections;
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
    SchoolService schoolService;
    @Autowired
    RESTService restService;

    public List<District> getDistrictsFromInstituteApi() {
        try {
            log.debug("****Before Calling Institute API");
            List<DistrictEntity> response = this.restService.get(constants.getAllDistrictsFromInstituteApiUrl(),
                    List.class, webClient);
            List<District> dList = districtTransformer.transformToDTO(response);
            List<District> districts = new ArrayList<>();
            District dist;
            for (District d : dList) {
                dist = getDistrictByIdFromInstituteApi(d.getDistrictId());
                districts.add(dist);
            }
            return districts;
        } catch (WebClientResponseException e) {
            log.warn(String.format("Error getting Districts from Institute API: %s", e.getMessage()));
        } catch (Exception e) {
            log.error(String.format("Error while calling institute-api: %s", e.getMessage()));
        }
        return Collections.emptyList();
    }

    public District getDistrictByIdFromInstituteApi(String districtId) {
        try {
            return this.restService.get(String.format(constants.getGetDistrictFromInstituteApiUrl(), districtId),
                    District.class, webClient);
        } catch (WebClientResponseException e) {
            log.warn(String.format("Error getting District from Institute API: %s", e.getMessage()));
        } catch (Exception e) {
            log.error(String.format("Error while calling institute-api: %s", e.getMessage()));
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

    public District getDistrictByDistNoFromRedisCache(String districtNumber) {
        log.debug("**** Getting district by district no. from Redis Cache.");
        return districtRedisRepository.findByDistrictNumber(districtNumber).map(districtTransformer::transformToDTO).orElse(null);
    }

    public District getDistrictByIdFromRedisCache(String districtId) {
        log.debug("**** Getting district by ID from Redis Cache.");
        return districtRedisRepository.findById(districtId).map(districtTransformer::transformToDTO).orElse(null);
    }

    public List<District> getDistrictsBySchoolCategoryCode(String schoolCategoryCode) {
        List<SchoolDetail> schoolDetails;

        if (schoolCategoryCode.isBlank() || schoolCategoryCode.isEmpty())
            schoolDetails = schoolService.getSchoolDetailsFromRedisCache();
        else
            schoolDetails = schoolService.getSchoolDetailsBySchoolCategoryCode(schoolCategoryCode);

        List<District> districts = new ArrayList<>();
        for (SchoolDetail schoolDetail : schoolDetails) {
            districts.add(getDistrictByIdFromRedisCache(schoolDetail.getDistrictId()));
        }
        return districts;
    }

    /**
     * Updates the district details in the cache
     * based on schoolId
     * @param districtId the district id guid
     */
    public void updateDistrictCache(String districtId) throws ServiceException {
        log.debug(String.format("Updating district %s in cache.",  districtId));
        District district = this.restService.get(String.format(constants.getGetDistrictFromInstituteApiUrl(), districtId),
                District.class, webClient);
        districtRedisRepository.save(this.districtTransformer.transformToEntity(district));
    }
}
