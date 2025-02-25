package ca.bc.gov.educ.api.trax.service.institute;

import ca.bc.gov.educ.api.trax.model.entity.institute.*;
import ca.bc.gov.educ.api.trax.repository.redis.*;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service("cacheService")
public class CacheService {

    @Autowired
    SchoolRedisRepository schoolRedisRepository;

    @Autowired
    SchoolDetailRedisRepository schoolDetailRedisRepository;

    @Autowired
    DistrictRedisRepository districtRedisRepository;

    @Autowired
    SchoolCategoryCodeRedisRepository schoolCategoryCodeRedisRepository;

    @Autowired
    SchoolFundingGroupCodeRedisRepository schoolFundingGroupCodeRedisRepository;

    @Async("taskExecutor")
    public void loadSchoolsIntoRedisCacheAsync(List<SchoolEntity> schools) {
        if(!CollectionUtils.isEmpty(schools)) {
            loadSchoolsIntoRedisCache(schools);
        }
    }

    public void loadSchoolsIntoRedisCache(List<SchoolEntity> schools) {
        if(!CollectionUtils.isEmpty(schools)) {
            long start = System.currentTimeMillis();
            log.debug("****Before loading schools into cache");
            for (List<SchoolEntity> partition : Lists.partition(schools, 1000)) {
                schoolRedisRepository.saveAll(partition);
            }
            log.info("{} Schools Loaded into cache in {} ms.", schools.size(), (System.currentTimeMillis() - start));
        }
    }

    @Async("taskExecutor")
    public void loadSchoolDetailsIntoRedisCacheAsync(List<SchoolDetailEntity> schoolDetails) {
        if(!CollectionUtils.isEmpty(schoolDetails)) {
            loadSchoolDetailsIntoRedisCache(schoolDetails);
        }
    }

    public void loadSchoolDetailsIntoRedisCache(List<SchoolDetailEntity> schoolDetails) {
        if(!CollectionUtils.isEmpty(schoolDetails)) {
            long start = System.currentTimeMillis();
            log.debug("****Before loading school details into cache");
            for (List<SchoolDetailEntity> partition : Lists.partition(schoolDetails, 1000)) {
                schoolDetailRedisRepository.saveAll(partition);
            }
            log.info("{} School Details Loaded into cache in {} ms.", schoolDetails.size(), (System.currentTimeMillis() - start));
        }
    }

    @Async("taskExecutor")
    public void loadDistrictsIntoRedisCacheAsync(List<DistrictEntity> districts) {
        if(!CollectionUtils.isEmpty(districts)) {
            loadDistrictsIntoRedisCache(districts);
        }
    }

    public void loadDistrictsIntoRedisCache(List<DistrictEntity> districts) {
        if(!CollectionUtils.isEmpty(districts)) {
            long start = System.currentTimeMillis();
            log.debug("****Before loading districts into cache");
            districtRedisRepository.saveAll(districts);
            log.info("{} Districts Loaded into cache in {} ms.", districts.size(), (System.currentTimeMillis() - start));
        }
    }

    @Async("taskExecutor")
    public void loadSchoolCategoryCodesIntoRedisCacheAsync(List<SchoolCategoryCodeEntity> schoolCategoryCodes) {
        if(!CollectionUtils.isEmpty(schoolCategoryCodes)) {
            loadSchoolCategoryCodesIntoRedisCache(schoolCategoryCodes);
        }
    }

    public void loadSchoolCategoryCodesIntoRedisCache(List<SchoolCategoryCodeEntity> schoolCategoryCodes) {
        if(!CollectionUtils.isEmpty(schoolCategoryCodes)) {
            long start = System.currentTimeMillis();
            log.debug("****Before loading School Category Codes into cache");
            schoolCategoryCodeRedisRepository.saveAll(schoolCategoryCodes);
            log.info("{} School Category Codes  Loaded into cache in {} ms.", schoolCategoryCodes.size(), (System.currentTimeMillis() - start));
        }
    }

    @Async("taskExecutor")
    public void loadSchoolFundingGroupCodesIntoRedisCacheAsync(List<SchoolFundingGroupCodeEntity> schoolFundingGroupCodes) {
        loadSchoolFundingGroupCodesIntoRedisCache(schoolFundingGroupCodes);
    }

    public void loadSchoolFundingGroupCodesIntoRedisCache(List<SchoolFundingGroupCodeEntity> schoolFundingGroupCodes) {
        if(!CollectionUtils.isEmpty(schoolFundingGroupCodes)) {
            long start = System.currentTimeMillis();
            log.debug("****Before loading School Funding Group Codes into cache");
            schoolFundingGroupCodeRedisRepository.saveAll(schoolFundingGroupCodes);
            log.info("{} School Funding Group Codes Loaded into cache in {} ms.", schoolFundingGroupCodes.size(), (System.currentTimeMillis() - start));
        }
    }

}
