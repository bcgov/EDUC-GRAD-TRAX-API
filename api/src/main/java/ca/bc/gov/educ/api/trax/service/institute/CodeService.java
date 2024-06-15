package ca.bc.gov.educ.api.trax.service.institute;

import ca.bc.gov.educ.api.trax.model.dto.institute.District;
import ca.bc.gov.educ.api.trax.model.dto.institute.SchoolCategoryCode;
import ca.bc.gov.educ.api.trax.model.dto.institute.SchoolFundingGroupCode;
import ca.bc.gov.educ.api.trax.model.entity.institute.SchoolCategoryCodeEntity;
import ca.bc.gov.educ.api.trax.model.entity.institute.SchoolFundingGroupCodeEntity;
import ca.bc.gov.educ.api.trax.model.transformer.institute.SchoolCategoryCodeTransformer;
import ca.bc.gov.educ.api.trax.model.transformer.institute.SchoolFundingGroupCodeTransformer;
import ca.bc.gov.educ.api.trax.repository.redis.SchoolCategoryCodeRedisRepository;
import ca.bc.gov.educ.api.trax.repository.redis.SchoolFundingGroupCodeRedisRepository;
import ca.bc.gov.educ.api.trax.util.EducGradTraxApiConstants;
import ca.bc.gov.educ.api.trax.util.RestUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service("InstituteCodeService")
public class CodeService {

	@Autowired
	private EducGradTraxApiConstants constants;
	@Autowired
	private WebClient webClient;
	@Autowired
	SchoolCategoryCodeRedisRepository schoolCategoryCodeRedisRepository;
	@Autowired
	SchoolFundingGroupCodeRedisRepository schoolFundingGroupCodeRedisRepository;
	@Autowired
	SchoolCategoryCodeTransformer schoolCategoryCodeTransformer;
	@Autowired
	SchoolFundingGroupCodeTransformer schoolFundingGroupCodeTransformer;
	@Autowired
	private RestUtils restUtils;
	@Autowired
	RedisTemplate<String, String> redisTemplate;

	public List<SchoolCategoryCode> getSchoolCategoryCodesFromInstituteApi() {
		try {
			log.debug("****Before Calling Institute API");
			List<SchoolCategoryCodeEntity> schoolCategoryCodes =
					webClient.get()
							.uri(constants.getAllSchoolCategoryCodesFromInstituteApiUrl())
							.headers(h -> {
								h.setBearerAuth(restUtils.getTokenResponseObject(
										constants.getInstituteClientId(),
										constants.getInstituteClientSecret()
								).getAccess_token());
					})
					.retrieve()
							.bodyToMono(new ParameterizedTypeReference<List<SchoolCategoryCodeEntity>>() {
					}).block();
			assert schoolCategoryCodes != null;
            log.debug("# of School Category Codes: " + schoolCategoryCodes.size());
			return schoolCategoryCodeTransformer.transformToDTO(schoolCategoryCodes);
		} catch (WebClientResponseException e) {
			log.warn(String.format("Error getting School Category Codes: %s", e.getMessage()));
		} catch (Exception e) {
			log.error(String.format("Error while calling school-api: %s", e.getMessage()));
		}
		return null;
	}

	public void loadSchoolCategoryCodesIntoRedisCache(List<SchoolCategoryCode> schoolCategoryCodes) {
		schoolCategoryCodeRedisRepository
				.saveAll(schoolCategoryCodeTransformer.transformToEntity(schoolCategoryCodes));
	}

	public List<SchoolCategoryCode> getSchoolCategoryCodesFromRedisCache() {
		log.debug("**** Getting school category codes from Redis Cache.");
		return  schoolCategoryCodeTransformer.transformToDTO(schoolCategoryCodeRedisRepository.findAll());
	}

