package ca.bc.gov.educ.api.trax.service.institute;

import ca.bc.gov.educ.api.trax.constant.CacheKey;
import ca.bc.gov.educ.api.trax.constant.CacheStatus;
import ca.bc.gov.educ.api.trax.model.dto.institute.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import redis.clients.jedis.JedisCluster;

import java.util.List;

@Slf4j
@Component
public class ServiceHelper<T> {

    @Autowired
    JedisCluster jedisCluster;

    public void initializeCache(boolean force, CacheKey cacheKey, T service) {
        String cacheStatus = jedisCluster.get(cacheKey.name());
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
        jedisCluster.set(cacheKey.name(), CacheStatus.LOADING.name());
        loadDataIntoRedisCache(cacheKey, service);
        jedisCluster.set(cacheKey.name(), CacheStatus.READY.name());
        log.info(String.format("Success! - %s is now READY", cacheKey));
    }

    private void loadDataIntoRedisCache(CacheKey cacheKey, T service) {
        try {
            switch (cacheKey) {
                case SCHOOL_CATEGORY_CODE_CACHE -> {
                    List<SchoolCategoryCode> schoolCategoryCodes = ((CodeService)service).getSchoolCategoryCodesFromInstituteApi();
                    if(!CollectionUtils.isEmpty(schoolCategoryCodes)) {
                        ((CodeService)service).loadSchoolCategoryCodesIntoRedisCache(schoolCategoryCodes);
                    }
                    break;
                }
                case SCHOOL_FUNDING_GROUP_CODE_CACHE -> {
                    List<SchoolFundingGroupCode> schoolFundingGroupCodes = ((CodeService)service).getSchoolFundingGroupCodesFromInstituteApi();
                    if(!CollectionUtils.isEmpty(schoolFundingGroupCodes)) {
                        ((CodeService)service).loadSchoolFundingGroupCodesIntoRedisCache(schoolFundingGroupCodes);
                    }
                    break;
                }
                case DISTRICT_CACHE -> {
                    List<District> districts = ((DistrictService)service).getDistrictsFromInstituteApi();
                    if(!CollectionUtils.isEmpty(districts)) {
                        ((DistrictService) service).loadDistrictsIntoRedisCache(districts);
                    }
                    break;
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
                    break;
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
