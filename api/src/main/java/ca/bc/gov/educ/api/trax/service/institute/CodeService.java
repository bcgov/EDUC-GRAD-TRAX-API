package ca.bc.gov.educ.api.trax.service.institute;

import ca.bc.gov.educ.api.trax.model.entity.institute.SchoolCategoryCodeEntity;
import ca.bc.gov.educ.api.trax.model.entity.institute.SchoolFundingGroupCodeEntity;
import ca.bc.gov.educ.api.trax.repository.redis.SchoolCategoryCodeRedisRepository;
import ca.bc.gov.educ.api.trax.repository.redis.SchoolFundingGroupCodeRedisRepository;
import ca.bc.gov.educ.api.trax.util.EducGradTraxApiConstants;
import ca.bc.gov.educ.api.trax.util.RestUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
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
	private WebClient webClient;

	@Autowired
	SchoolCategoryCodeRedisRepository schoolCategoryCodeRedisRepository;

	@Autowired
	SchoolFundingGroupCodeRedisRepository schoolFundingGroupCodeRedisRepository;

	@Autowired
	private RestUtils restUtils;

	public List<SchoolCategoryCodeEntity> getSchoolCategoryCodesFromInstituteApi() {
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
			return schoolCategoryCodes;
		} catch (WebClientResponseException e) {
			log.warn(String.format("Error getting School Category Codes: %s", e.getMessage()));
		} catch (Exception e) {
			log.error(String.format("Error while calling school-api: %s", e.getMessage()));
		}
		return null;
	}

	public void loadSchoolCategoryCodesIntoRedisCache(List<SchoolCategoryCodeEntity> schoolCategoryCodes) {
		schoolCategoryCodeRedisRepository.saveAll(schoolCategoryCodes);
	}

	public List<SchoolFundingGroupCodeEntity> getSchoolFundingGroupCodesFromInstituteApi() {
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
			return schoolFundingGroupCodes;
		} catch (WebClientResponseException e) {
			log.warn(String.format("Error getting School Funding Group Codes: %s", e.getMessage()));
		} catch (Exception e) {
			log.error(String.format("Error while calling school-api: %s", e.getMessage()));
		}
		return null;
	}

	public void loadSchoolFundingGroupCodesIntoRedisCache(List<SchoolFundingGroupCodeEntity> schoolFundingGroupCodes) {
		schoolFundingGroupCodeRedisRepository
				.saveAll(schoolFundingGroupCodes);
	}
}
