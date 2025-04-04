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
import ca.bc.gov.educ.api.trax.model.dto.institute.PaginatedResponse;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
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
	@Autowired
	CacheService cacheService;

	ObjectMapper mapper =  new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

	public List<School> getSchoolsFromRedisCache() {
		log.debug("**** Getting schools from Redis Cache.");
		List<School> schools =  schoolTransformer.transformToDTO(schoolRedisRepository.findAll());
		return CollectionUtils.isEmpty(schools) ? loadSchoolsFromInstituteApiIntoRedisCacheAsync() : schools;
	}

	public School getSchoolByMinCodeFromRedisCache(String minCode) {
		if (StringUtils.isBlank(minCode)) { return null; }
		log.debug("Get School by minCode from Redis Cache: {}", minCode);
		return schoolRedisRepository.findByMincode(minCode)
				.map(schoolTransformer::transformToDTO)
				.orElseGet(() -> {
				 log.debug("School not found in cache, fetched from API.");
				 School school = getSchoolByMinCodeFromInstituteApi(minCode);
				 if(school != null) {
					 updateSchoolCache(school.getSchoolId());
				 }
				 return school;
		});
	}

	public boolean checkIfSchoolExists(String minCode) {
		Optional<SchoolEntity> schoolOptional = schoolRedisRepository.findByMincode(minCode);
		return schoolOptional.isPresent() || getSchoolByMinCodeFromInstituteApi(minCode) != null;
	}

	private List<School> loadSchoolsFromInstituteApiIntoRedisCacheAsync() {
		List<School> schools = getSchoolsFromInstituteApi();
		if(!CollectionUtils.isEmpty(schools)) {
			log.info(String.format("%s Schools fetched from Institute API.", schools.size()));
			cacheService.loadSchoolsIntoRedisCacheAsync(schoolTransformer.transformToEntity(schools));
		}
		return schools;
	}

	public void loadSchoolsIntoRedisCache(List<School> schools) {
		if(!CollectionUtils.isEmpty(schools)) {
			log.info(String.format("%s Schools fetched from Institute API.", schools.size()));
			cacheService.loadSchoolsIntoRedisCache(schoolTransformer.transformToEntity(schools));
		}
	}

	public List<School> getSchoolsFromInstituteApi() {
		try {
			log.debug("****Before Calling Institute API for schools");
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

	public School getSchoolByMinCodeFromInstituteApi(String minCode) {
		if(StringUtils.isNotBlank(minCode)) {
			return getSchoolsFromInstituteApi().stream().filter(school -> school.getMincode().equals(minCode)).findFirst().orElse(null);
		}
		return null;
	}

	/**
	 * This method invokes loadSchoolsIntoRedisCache.
	 * @param force
	 */
	public void initializeSchoolCache(boolean force) {
		serviceHelper.initializeCache(force, CacheKey.SCHOOL_CACHE, this);
	}

	public SchoolDetail getSchoolDetailByIdFromInstituteApi(String schoolId) {
		if (StringUtils.isBlank(schoolId)) { return null; }
		try {
			log.debug("****Before Calling Institute API for schoolId: {}", schoolId);
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

	private PaginatedResponse<SchoolDetail> getSchoolDetailsPaginatedFromInstituteApi(int pageNumber) {
		int pageSize = 1000;
		try {
			return this.restService.get(String.format(constants.getSchoolsPaginatedFromInstituteApiUrl()+"?pageNumber=%d&pageSize=%d", pageNumber, pageSize),
					PaginatedResponse.class, webClient);
		} catch (WebClientResponseException e) {
			log.warn(String.format("Error getting School Details from Institute API: %s", e.getMessage()));
		} catch (Exception e) {
			log.error(String.format("Error while calling institute-api: %s", e.getMessage()));
		}
		log.warn("No school details found for the given search criteria.");
		return null;
	}

	// Recursive method to fetch school details page by page
	public List<SchoolDetail> getSchoolDetailsPaginatedFromInstituteApi(int pageNumber, List<SchoolDetail> schoolDetails) {
		PaginatedResponse response =  getSchoolDetailsPaginatedFromInstituteApi(pageNumber);
		if (response == null) {
			return schoolDetails;
		}
		schoolDetails.addAll(response.getContent().stream()
				.map(entry -> mapper.convertValue(entry, SchoolDetail.class))
				.toList());

		if (response.hasNext()) {
			return getSchoolDetailsPaginatedFromInstituteApi(response.nextPageable().getPageNumber(), schoolDetails);
		}
		return schoolDetails;
	}

	public List<SchoolDetail> getSchoolDetailsFromInstituteApi() {
		log.debug("****Before Calling Institute API for school details");
		return getSchoolDetailsPaginatedFromInstituteApi(0, new ArrayList<>());
	}

	public List<SchoolDetail> loadSchoolDetailsFromInstituteApiIntoRedisCacheAsync() {
		List<SchoolDetail> schoolDetails = getSchoolDetailsFromInstituteApi();
		if(!CollectionUtils.isEmpty(schoolDetails)) {
			log.info(String.format("%s School details fetched from Institute API.", schoolDetails.size()));
			cacheService.loadSchoolDetailsIntoRedisCacheAsync(schoolDetailTransformer.transformToEntity(schoolDetails));
		}
		return schoolDetails;
	}

	public void loadSchoolDetailsIntoRedisCache(List<SchoolDetail> schoolDetails) {
		if(!CollectionUtils.isEmpty(schoolDetails)) {
			log.info(String.format("%s Schools details fetched from Institute API.", schoolDetails.size()));
			cacheService.loadSchoolDetailsIntoRedisCache(schoolDetailTransformer.transformToEntity(schoolDetails));
		}
	}

	public List<SchoolDetail> getSchoolDetailsFromRedisCache() {
		log.debug("**** Getting school Details from Redis Cache.");
		List<SchoolDetail> schoolDetails = schoolDetailTransformer.transformToDTO(schoolDetailRedisRepository.findAll());
		return CollectionUtils.isEmpty(schoolDetails) ? loadSchoolDetailsFromInstituteApiIntoRedisCacheAsync() : schoolDetails;
	}

	public SchoolDetail getSchoolDetailByMincodeFromRedisCache(String minCode) {
		if (StringUtils.isBlank(minCode)) { return null; }
		log.debug("**** Getting school Details By Mincode from Redis Cache.");
		return schoolDetailRedisRepository.findByMincode(minCode)
				.map(schoolDetailTransformer::transformToDTO)
				.orElseGet(() -> {
					log.debug("School detail not found in cache for mincode: {}, , fetched from API.", minCode);
					School school = getSchoolByMinCodeFromRedisCache(minCode);
					if(school != null) {
						return getSchoolDetailBySchoolIdFromRedisCache(UUID.fromString(school.getSchoolId()));
					}
					return null;
				});
	}

	public SchoolDetail getSchoolDetailBySchoolIdFromRedisCache(UUID schoolId) {
		if (schoolId == null) { return null; }
		log.debug("**** Getting school Details By SchoolId from Redis Cache.");
		return schoolDetailRedisRepository.findById(String.valueOf(schoolId))
				.map(schoolDetailTransformer::transformToDTO)
				.orElseGet(() -> {
					log.debug("School detail not found in cache for schoolId: {}, , fetched from API.", schoolId);
					SchoolDetail schoolDetail = getSchoolDetailByIdFromInstituteApi(schoolId.toString());
					updateSchoolCache(schoolDetail);
					return schoolDetail;
				});
	}

	public List<SchoolDetail> getSchoolDetailsBySchoolCategoryCode(String schoolCategoryCode) {
		if (StringUtils.isBlank(schoolCategoryCode)) { return Collections.emptyList(); }
		List<SchoolDetail> schoolDetails = schoolDetailTransformer.transformToDTO(schoolDetailRedisRepository.findBySchoolCategoryCode(schoolCategoryCode));
		if(CollectionUtils.isEmpty(schoolDetails)) {
			log.debug("School detail not found in cache for schoolCategoryCode: {}, fetched from API.", schoolCategoryCode);
			return loadSchoolDetailsFromInstituteApiIntoRedisCacheAsync().stream().filter(schoolDetail -> schoolDetail.getSchoolCategoryCode().equals(schoolCategoryCode)).toList();
		}
		return schoolDetails;
	}

	public List<SchoolDetail> getSchoolDetailsByDistrictFromRedisCache(String districtId) {
		if (StringUtils.isBlank(districtId)) { return Collections.emptyList(); }
		List<SchoolDetail> schoolDetails = schoolDetailTransformer.transformToDTO(schoolDetailRedisRepository.findByDistrictId(districtId));
		if(CollectionUtils.isEmpty(schoolDetails)) {
			log.debug("School detail not found in cache for districtId: {}, fetched from API.", districtId);
			return loadSchoolDetailsFromInstituteApiIntoRedisCacheAsync().stream().filter(schoolDetail -> schoolDetail.getDistrictId().equals(districtId)).toList();
		}
		return schoolDetails;
	}

	/**
	 * Updates the school and school details in the cache
	 * based on schoolId
	 * @param schoolId the school id guid
	 */
	public void updateSchoolCache(String schoolId) throws ServiceException {
		if (StringUtils.isBlank(schoolId)) { return; }
		// get details from institute
		log.debug("Updating school {} in cache.",  schoolId);
		SchoolDetail schoolDetail = this.restService.get(String.format(constants.getSchoolDetailsByIdFromInstituteApiUrl(), schoolId),
				SchoolDetail.class, webClient);
		log.debug("Retrieved school: {} from Institute API", schoolId);
		updateSchoolCache(schoolDetail);
	}

	/**
	 * Updates the school and school details in the cache
	 * @param schoolDetail the school detail object
	 */
	public void updateSchoolCache(SchoolDetail schoolDetail) throws ServiceException {
		if(schoolDetail != null) {
			schoolDetailRedisRepository.save(schoolDetailTransformer.transformToEntity(schoolDetail));
			schoolRedisRepository.save(schoolDetailTransformer.transformToSchoolEntity(schoolDetail));
		}
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
		if (schoolId == null) { return Optional.empty(); }
		log.debug("**** Getting school By SchoolId from Redis Cache.");
		return schoolRedisRepository.findById(String.valueOf(schoolId))
				.map(schoolTransformer::transformToDTO)
				.or(() -> {
					log.debug("School not found in cache for schoolId: {}, , fetched from API.", schoolId);
					Optional<School> school = getSchoolsFromInstituteApi().stream().filter(entry -> entry.getSchoolId().equals(schoolId.toString())).findFirst();
					if(school.isPresent()) {
						updateSchoolCache(school.get().getSchoolId());
					}
					return school;
				});
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
		return filterByCriteria(criteria, getSchoolsFromRedisCache());
	}

	/**
	 * Filter a list of SchoolEntities by given criteria
	 * @param criteria
	 * @param schoolEntities
	 * @return
	 */
	private List<School> filterByCriteria(SchoolSearchCriteria criteria, List<School> schoolEntities) {
		List<School> schools = new ArrayList<>();
		for (School school : schoolEntities) {
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