	public void initializeSchoolCategoryCodeCache(boolean force) {
		if ("READY".compareToIgnoreCase(Objects.requireNonNull(redisTemplate.opsForValue().get("SCHOOL_CATEGORY_CODE_CACHE"))) == 0) {
			log.info("SCHOOL_CATEGORY_CODE_CACHE status: READY");
			if (force) {
				log.info("Force Flag is true. Reloading SCHOOL_CATEGORY_CODE_CACHE...");
				loadSchoolCategoryCodesIntoRedisCache(getSchoolCategoryCodesFromInstituteApi());
				log.info("SUCCESS! - SCHOOL_CATEGORY_CODE_CACHE is now READY");
			} else {
				log.info("Force Flag is false. Skipping SCHOOL_CATEGORY_CODE_CACHE reload");
			}
		} else {
			log.info("Loading SCHOOL_CATEGORY_CODE_CACHE...");
			loadSchoolCategoryCodesIntoRedisCache(getSchoolCategoryCodesFromInstituteApi());
			redisTemplate.opsForValue().set("SCHOOL_CATEGORY_CODE_CACHE", "READY");
			log.info("SUCCESS! - SCHOOL_CATEGORY_CODE_CACHE is now READY");
		}
	}

	public List<SchoolFundingGroupCode> getSchoolFundingGroupCodesFromInstituteApi() {
		try {
			log.debug("****Before Calling Institute API");
			List<SchoolFundingGroupCodeEntity> schoolFundingGroupCodes;
			schoolFundingGroupCodes = webClient.get()
					.uri(constants.getAllSchoolFundingGroupCodesFromInstituteApiUrl())
					.headers(h -> {
						h.setBearerAuth(restUtils.getTokenResponseObject(
								constants.getInstituteClientId(),
								constants.getInstituteClientSecret()
						).getAccess_token());
					})
					.retrieve()
					.bodyToMono(new ParameterizedTypeReference<List<SchoolFundingGroupCodeEntity>>() {
					}).block();
			//assert schoolFundingGroupCodes != null;
			//log.debug("# of School Funding Group Codes: " + schoolFundingGroupCodes.size());
			return schoolFundingGroupCodeTransformer.transformToDTO(schoolFundingGroupCodes);
		} catch (WebClientResponseException e) {
			log.warn(String.format("Error getting School Funding Group Codes: %s", e.getMessage()));
		} catch (Exception e) {
			log.error(String.format("Error while calling school-api: %s", e.getMessage()));
		}
		return null;
	}

	public void loadSchoolFundingGroupCodesIntoRedisCache(List<SchoolFundingGroupCode> schoolFundingGroupCodes) {
		schoolFundingGroupCodeRedisRepository
				.saveAll(schoolFundingGroupCodeTransformer.transformToEntity(schoolFundingGroupCodes));
	}

	public List<SchoolFundingGroupCode> getSchoolFundingGroupCodesFromRedisCache() {
		log.debug("**** Getting school funding group codes from Redis Cache.");
		return  schoolFundingGroupCodeTransformer.transformToDTO(schoolFundingGroupCodeRedisRepository.findAll());
	}

	public void initializeSchoolFundingGroupCodeCache(boolean force) {
		if ("READY".compareToIgnoreCase(Objects.requireNonNull(redisTemplate.opsForValue().get("SCHOOL_FUNDING_GROUP_CODE_CACHE"))) == 0) {
			log.info("SCHOOL_FUNDING_GROUP_CODE_CACHE status: READY");
			if (force) {
				log.info("Force Flag is true. Reloading SCHOOL_FUNDING_GROUP_CODE_CACHE...");
				loadSchoolFundingGroupCodesIntoRedisCache(getSchoolFundingGroupCodesFromInstituteApi());
				log.info("SUCCESS! - SCHOOL_FUNDING_GROUP_CODE_CACHE is now READY");
			} else {
				log.info("Force Flag is false. Skipping SCHOOL_FUNDING_GROUP_CODE_CACHE reload");
			}
		} else {
			log.info("Loading SCHOOL_FUNDING_GROUP_CODE_CACHE...");
			loadSchoolFundingGroupCodesIntoRedisCache(getSchoolFundingGroupCodesFromInstituteApi());
			redisTemplate.opsForValue().set("SCHOOL_FUNDING_GROUP_CODE_CACHE", "READY");
			log.info("SUCCESS! - SCHOOL_FUNDING_GROUP_CODE_CACHE is now READY");
		}
	}
}
