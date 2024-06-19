package ca.bc.gov.educ.api.trax.service.institute;

import ca.bc.gov.educ.api.trax.constant.CacheKey;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

@Slf4j
@Service("InstituteCodeService")
public class CodeService {

	@Autowired
	private EducGradTraxApiConstants constants;
	@Autowired
	@Qualifier("instituteWebClient")
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
	ServiceHelper<CodeService> serviceHelper;
	@Autowired
	RESTService restService;

	public List<SchoolCategoryCode> getSchoolCategoryCodesFromInstituteApi() {
		try {
			log.debug("****Before Calling Institute API");
			List<SchoolCategoryCodeEntity> response = this.restService.get(constants.getAllSchoolCategoryCodesFromInstituteApiUrl(),
					List.class);
			return schoolCategoryCodeTransformer.transformToDTO(response);
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
		log.info(String.format("%s School Category Codes Loaded into cache.", schoolCategoryCodes.size()));
	}

	public List<SchoolCategoryCode> getSchoolCategoryCodesFromRedisCache() {
		log.debug("**** Getting school category codes from Redis Cache.");
		return  schoolCategoryCodeTransformer.transformToDTO(schoolCategoryCodeRedisRepository.findAll());
	}

	public void initializeSchoolCategoryCodeCache(boolean force) {
		serviceHelper.initializeCache(force, CacheKey.SCHOOL_CATEGORY_CODE_CACHE, this);
	}

	public List<SchoolFundingGroupCode> getSchoolFundingGroupCodesFromInstituteApi() {
		try {
			log.debug("****Before Calling Institute API");
			List<SchoolFundingGroupCodeEntity> response = this.restService.get(constants.getAllSchoolFundingGroupCodesFromInstituteApiUrl(),
					List.class);
			return schoolFundingGroupCodeTransformer.transformToDTO(response);
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
		log.info(String.format("%s School Funding Group Codes Loaded into cache.", schoolFundingGroupCodes.size()));
	}

	public List<SchoolFundingGroupCode> getSchoolFundingGroupCodesFromRedisCache() {
		log.debug("**** Getting school funding group codes from Redis Cache.");
		return  schoolFundingGroupCodeTransformer.transformToDTO(schoolFundingGroupCodeRedisRepository.findAll());
	}

	public void initializeSchoolFundingGroupCodeCache(boolean force) {
		serviceHelper.initializeCache(force, CacheKey.SCHOOL_FUNDING_GROUP_CODE_CACHE, this);
	}
}
