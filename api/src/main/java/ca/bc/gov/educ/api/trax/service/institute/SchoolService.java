package ca.bc.gov.educ.api.trax.service.institute;

import ca.bc.gov.educ.api.trax.constant.CacheKey;
import ca.bc.gov.educ.api.trax.constant.CacheStatus;
import ca.bc.gov.educ.api.trax.model.dto.institute.School;
import ca.bc.gov.educ.api.trax.model.dto.institute.SchoolDetail;
import ca.bc.gov.educ.api.trax.model.entity.institute.SchoolEntity;
import ca.bc.gov.educ.api.trax.model.transformer.institute.SchoolDetailTransformer;
import ca.bc.gov.educ.api.trax.model.transformer.institute.SchoolTransformer;
import ca.bc.gov.educ.api.trax.repository.redis.SchoolDetailRedisRepository;
import ca.bc.gov.educ.api.trax.repository.redis.SchoolRedisRepository;
import ca.bc.gov.educ.api.trax.util.EducGradTraxApiConstants;
import ca.bc.gov.educ.api.trax.util.RestUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service("InstituteSchoolService")
public class SchoolService {

	@Autowired
	private EducGradTraxApiConstants constants;
	@Autowired
	private WebClient webClient;
	@Autowired
	SchoolRedisRepository schoolRedisRepository;
	@Autowired
	SchoolDetailRedisRepository schoolDetailRedisRepository;
	@Autowired
	SchoolTransformer schoolTransformer;
	@Autowired
	SchoolDetailTransformer schoolDetailTransformer;
	@Autowired
	private RestUtils restUtils;
	@Autowired
	RedisTemplate<String, String> redisTemplate;

	public List<School> getSchoolsFromInstituteApi() {
		try {
			log.debug("****Before Calling Institute API");
			List<SchoolEntity> schools;
			schools = webClient.get()
					.uri(constants.getAllSchoolsFromInstituteApiUrl())
					.headers(h -> {
						h.setBearerAuth(restUtils.getTokenResponseObject(
								constants.getInstituteClientId(),
								constants.getInstituteClientSecret()
						).getAccess_token());
					})
					.retrieve()
					.bodyToMono(new ParameterizedTypeReference<List<SchoolEntity>>(){}).block();
			//assert schools != null;
			//log.debug("# of Schools: " + schools.size());
			return  schoolTransformer.transformToDTO(schools);
		} catch (WebClientResponseException e) {
			log.warn(String.format("Error getting Common School List: %s", e.getMessage()));
		} catch (Exception e) {
			log.error(String.format("Error getting data from Institute api: %s", e.getMessage()));
		}
		return null;
	}

	public void loadSchoolsIntoRedisCache(List<ca.bc.gov.educ.api.trax.model.dto.institute.School> schools) {
		schoolRedisRepository
				.saveAll(schoolTransformer.transformToEntity(schools));
	}

	public List<School> getSchoolsFromRedisCache() {
		log.debug("**** Getting schools from Redis Cache.");
		return  schoolTransformer.transformToDTO(schoolRedisRepository.findAll());
	}

	public void initializeSchoolCache(boolean force) {
		String cacheStatus = redisTemplate.opsForValue().get(CacheKey.SCHOOL_CACHE.name());
		cacheStatus = cacheStatus == null ? "" : cacheStatus;
		if (CacheStatus.LOADING.name().compareToIgnoreCase(cacheStatus) == 0
				|| CacheStatus.READY.name().compareToIgnoreCase(cacheStatus) == 0) {
			log.info(String.format("SCHOOL_CACHE status: %s", cacheStatus));
			if (force) {
				log.info("Force Flag is true. Reloading SCHOOL_CACHE...");
				redisTemplate.opsForValue().set(CacheKey.SCHOOL_CACHE.name(), CacheStatus.LOADING.name());
				loadSchoolsIntoRedisCache(getSchoolsFromInstituteApi());
				redisTemplate.opsForValue().set(CacheKey.SCHOOL_CACHE.name(), CacheStatus.READY.name());
				log.info("SUCCESS! - SCHOOL_CACHE is now READY");
			} else {
				log.info("Force Flag is false. Skipping SCHOOL_CACHE reload");
			}
		} else {
			log.info("Loading SCHOOL_CACHE...");
			redisTemplate.opsForValue().set(CacheKey.SCHOOL_CACHE.name(), CacheStatus.LOADING.name());
			loadSchoolsIntoRedisCache(getSchoolsFromInstituteApi());
			redisTemplate.opsForValue().set(CacheKey.SCHOOL_CACHE.name(), CacheStatus.READY.name());
			log.info("SUCCESS! - SCHOOL_CACHE is now READY");
		}
	}

