package ca.bc.gov.educ.api.trax.service.institute;

import ca.bc.gov.educ.api.trax.constant.CacheKey;
import ca.bc.gov.educ.api.trax.exception.ServiceException;
import ca.bc.gov.educ.api.trax.mapper.GradSchoolMapper;
import ca.bc.gov.educ.api.trax.model.dto.GradSchool;
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
import ca.bc.gov.educ.api.trax.util.JsonTransformer;
import com.fasterxml.jackson.core.type.TypeReference;
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
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service("instituteSchoolService")
public class SchoolService {

	private EducGradTraxApiConstants constants;
	private WebClient webClient;
	private WebClient gradSchoolWebClient;
	SchoolRedisRepository schoolRedisRepository;
	SchoolDetailRedisRepository schoolDetailRedisRepository;
	SchoolTransformer schoolTransformer;
	SchoolDetailTransformer schoolDetailTransformer;
	ServiceHelper<SchoolService> serviceHelper;
	RESTService restService;
	CacheService cacheService;
	JsonTransformer jsonTransformer;

	GradSchoolMapper gradSchoolMapper = GradSchoolMapper.mapper;

	@Autowired
	public SchoolService(EducGradTraxApiConstants constants, @Qualifier("gradInstituteApiClient") WebClient webClient,
						 @Qualifier("gradSchoolApiClient") WebClient gradSchoolWebClient,
						 SchoolRedisRepository schoolRedisRepository, SchoolDetailRedisRepository schoolDetailRedisRepository,
						 SchoolTransformer schoolTransformer, SchoolDetailTransformer schoolDetailTransformer,
						 ServiceHelper<SchoolService> serviceHelper, RESTService restService, CacheService cacheService,
						 JsonTransformer jsonTransformer) {
		this.constants = constants;
		this.webClient = webClient;
		this.gradSchoolWebClient = gradSchoolWebClient;
		this.schoolRedisRepository = schoolRedisRepository;
		this.schoolDetailRedisRepository = schoolDetailRedisRepository;
		this.schoolTransformer = schoolTransformer;
		this.schoolDetailTransformer = schoolDetailTransformer;
		this.serviceHelper = serviceHelper;
		this.restService = restService;
		this.cacheService = cacheService;
		this.jsonTransformer = jsonTransformer;
	}

	public List<School> getSchoolsFromRedisCache() {
		log.debug("**** Getting schools from Redis Cache.");
		List<School> schools =  schoolTransformer.transformToDTO(schoolRedisRepository.findAll());
		return CollectionUtils.isEmpty(schools) ? loadSchoolsFromInstituteApiIntoRedisCacheAsync() : schools;
	}

