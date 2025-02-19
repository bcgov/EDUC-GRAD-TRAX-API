package ca.bc.gov.educ.api.trax.service.institute;

import ca.bc.gov.educ.api.trax.constant.CacheKey;
import ca.bc.gov.educ.api.trax.exception.ServiceException;
import ca.bc.gov.educ.api.trax.model.dto.institute.School;
import ca.bc.gov.educ.api.trax.model.dto.institute.SchoolDetail;
import ca.bc.gov.educ.api.trax.model.entity.institute.SchoolEntity;
import ca.bc.gov.educ.api.trax.model.entity.institute.SchoolDetailEntity;
import ca.bc.gov.educ.api.trax.model.transformer.institute.SchoolDetailTransformer;
import ca.bc.gov.educ.api.trax.model.transformer.institute.SchoolTransformer;
import ca.bc.gov.educ.api.trax.repository.redis.SchoolDetailRedisRepository;
import ca.bc.gov.educ.api.trax.repository.redis.SchoolRedisRepository;
import ca.bc.gov.educ.api.trax.service.RESTService;
import ca.bc.gov.educ.api.trax.util.EducGradTraxApiConstants;
import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service("instituteSchoolService")
public class SchoolService {

	@Autowired
	private EducGradTraxApiConstants constants;
	@Autowired
	@Qualifier("instituteWebClient")
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
	ServiceHelper<SchoolService> serviceHelper;
	@Autowired
	RESTService restService;

	public List<School> getSchoolsFromInstituteApi() {
		try {
			log.debug("****Before Calling Institute API");
			List<SchoolEntity> response = this.restService.get(constants.getAllSchoolsFromInstituteApiUrl(),
					List.class, webClient);
			return schoolTransformer.transformToDTO(response);
		} catch (WebClientResponseException e) {
			log.warn(String.format("Error getting Common School List: %s", e.getMessage()));
		} catch (Exception e) {
			log.error(String.format("Error getting data from Institute api: %s", e.getMessage()));
		}
		return Collections.emptyList();
	}

	public void loadSchoolsIntoRedisCache(List<ca.bc.gov.educ.api.trax.model.dto.institute.School> schools) {
		schoolRedisRepository
				.saveAll(schoolTransformer.transformToEntity(schools));
		log.info(String.format("%s Schools Loaded into cache.", schools.size()));
	}

	public List<School> getSchoolsFromRedisCache() {
		log.debug("**** Getting schools from Redis Cache.");
		return  schoolTransformer.transformToDTO(schoolRedisRepository.findAll());
	}

	public School getSchoolByMinCodeFromRedisCache(String minCode) {
		log.debug("Get School by Mincode from Redis Cache: {}", minCode);
		return schoolRedisRepository.findByMincode(minCode).map(schoolTransformer::transformToDTO).orElse(null);
	}

	public boolean checkIfSchoolExists(String minCode) {
		Optional<SchoolEntity> schoolOptional = schoolRedisRepository.findByMincode(minCode);
		return schoolOptional.isPresent();
	}

	public void initializeSchoolCache(boolean force) {
		serviceHelper.initializeCache(force, CacheKey.SCHOOL_CACHE, this);
	}

    public SchoolDetail getSchoolDetailByIdFromInstituteApi(String schoolId) {
        try {
			log.debug("****Before Calling Institute API");
			SchoolDetailEntity sde = this.restService.get(String.format(constants.getSchoolDetailsByIdFromInstituteApiUrl(), schoolId),
					SchoolDetailEntity.class, webClient);
			return schoolDetailTransformer.transformToDTO(sde);
        } catch (WebClientResponseException e) {
            log.warn("Error getting School Details");
        } catch (Exception e) {
            log.error(String.format("Error while calling Institute api: %s", e.getMessage()));
        }
        return null;
    }

    public List<SchoolDetail> getSchoolDetailsFromInstituteApi() {

        List<School> schools = getSchoolsFromRedisCache();
        List<SchoolDetail> schoolDetails = new ArrayList<>();
        for (School s : schools) {
            schoolDetails.add(getSchoolDetailByIdFromInstituteApi(s.getSchoolId()));
        }
        return schoolDetails;
    }

	public void loadSchoolDetailsIntoRedisCache(List<SchoolDetail> schoolDetails) {
		schoolDetailRedisRepository
				.saveAll(schoolDetailTransformer.transformToEntity(schoolDetails));
		log.info(String.format("%s School Details Loaded into cache.", schoolDetails.size()));
	}

	public List<SchoolDetail> getSchoolDetailsFromRedisCache() {
		log.debug("**** Getting school Details from Redis Cache.");
		return schoolDetailTransformer.transformToDTO(schoolDetailRedisRepository.findAll());
	}

