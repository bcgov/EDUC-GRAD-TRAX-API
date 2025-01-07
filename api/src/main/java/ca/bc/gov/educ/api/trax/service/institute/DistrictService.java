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
import ca.bc.gov.educ.api.trax.util.SearchUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

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

    public List <District> getDistrictsBySearchCriteriaFromInstituteApi(String key, String value) {
        try {
            log.debug("****Before Calling Institute API");
            HashMap<String, String> params;
            HashMap<String, String> searchInput = new HashMap<>();
            searchInput.put(key, value);

            try {
                params  = SearchUtil.searchStringsToHTTPParams(searchInput);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }


            List <DistrictEntity> districtEntities = this.restService.get(constants.getDistrictsPaginated(),params,
                    List.class, webClient);
            return districtTransformer.transformToDTO(districtEntities);
        } catch (WebClientResponseException e) {
            log.warn("Error getting District By search Criteria from Institute API");
        } catch (Exception e) {
            log.error(String.format("Error while calling Institute api: %s", e.getMessage()));
        }
        return null;
    }

    public List<District> getDistrictsFromRedisCache() {
        log.debug("**** Getting districts from Redis Cache.");
        Iterable<DistrictEntity> districtEntities= districtRedisRepository.findAll();
        if ( (!districtEntities.iterator().hasNext())){
            log.debug("Get District  from Redis Cache returned empty");
            List<District> districts = this.getDistrictsFromInstituteApi();
            if ((districts  != null) && (!districts .isEmpty())) {
                this.loadDistrictsIntoRedisCache(districts);
                return districts;
            }
        }
        return  districtTransformer.transformToDTO(districtEntities);
    }

    public void initializeDistrictCache(boolean force) {
        serviceHelper.initializeCache(force, CacheKey.DISTRICT_CACHE, this);
    }

    public District getDistrictByDistNoFromRedisCache(String districtNumber) {
        log.debug("**** Getting district by district no. from Redis Cache.");
        DistrictEntity districtEntity = districtRedisRepository.findByDistrictNumber(districtNumber);
        if (  districtEntity  == null){
            log.debug("Getting district by district no from Redis Cache returned empty");
            List<District> districts = this.getDistrictsBySearchCriteriaFromInstituteApi("districtNumber",districtNumber );
            if ((districts != null) &&(!districts.isEmpty())){
                this.loadDistrictsIntoRedisCache(districts);
                return districts.get(0);
            }
        }
        return  districtTransformer.transformToDTO(districtEntity);
    }

    public District getDistrictByIdFromRedisCache(String districtId) {
        log.debug("**** Getting district by ID from Redis Cache.");
        Optional<DistrictEntity> districtEntity = districtRedisRepository.findById(districtId);
        if((districtEntity == null) || (districtEntity.isEmpty())) {
            log.debug("Getting district by ID from Redis Cache returned empty");
            District district = this.getDistrictByIdFromInstituteApi(districtId);
            if (district!=null) {
                this.loadDistrictsIntoRedisCache(List.of(district));
                return district;
            }

        }
        return  districtTransformer.transformToDTO(districtEntity);
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

    public District getDistrictByIdFromInstituteApi(String districtId) {
        try {
            log.debug("****Before Calling Institute API");
            Optional<DistrictEntity> districtEntity = this.restService.get(String.format(constants.getGetDistrictFromInstituteApiUrl(), districtId),
                    Optional.class, webClient);
            return districtTransformer.transformToDTO(districtEntity);
        } catch (WebClientResponseException e) {
            log.warn("Error getting District");
        } catch (Exception e) {
            log.error(String.format("Error while calling Institute api: %s", e.getMessage()));
        }
        return null;
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