	public School getSchoolByMinCodeFromRedisCache(String minCode) {
		if (StringUtils.isBlank(minCode)) { log.info("getSchoolByMinCodeFromRedisCache: minCode is null."); return null; }
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
			var response = this.restService.get(constants.getAllSchoolsFromInstituteApiUrl(),
					List.class, webClient);
			List<SchoolEntity> schoolEntities= response != null ? jsonTransformer.convertValue(response, new TypeReference<List<SchoolEntity>>() {}) : Collections.emptyList();
			List<GradSchool> gradSchools = getSchoolGradDetailsFromSchoolApi();
			if(CollectionUtils.isEmpty(gradSchools)) {
				throw new ServiceException("Unable to fetch grad schools from Grad School API.");
			}
			Map<String, GradSchool> gradSchoolResponse = gradSchools.stream()
					.collect(Collectors.toMap(GradSchool::getSchoolID, Function.identity(), (existing, replacement) -> replacement));
			return gradSchoolMapper.toSchools(schoolEntities, gradSchoolResponse);
		} catch (WebClientResponseException e) {
			log.warn(String.format("Error getting Common School List: %s", e.getMessage()));
		} catch (Exception e) {
			log.error(String.format("Error getting data from Institute api: %s", e.getMessage()));
		}
		return Collections.emptyList();
	}

	public List<GradSchool> getSchoolGradDetailsFromSchoolApi() {
		try {
			log.debug("****Before Calling Grad School API for schools");
			var response =  this.restService.get(constants.getSchoolGradDetailsFromGradSchoolApiUrl(),
					List.class, gradSchoolWebClient);
			return response != null ? jsonTransformer.convertValue(response, new TypeReference<List<GradSchool>>() {}) : Collections.emptyList();
		} catch (WebClientResponseException e) {
			log.warn(String.format("Error getting grad details from Grad School api : %s", e.getMessage()));
		} catch (Exception e) {
			log.error(String.format("Error getting grad details from Grad School api : %s", e.getMessage()));
		}
		return Collections.emptyList();
	}

	public School getSchoolByMinCodeFromInstituteApi(String minCode) {
		if(StringUtils.isNotBlank(minCode)) {
			return getSchoolsFromInstituteApi().stream().filter(school -> school.getMincode().equals(minCode)).findFirst().orElse(null);
		} else {
			log.warn("getSchoolByMinCodeFromInstituteApi: minCode is null.");
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
		if (StringUtils.isBlank(schoolId)) { log.info("getSchoolDetailByIdFromInstituteApi: schoolId is null."); return null; }
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
			return this.restService.get(String.format("%s?pageNumber=%d&pageSize=%d", constants.getSchoolsPaginatedFromInstituteApiUrl(), pageNumber, pageSize),
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
		PaginatedResponse<SchoolDetail> response =  getSchoolDetailsPaginatedFromInstituteApi(pageNumber);
		if (response == null) {
			return schoolDetails;
		}
		List<SchoolDetail> pagedSchoolDetails = jsonTransformer.convertValue(response.getContent(), new TypeReference<List<SchoolDetail>>() {});
		if(!CollectionUtils.isEmpty(pagedSchoolDetails)) {
			schoolDetails.addAll(pagedSchoolDetails);
		}
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
		if (StringUtils.isBlank(minCode)) { log.info("getSchoolDetailByMincodeFromRedisCache: minCode is null."); return null; }
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
		if (schoolId == null) { log.info("getSchoolDetailBySchoolIdFromRedisCache: schoolId is null.");  return null; }
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
		if (StringUtils.isBlank(schoolCategoryCode)) { log.info("getSchoolDetailsBySchoolCategoryCode: schoolCategoryCode is null."); return Collections.emptyList(); }
		List<SchoolDetail> schoolDetails = schoolDetailTransformer.transformToDTO(schoolDetailRedisRepository.findBySchoolCategoryCode(schoolCategoryCode));
		if(CollectionUtils.isEmpty(schoolDetails)) {
			log.debug("School detail not found in cache for schoolCategoryCode: {}, fetched from API.", schoolCategoryCode);
			return loadSchoolDetailsFromInstituteApiIntoRedisCacheAsync().stream().filter(schoolDetail -> schoolDetail.getSchoolCategoryCode().equals(schoolCategoryCode)).toList();
		}
		return schoolDetails;
	}

	public List<SchoolDetail> getSchoolDetailsByDistrictFromRedisCache(String districtId) {
		if (StringUtils.isBlank(districtId)) { log.info("getSchoolDetailsByDistrictFromRedisCache: districtId is null."); return Collections.emptyList(); }
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
		if (StringUtils.isBlank(schoolId)) { log.info("updateSchoolCache: schoolId is null."); return; }
		// get details from institute
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
			// obtain grad school details from Grad School API
			log.debug("Obtaining grad school details for school {}.",  schoolDetail.getSchoolId());
			GradSchool gradSchool = this.restService.get(String.format(constants.getSchoolGradDetailsByIdFromGradSchoolApiUrl(), schoolDetail.getSchoolId()),
					GradSchool.class, gradSchoolWebClient);
			schoolDetail.setCanIssueTranscripts(gradSchool.getCanIssueTranscripts().equalsIgnoreCase("Y"));
			schoolDetail.setCanIssueCertificates(gradSchool.getCanIssueCertificates().equalsIgnoreCase("Y"));
			log.debug("Updating school {} in cache.",  schoolDetail.getSchoolId());
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
		if (schoolId == null) { log.info("getSchoolBySchoolId: schoolId is null."); return Optional.empty(); }
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
		return Strings.isNullOrEmpty(value) ? "(.*)" : "*".concat(value).concat("*").replace("\\*", "(.*)");
	}
}