	public SchoolDetail getSchoolDetailByMincodeFromRedisCache(String mincode) {
		log.debug("**** Getting school Details By Mincode from Redis Cache.");
		return schoolDetailRedisRepository.findByMincode(mincode).map(schoolDetailTransformer::transformToDTO).orElse(null);
	}

	public SchoolDetail getSchoolDetailBySchoolIdFromRedisCache(UUID schoolId) {
		log.debug("**** Getting school Details By SchoolId from Redis Cache.");
		return schoolDetailRedisRepository.findById(String.valueOf(schoolId)).map(schoolDetailTransformer::transformToDTO).orElse(null);
	}

	public List<SchoolDetail> getSchoolDetailsBySchoolCategoryCode(String schoolCategoryCode) {

		return schoolDetailTransformer.transformToDTO(
				schoolDetailRedisRepository.findBySchoolCategoryCode(schoolCategoryCode));
	}

	public List<SchoolDetail> getSchoolDetailsByDistrictFromRedisCache(String districtId) {
		return schoolDetailTransformer.transformToDTO(
				schoolDetailRedisRepository.findByDistrictId(districtId));
	}

	/**
	 * Updates the school and school details in the cache
	 * based on schoolId
	 * @param schoolId the school id guid
	 */
	public void updateSchoolCache(String schoolId) throws ServiceException {
		// get details from institute
		log.debug("Updating school {} in cache.",  schoolId);
		SchoolDetail schoolDetail = this.restService.get(String.format(constants.getSchoolDetailsByIdFromInstituteApiUrl(), schoolId),
				SchoolDetail.class, webClient);
		log.debug("Retrieved school: {} from Institute API", schoolDetail.getSchoolId());
		updateSchoolCache(schoolDetail);
	}

	/**
	 * Updates the school and school details in the cache
	 * @param schoolDetail the school detail object
	 */
	public void updateSchoolCache(SchoolDetail schoolDetail) throws ServiceException {
		schoolDetailRedisRepository.save(schoolDetailTransformer.transformToEntity(schoolDetail));
		schoolRedisRepository.save(schoolDetailTransformer.transformToSchoolEntity(schoolDetail));
	}

	/**
	 * Updates the school and school details in the cache
	 * based on schoolId
	 * @param schoolIds the school id guids
	 */
	public void updateSchoolCache(List<String> schoolIds) throws ServiceException {
		for (String schoolId : schoolIds) {
			updateSchoolCache(schoolId);
		}
	}

	public Optional<School> getSchoolBySchoolId(UUID schoolId) {
		return schoolRedisRepository.findById(String.valueOf(schoolId)).map(schoolTransformer::transformToDTO);
	}

	/**
	 * Get a list of schools that match the given params with wildcards
	 * @param districtId
	 * @param mincode
	 * @param displayName
	 * @param distNo
	 * @return
	 */
	@Transactional(readOnly = true)
	public List<School> getSchoolsByParams(String districtId, String mincode, String displayName, String distNo, List<String> schoolCategoryCodes) {

		SchoolSearchCriteria criteria = SchoolSearchCriteria.builder()
				.districtId(transformToWildcard(districtId))
				.mincode(transformToWildcard(mincode))
				.displayName(transformToWildcard(displayName))
				.distNo(transformToWildcard(distNo))
				.schoolCategoryCodes(schoolCategoryCodes)
				.build();

		log.debug(criteria.toString());
		List<SchoolEntity> schools = filterByCriteria(criteria, schoolRedisRepository.findAll());
		return schoolTransformer.transformToDTO(schools);
	}

	/**
	 * Filter a list of SchoolEntities by given criteria
	 * @param criteria
	 * @param schoolEntities
	 * @return
	 */
	private List<SchoolEntity> filterByCriteria(SchoolSearchCriteria criteria, Iterable<SchoolEntity> schoolEntities) {
		List<SchoolEntity> schools = new ArrayList<>();
		for (SchoolEntity school : schoolEntities) {
			if (school.getDistrictId().matches(criteria.getDistrictId())
					&& school.getMincode().matches(criteria.getMincode())
					&& school.getDisplayName().matches(criteria.getDisplayName())
					&& school.getMincode().substring(0, 3).matches(criteria.getDistNo())
					&& (criteria.getSchoolCategoryCodes() == null || criteria.getSchoolCategoryCodes().isEmpty() || criteria.getSchoolCategoryCodes().contains(school.getSchoolCategoryCode()))
			)
				schools.add(school);
		}
		return schools;
	}

	/**
	 * Transform '*' wildcard into Regex format
	 * @param value
	 * @return
	 */
	private String transformToWildcard(String value) {
		return Strings.isNullOrEmpty(value) ? "(.*)" : "*".concat(value).concat("*").replaceAll("\\*", "(.*)");
	}
}
