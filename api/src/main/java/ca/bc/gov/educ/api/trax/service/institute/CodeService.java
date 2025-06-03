package ca.bc.gov.educ.api.trax.service.institute;

import ca.bc.gov.educ.api.trax.constant.CacheKey;
import ca.bc.gov.educ.api.trax.exception.ServiceException;
import ca.bc.gov.educ.api.trax.model.dto.institute.SchoolCategoryCode;
import ca.bc.gov.educ.api.trax.model.dto.institute.SchoolFundingGroupCode;
import ca.bc.gov.educ.api.trax.model.entity.institute.SchoolCategoryCodeEntity;
import ca.bc.gov.educ.api.trax.model.entity.institute.SchoolFundingGroupCodeEntity;
import ca.bc.gov.educ.api.trax.model.transformer.institute.SchoolCategoryCodeTransformer;
import ca.bc.gov.educ.api.trax.model.transformer.institute.SchoolFundingGroupCodeTransformer;
import ca.bc.gov.educ.api.trax.repository.redis.SchoolCategoryCodeRedisRepository;
import ca.bc.gov.educ.api.trax.repository.redis.SchoolFundingGroupCodeRedisRepository;
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

import java.util.Collections;
import java.util.List;

@Slf4j
@Service("instituteCodeService")
public class CodeService {

	private final EducGradTraxApiConstants constants;
	private final WebClient webClient;
	SchoolCategoryCodeRedisRepository schoolCategoryCodeRedisRepository;
	SchoolFundingGroupCodeRedisRepository schoolFundingGroupCodeRedisRepository;
	SchoolCategoryCodeTransformer schoolCategoryCodeTransformer;
	SchoolFundingGroupCodeTransformer schoolFundingGroupCodeTransformer;
	ServiceHelper<CodeService> serviceHelper;
	RESTService restService;
	CacheService cacheService;

	@Autowired
	public CodeService(EducGradTraxApiConstants constants, @Qualifier("gradInstituteApiClient") WebClient webClient,
					   SchoolCategoryCodeRedisRepository schoolCategoryCodeRedisRepository,
					   SchoolFundingGroupCodeRedisRepository schoolFundingGroupCodeRedisRepository,
					   SchoolCategoryCodeTransformer schoolCategoryCodeTransformer,
					   SchoolFundingGroupCodeTransformer schoolFundingGroupCodeTransformer,
					   ServiceHelper<CodeService> serviceHelper,
					   RESTService restService,
					   CacheService cacheService) {
		this.constants = constants;
		this.webClient = webClient;
		this.schoolCategoryCodeRedisRepository = schoolCategoryCodeRedisRepository;
		this.schoolFundingGroupCodeRedisRepository = schoolFundingGroupCodeRedisRepository;
		this.schoolCategoryCodeTransformer = schoolCategoryCodeTransformer;
		this.schoolFundingGroupCodeTransformer = schoolFundingGroupCodeTransformer;
		this.serviceHelper = serviceHelper;
		this.restService = restService;
		this.cacheService = cacheService;
	}

	public List<SchoolCategoryCode> getSchoolCategoryCodesFromInstituteApi() {
		try {
			log.debug("****Before Calling Institute API for SchoolCategoryCode");
			List<SchoolCategoryCodeEntity> response = this.restService.get(constants.getAllSchoolCategoryCodesFromInstituteApiUrl(),
					List.class, webClient);
			return schoolCategoryCodeTransformer.transformToDTO(response);
		} catch (WebClientResponseException e) {
			log.warn(String.format("Error getting School Category Codes: %s", e.getMessage()));
		} catch (Exception e) {
			log.error(String.format("Error while calling school-api: %s", e.getMessage()));
		}
		return Collections.emptyList();
	}

	public List<SchoolCategoryCode> loadSchoolCategoryCodesFromInstituteApiIntoRedisCacheAsync() {
		List<SchoolCategoryCode> schoolCategoryCodes = getSchoolCategoryCodesFromInstituteApi();
		if(!CollectionUtils.isEmpty(schoolCategoryCodes)) {
			cacheService.loadSchoolCategoryCodesIntoRedisCacheAsync(schoolCategoryCodeTransformer.transformToEntity(schoolCategoryCodes));
		}
		return schoolCategoryCodes;
	}

	public void loadSchoolCategoryCodesIntoRedisCache(List<SchoolCategoryCode> schoolCategoryCodes) {
		if(!CollectionUtils.isEmpty(schoolCategoryCodes)) {
			cacheService.loadSchoolCategoryCodesIntoRedisCache(schoolCategoryCodeTransformer.transformToEntity(schoolCategoryCodes));
		}
	}

