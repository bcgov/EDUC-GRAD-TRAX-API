package ca.bc.gov.educ.api.trax.service.institute;

import ca.bc.gov.educ.api.trax.config.RedisConfig;
import ca.bc.gov.educ.api.trax.constant.CacheKey;
import ca.bc.gov.educ.api.trax.constant.CacheStatus;
import ca.bc.gov.educ.api.trax.model.dto.institute.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;


import java.util.List;

@Slf4j
@Component
@AllArgsConstructor
public class ServiceHelper<T> {

    RedisConfig redisConfig;

    public void initializeCache(boolean force, CacheKey cacheKey, T service) {
        String cacheStatus = redisConfig.getStringRedisTemplate().opsForValue().get(cacheKey.name());
        cacheStatus = cacheStatus == null ? "" : cacheStatus;
        if (CacheStatus.LOADING.name().compareToIgnoreCase(cacheStatus) == 0
                || CacheStatus.READY.name().compareToIgnoreCase(cacheStatus) == 0) {
            log.info(String.format("%s status: %s", cacheKey, cacheStatus));
            if (force) {
                log.info(String.format("Force Flag is true. Reloading %s...", cacheKey.name()));
                loadCache(cacheKey, service);
            } else {
                log.info(String.format("Force Flag is false. Skipping %s reload", cacheKey.name()));
            }
        } else {
            log.info(String.format("Loading %s...", cacheKey));
            loadCache(cacheKey, service);
        }
    }

    private void loadCache(CacheKey cacheKey, T service) {
        loadDataIntoRedisCache(cacheKey, service);
    }

    private void loadDataIntoRedisCache(CacheKey cacheKey, T service) {
        try {
            switch (cacheKey) {
                case SCHOOL_CATEGORY_CODE_CACHE -> {
                    List<SchoolCategoryCode> schoolCategoryCodes = ((CodeService)service).getSchoolCategoryCodesFromInstituteApi();
                    if(!CollectionUtils.isEmpty(schoolCategoryCodes)) {
                        ((CodeService)service).loadSchoolCategoryCodesIntoRedisCache(schoolCategoryCodes);
                    }
                }
                case SCHOOL_FUNDING_GROUP_CODE_CACHE -> {
                    List<SchoolFundingGroupCode> schoolFundingGroupCodes = ((CodeService)service).getSchoolFundingGroupCodesFromInstituteApi();
                    if(!CollectionUtils.isEmpty(schoolFundingGroupCodes)) {
                        ((CodeService)service).loadSchoolFundingGroupCodesIntoRedisCache(schoolFundingGroupCodes);
                    }
                }
                case DISTRICT_CACHE -> {
                    List<District> districts = ((DistrictService)service).getDistrictsFromInstituteApi();
                    if(!CollectionUtils.isEmpty(districts)) {
                        ((DistrictService) service).loadDistrictsIntoRedisCache(districts);
                    }
                }
                case SCHOOL_CACHE -> {
                    List<School> schools = ((SchoolService)service).getSchoolsFromInstituteApi();
                    if(!CollectionUtils.isEmpty(schools)) {
                        ((SchoolService) service).loadSchoolsIntoRedisCache(schools);
                    }
                    List<SchoolDetail> schoolDetails = ((SchoolService)service).getSchoolDetailsFromInstituteApi();
                    if(!CollectionUtils.isEmpty(schoolDetails)) {
                        ((SchoolService) service).loadSchoolDetailsIntoRedisCache(schoolDetails);
                    }
                }
                default -> {
                    log.info(String.format("Invalid Cache Key %s", cacheKey));
                }
            }
        } catch (Exception e) {
            log.info(String.format("Exception thrown while loading cache %s. \n%s", cacheKey, e));
        }
    }
}
