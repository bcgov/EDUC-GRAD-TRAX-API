package ca.bc.gov.educ.api.trax.service.institute;

import ca.bc.gov.educ.api.trax.constant.CacheKey;
import ca.bc.gov.educ.api.trax.constant.CacheStatus;
import ca.bc.gov.educ.api.trax.model.entity.institute.*;
import ca.bc.gov.educ.api.trax.repository.redis.*;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import redis.clients.jedis.JedisCluster;

import java.util.List;

@Slf4j
@AllArgsConstructor
@Service("cacheService")
public class CacheService {

    SchoolRedisRepository schoolRedisRepository;

    SchoolDetailRedisRepository schoolDetailRedisRepository;

    DistrictRedisRepository districtRedisRepository;

    SchoolCategoryCodeRedisRepository schoolCategoryCodeRedisRepository;

    SchoolFundingGroupCodeRedisRepository schoolFundingGroupCodeRedisRepository;

    JedisCluster jedisCluster;

    @Async("taskExecutor")
    public void loadSchoolsIntoRedisCacheAsync(List<SchoolEntity> schools) {
        if(!CollectionUtils.isEmpty(schools)) {
            loadSchoolsIntoRedisCache(schools);
        }
    }

    public void loadSchoolsIntoRedisCache(List<SchoolEntity> schools) {
        if(!isCacheLoading(CacheKey.SCHOOL_CACHE) && !CollectionUtils.isEmpty(schools)) {
            setCacheStateLoading(CacheKey.SCHOOL_CACHE);
            long start = System.currentTimeMillis();
            log.debug("****Before loading schools into cache");
            for (List<SchoolEntity> partition : Lists.partition(schools, 1000)) {
                schoolRedisRepository.saveAll(partition);
            }
            log.info("{} Schools Loaded into cache in {} ms.", schools.size(), (System.currentTimeMillis() - start));
            setCacheReadiness(CacheKey.SCHOOL_CACHE);
        }
    }

    @Async("taskExecutor")
    public void loadSchoolDetailsIntoRedisCacheAsync(List<SchoolDetailEntity> schoolDetails) {
        if(!CollectionUtils.isEmpty(schoolDetails)) {
            loadSchoolDetailsIntoRedisCache(schoolDetails);
        }
    }

    public void loadSchoolDetailsIntoRedisCache(List<SchoolDetailEntity> schoolDetails) {
        if(!isCacheLoading(CacheKey.SCHOOL_DETAIL_CACHE) && !CollectionUtils.isEmpty(schoolDetails)) {
            setCacheStateLoading(CacheKey.SCHOOL_DETAIL_CACHE);
            long start = System.currentTimeMillis();
            log.debug("****Before loading school details into cache");
            for (List<SchoolDetailEntity> partition : Lists.partition(schoolDetails, 1000)) {
                schoolDetailRedisRepository.saveAll(partition);
            }
            log.info("{} School Details Loaded into cache in {} ms.", schoolDetails.size(), (System.currentTimeMillis() - start));
            setCacheReadiness(CacheKey.SCHOOL_DETAIL_CACHE);
        }
    }

    @Async("taskExecutor")
    public void loadDistrictsIntoRedisCacheAsync(List<DistrictEntity> districts) {
        if(!CollectionUtils.isEmpty(districts)) {
            loadDistrictsIntoRedisCache(districts);
        }
    }

    public void loadDistrictsIntoRedisCache(List<DistrictEntity> districts) {
        if(!isCacheLoading(CacheKey.DISTRICT_CACHE) && !CollectionUtils.isEmpty(districts)) {
            setCacheStateLoading(CacheKey.DISTRICT_CACHE);
            long start = System.currentTimeMillis();
            log.debug("****Before loading districts into cache");
            districtRedisRepository.saveAll(districts);
            log.info("{} Districts Loaded into cache in {} ms.", districts.size(), (System.currentTimeMillis() - start));
            setCacheReadiness(CacheKey.DISTRICT_CACHE);
        }
    }

    @Async("taskExecutor")
    public void loadSchoolCategoryCodesIntoRedisCacheAsync(List<SchoolCategoryCodeEntity> schoolCategoryCodes) {
        if(!CollectionUtils.isEmpty(schoolCategoryCodes)) {
            loadSchoolCategoryCodesIntoRedisCache(schoolCategoryCodes);
        }
    }

    public void loadSchoolCategoryCodesIntoRedisCache(List<SchoolCategoryCodeEntity> schoolCategoryCodes) {
        if(!isCacheLoading(CacheKey.SCHOOL_CATEGORY_CODE_CACHE) && !CollectionUtils.isEmpty(schoolCategoryCodes)) {
            long start = System.currentTimeMillis();
            log.debug("****Before loading School Category Codes into cache");
            setCacheStateLoading(CacheKey.SCHOOL_CATEGORY_CODE_CACHE);
            schoolCategoryCodeRedisRepository.saveAll(schoolCategoryCodes);
            log.info("{} School Category Codes  Loaded into cache in {} ms.", schoolCategoryCodes.size(), (System.currentTimeMillis() - start));
            setCacheReadiness(CacheKey.SCHOOL_CATEGORY_CODE_CACHE);
        }
    }

    @Async("taskExecutor")
    public void loadSchoolFundingGroupCodesIntoRedisCacheAsync(List<SchoolFundingGroupCodeEntity> schoolFundingGroupCodes) {
        if(!CollectionUtils.isEmpty(schoolFundingGroupCodes)) {
            loadSchoolFundingGroupCodesIntoRedisCache(schoolFundingGroupCodes);
        }
    }

    public void loadSchoolFundingGroupCodesIntoRedisCache(List<SchoolFundingGroupCodeEntity> schoolFundingGroupCodes) {
        if(!isCacheLoading(CacheKey.SCHOOL_FUNDING_GROUP_CODE_CACHE) && !CollectionUtils.isEmpty(schoolFundingGroupCodes)) {
            long start = System.currentTimeMillis();
            log.debug("****Before loading School Funding Group Codes into cache");
            setCacheStateLoading(CacheKey.SCHOOL_FUNDING_GROUP_CODE_CACHE);
            schoolFundingGroupCodeRedisRepository.saveAll(schoolFundingGroupCodes);
            log.info("{} School Funding Group Codes Loaded into cache in {} ms.", schoolFundingGroupCodes.size(), (System.currentTimeMillis() - start));
            setCacheReadiness(CacheKey.SCHOOL_FUNDING_GROUP_CODE_CACHE);
        }
    }

    private boolean isCacheLoading(CacheKey cacheKey) {
        String cacheStatus = jedisCluster.get(cacheKey.name());
        return CacheStatus.LOADING.name().equals(cacheStatus);
    }

    private void setCacheStateLoading(CacheKey cacheKey) {
        jedisCluster.set(cacheKey.name(), CacheStatus.LOADING.name());
    }

    private void setCacheReadiness(CacheKey cacheKey) {
        jedisCluster.set(cacheKey.name(), CacheStatus.READY.name());
        log.info(String.format("Success! - %s is now READY", cacheKey));
    }

}
