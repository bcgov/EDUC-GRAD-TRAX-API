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
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service("instituteDistrictService")
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
    @Autowired
    CacheService cacheService;

    public List<District> getDistrictsFromInstituteApi() {
        try {
            log.debug("****Before Calling Institute API for districts");
            List<DistrictEntity> response = this.restService.get(constants.getAllDistrictsFromInstituteApiUrl(),
                    List.class, webClient);
            List<District> dList = districtTransformer.transformToDTO(response);
            List<District> districts = new ArrayList<>();
            District dist;
            for (District d : dList) {
                dist = getDistrictByIdFromInstituteApi(d.getDistrictId());
                if(dist != null) {
                    districts.add(dist);
                }
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
        if(StringUtils.isBlank(districtId)) { return null;}
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

    private List<District> loadDistrictFromInstituteApiIntoRedisCacheAsync() {
        List<District> districts = getDistrictsFromInstituteApi();
        if(!CollectionUtils.isEmpty(districts)) {
            cacheService.loadDistrictsIntoRedisCacheAsync(districtTransformer.transformToEntity(districts));
        }
        return districts;
    }

    public void loadDistrictsIntoRedisCache(List<District> districts) {
        if(!CollectionUtils.isEmpty(districts)) {
            log.info(String.format("%s Districts fetched from Institute API.", districts.size()));
            cacheService.loadDistrictsIntoRedisCache(districtTransformer.transformToEntity(districts));
        }
    }

    public List<District> getDistrictsFromRedisCache() {
        log.debug("**** Getting districts from Redis Cache.");
        List<District> districts =  districtTransformer.transformToDTO(districtRedisRepository.findAll());
        return CollectionUtils.isEmpty(districts) ? loadDistrictFromInstituteApiIntoRedisCacheAsync() : districts;
    }

    public void initializeDistrictCache(boolean force) {
        serviceHelper.initializeCache(force, CacheKey.DISTRICT_CACHE, this);
    }

    public District getDistrictByDistNoFromRedisCache(String districtNumber) {
        if(StringUtils.isBlank(districtNumber)) { return null;}
        log.debug("**** Getting district by district no. from Redis Cache.");
        return districtRedisRepository.findByDistrictNumber(districtNumber)
                .map(districtTransformer::transformToDTO)
                .orElseGet(() -> {
                    log.debug("district not found in cache for districtNumber: {}, , fetched from API.", districtNumber);
                    District district = getDistrictsFromInstituteApi().stream().filter(entry -> entry.getDistrictNumber().equals(districtNumber)).findFirst().orElse(null);
                    if(district != null) {
                        updateDistrictCache(district);
                    }
                    return district;
                });
    }

    public District getDistrictByIdFromRedisCache(String districtId) {
        if(StringUtils.isBlank(districtId)) { return null;}
        log.debug("**** Getting district by ID from Redis Cache.");
        return districtRedisRepository.findById(districtId)
                .map(districtTransformer::transformToDTO)
                .orElseGet(() -> {
                    log.debug("district not found in cache for districtId: {}, , fetched from API.", districtId);
                    District district = getDistrictByIdFromInstituteApi(districtId);
                    if(district != null) {
                        updateDistrictCache(district);
                    }
                    return district;
                });
    }

    public List<District> getDistrictsBySchoolCategoryCode(String schoolCategoryCode) {
        if(StringUtils.isBlank(schoolCategoryCode)) { return Collections.emptyList();}
        List<SchoolDetail> schoolDetails;

        if (schoolCategoryCode.isBlank() || schoolCategoryCode.isEmpty())
            schoolDetails = schoolService.getSchoolDetailsFromRedisCache();
        else
            schoolDetails = schoolService.getSchoolDetailsBySchoolCategoryCode(schoolCategoryCode);

        List<District> districts = new ArrayList<>();
        if(!CollectionUtils.isEmpty(schoolDetails)) {
            List<String> districtIds = schoolDetails.stream()
                    .map(schoolDetail -> schoolDetail.getDistrictId())
                    .distinct()
                    .collect(Collectors.toList());
            for (String districtId : districtIds) {
                districts.add(getDistrictByIdFromRedisCache(districtId));
            }
        }
        return districts;
    }

    /**
     * Updates the district details in the cache
     * based on schoolId
     * @param districtId the district id guid
     */
    public void updateDistrictCache(String districtId) throws ServiceException {
        if(StringUtils.isNotBlank(districtId)) {
            District district = getDistrictByIdFromInstituteApi(districtId);
            updateDistrictCache(district);
        }
    }

    /**
     * Updates the district details in the cache
     */
    private void updateDistrictCache(District district) throws ServiceException {
        if(district != null) {
            log.debug(String.format("Updating district %s in cache.",  district.getDistrictId()));
            districtRedisRepository.save(this.districtTransformer.transformToEntity(district));
            log.debug(String.format("Updated district %s in cache.",  district.getDistrictId()));
        }
    }
}