	public SchoolDetail getSchoolDetailByIdFromInstituteApi(String schoolId) {
		return getSchoolDetailByIdFromInstituteApi(schoolId, "");
	}

    public SchoolDetail getSchoolDetailByIdFromInstituteApi(String schoolId, String accessToken) {
        try {
            return webClient.get().uri(
                            String.format(constants.getSchoolDetailsByIdFromInstituteApiUrl(), schoolId))
                    .headers(h -> {
						if (accessToken.isEmpty() || accessToken.isBlank()) {
							h.setBearerAuth(restUtils.getTokenResponseObject(
									constants.getInstituteClientId(),
									constants.getInstituteClientSecret()
							).getAccess_token());
						} else {
							h.setBearerAuth(accessToken);
						}
                    })
                    .retrieve().bodyToMono(SchoolDetail.class).block();
        } catch (WebClientResponseException e) {
            log.warn("Error getting School Details");
        } catch (Exception e) {
            log.error(String.format("Error while calling Institute api: %s", e.getMessage()));
        }
        return null;
    }

    public List<SchoolDetail> getSchoolDetailsFromInstituteApi() {

		String accessToken = "";
        List<School> schools = getSchoolsFromRedisCache();
        List<SchoolDetail> schoolDetails = new ArrayList<SchoolDetail>();

        int counter = 1;

        for (School s : schools) {
            SchoolDetail sd = new SchoolDetail();

            if (counter%200 == 0)
                accessToken = restUtils.getTokenResponseObject(
						constants.getInstituteClientId(),
						constants.getInstituteClientSecret()
				).getAccess_token();
            sd = getSchoolDetailByIdFromInstituteApi(s.getSchoolId(), accessToken);
            schoolDetails.add(sd);
            counter++;
        }
        return schoolDetails;
    }

	public void loadSchoolDetailsIntoRedisCache(List<SchoolDetail> schoolDetails) {
		schoolDetailRedisRepository
				.saveAll(schoolDetailTransformer.transformToEntity(schoolDetails));
	}

	public List<SchoolDetail> getSchoolDetailsFromRedisCache() {
		log.debug("**** Getting school Details from Redis Cache.");
		return schoolDetailTransformer.transformToDTO(schoolDetailRedisRepository.findAll());
	}

	public void initializeSchoolDetailCache(boolean force) {
		String cacheStatus = redisTemplate.opsForValue().get(CacheKey.SCHOOL_DETAIL_CACHE.name());
		cacheStatus = cacheStatus == null ? "" : cacheStatus;
		if (CacheStatus.LOADING.name().compareToIgnoreCase(cacheStatus) == 0
				|| CacheStatus.READY.name().compareToIgnoreCase(cacheStatus) == 0) {
			log.info(String.format("SCHOOL_DETAIL_CACHE status: %s", cacheStatus));
			if (force) {
				log.info("Force Flag is true. Reloading SCHOOL_DETAIL_CACHE...");
				redisTemplate.opsForValue().set(CacheKey.SCHOOL_DETAIL_CACHE.name(), CacheStatus.LOADING.name());
				loadSchoolDetailsIntoRedisCache(getSchoolDetailsFromInstituteApi());
				redisTemplate.opsForValue().set(CacheKey.SCHOOL_DETAIL_CACHE.name(), CacheStatus.READY.name());
				log.info("SUCCESS! - SCHOOL_DETAIL_CACHE is now READY");
			} else {
				log.info("Force Flag is false. Skipping SCHOOL_DETAIL_CACHE reload");
			}
		} else {
			log.info("Loading SCHOOL_DETAIL_CACHE...");
			redisTemplate.opsForValue().set(CacheKey.SCHOOL_DETAIL_CACHE.name(), CacheStatus.LOADING.name());
			loadSchoolDetailsIntoRedisCache(getSchoolDetailsFromInstituteApi());
			redisTemplate.opsForValue().set(CacheKey.SCHOOL_DETAIL_CACHE.name(), CacheStatus.READY.name());
			log.info("SUCCESS! - SCHOOL_DETAIL_CACHE is now READY");
		}
	}

}
