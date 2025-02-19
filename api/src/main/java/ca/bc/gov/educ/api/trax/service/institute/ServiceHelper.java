package ca.bc.gov.educ.api.trax.service.institute;

import ca.bc.gov.educ.api.trax.constant.CacheKey;
import ca.bc.gov.educ.api.trax.constant.CacheStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.JedisCluster;

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
                    ((CodeService)service).loadSchoolCategoryCodesIntoRedisCache(
                            ((CodeService)service).getSchoolCategoryCodesFromInstituteApi()
                    );
                    break;
                }
                case SCHOOL_FUNDING_GROUP_CODE_CACHE -> {
                    ((CodeService)service).loadSchoolFundingGroupCodesIntoRedisCache(
                            ((CodeService)service).getSchoolFundingGroupCodesFromInstituteApi()
                    );
                    break;
                }
                case DISTRICT_CACHE -> {
                    ((DistrictService)service).loadDistrictsIntoRedisCache(
                            ((DistrictService)service).getDistrictsFromInstituteApi()
                    );
                    break;
                }
                case SCHOOL_CACHE -> {
                    ((SchoolService)service).loadSchoolsIntoRedisCache(
                            ((SchoolService)service).getSchoolsFromInstituteApi()
                    );
                    ((SchoolService)service).loadSchoolDetailsIntoRedisCache(
                            ((SchoolService)service).getSchoolDetailsFromInstituteApi()
                    );
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
