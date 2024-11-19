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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.*;

@Slf4j
@RequiredArgsConstructor
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

	public SchoolDetail getSchoolDetailBySchoolId(UUID schoolId) {
		log.debug("**** Getting school Details By SchoolId from Redis Cache.");
		return schoolDetailRedisRepository.findById(String.valueOf(schoolId)).map(schoolDetailTransformer::transformToDTO).orElse(null);
	}

	public void initializeSchoolDetailCache(boolean force) {
		serviceHelper.initializeCache(force, CacheKey.SCHOOL_DETAIL_CACHE, this);
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

	public List<School> getSchoolsByParams(UUID districtId, String mincode) {
		if(districtId == null && mincode == null) {
			return schoolTransformer.transformToDTO(schoolRedisRepository.findAll());
		} else if (mincode == null) {
			return schoolTransformer.transformToDTO(schoolRedisRepository.findAllByDistrictId(String.valueOf(districtId)));
		} else if(districtId == null) {
			Optional<SchoolEntity> schoolOptional = schoolRedisRepository.findByMincode(mincode);
			return schoolOptional.map(schoolEntity -> List.of(schoolTransformer.transformToDTO(schoolEntity))).orElse(Collections.emptyList());
		} else {
			return schoolTransformer.transformToDTO(schoolRedisRepository.findAllByDistrictIdAndMincode(String.valueOf(districtId), mincode));
		}
	}
}
