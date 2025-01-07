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

import ca.bc.gov.educ.api.trax.util.SearchUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;


import java.util.*;

@Slf4j
@Service("InstituteSchoolService")
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
		return null;
	}

	public void loadSchoolsIntoRedisCache(List<ca.bc.gov.educ.api.trax.model.dto.institute.School> schools) {
		schoolRedisRepository
				.saveAll(schoolTransformer.transformToEntity(schools));
		log.info(String.format("%s Schools Loaded into cache.", schools.size()));
	}

	public List<School> getSchoolsFromRedisCache() {
		log.debug("**** Getting schools from Redis Cache.");
		Iterable<SchoolEntity> schoolEntities = schoolRedisRepository.findAll();
		if ( (!schoolEntities.iterator().hasNext())){
			log.debug("Get Schools  from Redis Cache returned empty");
			List<School> schools = getSchoolsFromInstituteApi();
			if ((schools != null) && (!schools.isEmpty())) {
				loadSchoolsIntoRedisCache(schools);
				return schools;
			}
		}
		return  schoolTransformer.transformToDTO(schoolEntities);
	}

	public School getSchoolByMincodeFromRedisCache(String mincode) {
		log.debug("Get School by Mincode from Redis Cache");
		SchoolEntity schoolEntity = schoolRedisRepository.findByMincode(mincode);
		if (schoolEntity == null) {
			log.debug("Get School by Mincode from Redis Cache returned null");
			List<School> schools = getSchoolsBySearchCriteriaFromInstituteApi("mincode", mincode);
			if ((schools != null) && (!schools.isEmpty())) {
				School school = schools.get(0);
				if (school != null) {
					loadSchoolsIntoRedisCache(List.of(school));
					return school;
				}
			}
		}
		return schoolTransformer.transformToDTO(schoolEntity);
	}

	public boolean checkIfSchoolExists(String minCode) {
		SchoolEntity schoolEntity = schoolRedisRepository.findByMincode(minCode);
		return schoolEntity != null;
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



	public List <School> getSchoolsBySearchCriteriaFromInstituteApi(String key, String value) {
		try {
			log.debug("****Before Calling Institute API");
			HashMap<String, String> params;
			HashMap<String, String> searchInput = new HashMap<>();
			searchInput.put(key, value);

			try {
				params  = SearchUtil.searchStringsToHTTPParams(searchInput);
			} catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}


			List <SchoolEntity> schoolEntities = this.restService.get(constants.getSchoolsPaginated(),params,
					List.class, webClient);
			return schoolTransformer.transformToDTO(schoolEntities);
		} catch (WebClientResponseException e) {
			log.warn("Error getting School By search Criteria from Institute API");
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
		log.debug(String.format("%s School Details Loaded into cache.", schoolDetails.size()));
	}

	public List<SchoolDetail> getSchoolDetailsFromRedisCache() {
		log.debug("**** Getting school Details from Redis Cache.");

		Iterable<SchoolDetailEntity> schoolDetailEntities = schoolDetailRedisRepository.findAll();
		if ( (!schoolDetailEntities.iterator().hasNext())){
			log.debug("Get Schools details  from Redis Cache returned empty");
			List<SchoolDetail> schoolDetails = this.getSchoolDetailsFromInstituteApi();
			if ((schoolDetails  != null) && (!schoolDetails .isEmpty())) {
				loadSchoolDetailsIntoRedisCache(schoolDetails);
				return schoolDetails;
			}
		}
		return schoolDetailTransformer.transformToDTO(schoolDetailEntities);
	}

	public SchoolDetail getSchoolDetailByMincodeFromRedisCache(String mincode) {
		log.debug("**** Getting school Details By Mincode from Redis Cache.");
		SchoolDetailEntity schoolDetailEntity = schoolDetailRedisRepository.findByMincode(mincode);
		if ( schoolDetailEntity == null){
			log.debug("Get Schools details  by mincode from Redis Cache returned empty");
			List<School> schools = this.getSchoolsBySearchCriteriaFromInstituteApi("mincode", mincode);
			if ((schools!= null) &&(!schools.isEmpty())){
				School school = schools.get(0);
				SchoolDetail schoolDetail = this.getSchoolDetailByIdFromInstituteApi(school.getSchoolId());
				if ((schoolDetail  != null) ) {
					loadSchoolDetailsIntoRedisCache(List.of(schoolDetail));
					return schoolDetail ;
				}
			}
		}
		return schoolDetailTransformer.transformToDTO(schoolDetailEntity );
	}

	public void initializeSchoolDetailCache(boolean force) {
		serviceHelper.initializeCache(force, CacheKey.SCHOOL_DETAIL_CACHE, this);
	}

	public List<SchoolDetail> getSchoolDetailsBySchoolCategoryCode(String schoolCategoryCode) {

		log.debug("**** Getting school Details By school category from Redis Cache.");
		List<SchoolDetailEntity> schoolDetailEntities = schoolDetailRedisRepository.findBySchoolCategoryCode(schoolCategoryCode);

		if ( (schoolDetailEntities == null) ||(schoolDetailEntities.isEmpty())){
			log.debug("Get Schools details  by category code from Redis Cache returned empty");

			List<School> schools = this.getSchoolsBySearchCriteriaFromInstituteApi("schoolCategoryCode",schoolCategoryCode);
			ArrayList<SchoolDetail> schoolDetails = new ArrayList<>();
			if ((schools != null) && (!schools.isEmpty())) {
				schools.forEach(school -> {
					schoolDetails.add(this.getSchoolDetailByIdFromInstituteApi(school.getSchoolId()));
				});
				loadSchoolDetailsIntoRedisCache(schoolDetails);
				return schoolDetails;
			}
		}
		return schoolDetailTransformer.transformToDTO(schoolDetailEntities);
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
}