	public SchoolCategoryCode getSchoolCategoryCodeFromRedisCache(String schoolCategoryCode) {
		if(StringUtils.isBlank(schoolCategoryCode)) { log.info("getSchoolCategoryCodeFromRedisCache: schoolCategoryCode is null.");  return null;}
		log.debug("**** Getting school category codes from Redis Cache for : {}.", schoolCategoryCode);
		return schoolCategoryCodeRedisRepository.findById(schoolCategoryCode)
				.map(schoolCategoryCodeTransformer::transformToDTO)
				.orElseGet(() -> {
					SchoolCategoryCode schoolCategory = getSchoolCategoryCodesFromInstituteApi().stream()
							.filter(schoolCategoryCode1 -> schoolCategoryCode1.getSchoolCategoryCode().equals(schoolCategoryCode))
							.findFirst()
							.orElse(null);
					if(schoolCategory != null) {
						updateSchoolCategoryCode(schoolCategory);
					}
					return schoolCategory;

				});
	}

	public List<SchoolCategoryCode> getSchoolCategoryCodesFromRedisCache() {
		log.debug("**** Getting school category codes from Redis Cache.");
		List<SchoolCategoryCode> schoolCategoryCodes =  schoolCategoryCodeTransformer.transformToDTO(schoolCategoryCodeRedisRepository.findAll());
		return CollectionUtils.isEmpty(schoolCategoryCodes) ? loadSchoolCategoryCodesFromInstituteApiIntoRedisCacheAsync() : schoolCategoryCodes;
	}

	public void initializeSchoolCategoryCodeCache(boolean force) {
		serviceHelper.initializeCache(force, CacheKey.SCHOOL_CATEGORY_CODE_CACHE, this);
	}

	public List<SchoolFundingGroupCode> getSchoolFundingGroupCodesFromInstituteApi() {
		try {
			log.debug("****Before Calling Institute API for SchoolFundingGroupCode");
			List<SchoolFundingGroupCodeEntity> response = this.restService.get(constants.getAllSchoolFundingGroupCodesFromInstituteApiUrl(),
					List.class, webClient);
			return schoolFundingGroupCodeTransformer.transformToDTO(response);
		} catch (WebClientResponseException e) {
			log.warn(String.format("Error getting School Funding Group Codes: %s", e.getMessage()));
		} catch (Exception e) {
			log.error(String.format("Error while calling school-api: %s", e.getMessage()));
		}
		return Collections.emptyList();
	}

	public List<SchoolFundingGroupCode> loadSchoolFundingGroupCodesFromInstituteApiIntoRedisCacheAsync() {
		List<SchoolFundingGroupCode> schoolFundingGroupCodes = getSchoolFundingGroupCodesFromInstituteApi();
		if(!CollectionUtils.isEmpty(schoolFundingGroupCodes)) {
			cacheService.loadSchoolFundingGroupCodesIntoRedisCacheAsync(schoolFundingGroupCodeTransformer.transformToEntity(schoolFundingGroupCodes));
		}
		return schoolFundingGroupCodes;
	}

	public void loadSchoolFundingGroupCodesIntoRedisCache(List<SchoolFundingGroupCode> schoolFundingGroupCodes) {
		if(!CollectionUtils.isEmpty(schoolFundingGroupCodes)) {
			cacheService.loadSchoolFundingGroupCodesIntoRedisCache(schoolFundingGroupCodeTransformer.transformToEntity(schoolFundingGroupCodes));
		}
	}

	public List<SchoolFundingGroupCode> getSchoolFundingGroupCodesFromRedisCache() {
		log.debug("**** Getting school funding group codes from Redis Cache.");
		List<SchoolFundingGroupCode> schoolFundingGroupCodes =   schoolFundingGroupCodeTransformer.transformToDTO(schoolFundingGroupCodeRedisRepository.findAll());
		return CollectionUtils.isEmpty(schoolFundingGroupCodes) ? loadSchoolFundingGroupCodesFromInstituteApiIntoRedisCacheAsync() : schoolFundingGroupCodes;
	}

	public void initializeSchoolFundingGroupCodeCache(boolean force) {
		serviceHelper.initializeCache(force, CacheKey.SCHOOL_FUNDING_GROUP_CODE_CACHE, this);
	}
	
	/**
	 * Updates the school category code in the cache
	 * @param schoolCategoryCode the school detail object
	 */
	public void updateSchoolCategoryCode(SchoolCategoryCode schoolCategoryCode) throws ServiceException {
		if (schoolCategoryCode != null) {
			schoolCategoryCodeRedisRepository.save(schoolCategoryCodeTransformer.transformToEntity(schoolCategoryCode));
		}
	}

}
