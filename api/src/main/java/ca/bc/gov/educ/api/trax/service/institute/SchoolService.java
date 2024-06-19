package ca.bc.gov.educ.api.trax.service.institute;

import ca.bc.gov.educ.api.trax.constant.CacheKey;
import ca.bc.gov.educ.api.trax.model.dto.institute.School;
import ca.bc.gov.educ.api.trax.model.dto.institute.SchoolDetail;
import ca.bc.gov.educ.api.trax.model.entity.institute.SchoolEntity;
import ca.bc.gov.educ.api.trax.model.transformer.institute.SchoolDetailTransformer;
import ca.bc.gov.educ.api.trax.model.transformer.institute.SchoolTransformer;
import ca.bc.gov.educ.api.trax.repository.redis.SchoolDetailRedisRepository;
import ca.bc.gov.educ.api.trax.repository.redis.SchoolRedisRepository;
import ca.bc.gov.educ.api.trax.util.EducGradTraxApiConstants;
import ca.bc.gov.educ.api.trax.util.ThreadLocalStateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

	public List<School> getSchoolsFromInstituteApi() {
		try {
			log.debug("****Before Calling Institute API");
			List<SchoolEntity> schools;
			schools = webClient.get()
					.uri(constants.getAllSchoolsFromInstituteApiUrl())
					.headers(h -> {
						h.set(EducGradTraxApiConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID());
					})
					.retrieve()
					.bodyToMono(new ParameterizedTypeReference<List<SchoolEntity>>(){}).block();
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
		log.info(String.format("%s Schools Loaded into cache.", schools.size()));
	}

	public List<School> getSchoolsFromRedisCache() {
		log.debug("**** Getting schools from Redis Cache.");
		return  schoolTransformer.transformToDTO(schoolRedisRepository.findAll());
	}

	public void initializeSchoolCache(boolean force) {
		serviceHelper.initializeCache(force, CacheKey.SCHOOL_CACHE, this);
	}

    public SchoolDetail getSchoolDetailByIdFromInstituteApi(String schoolId) {
        try {
            return webClient.get().uri(
                            String.format(constants.getSchoolDetailsByIdFromInstituteApiUrl(), schoolId))
					.headers(h -> {
						h.set(EducGradTraxApiConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID());
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

        List<School> schools = getSchoolsFromRedisCache();
        List<SchoolDetail> schoolDetails = new ArrayList<SchoolDetail>();

        for (School s : schools) {
            SchoolDetail sd = new SchoolDetail();

            sd = getSchoolDetailByIdFromInstituteApi(s.getSchoolId());
            schoolDetails.add(sd);
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

	public void initializeSchoolDetailCache(boolean force) {
		serviceHelper.initializeCache(force, CacheKey.SCHOOL_DETAIL_CACHE, this);
	}

}
