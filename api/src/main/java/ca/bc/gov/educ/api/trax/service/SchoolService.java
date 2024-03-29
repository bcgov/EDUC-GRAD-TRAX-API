package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.model.dto.*;
import ca.bc.gov.educ.api.trax.model.entity.SchoolEntity;
import ca.bc.gov.educ.api.trax.model.transformer.DistrictTransformer;
import ca.bc.gov.educ.api.trax.model.transformer.SchoolTransformer;
import ca.bc.gov.educ.api.trax.repository.DistrictRepository;
import ca.bc.gov.educ.api.trax.repository.SchoolRepository;
import ca.bc.gov.educ.api.trax.repository.TraxSchoolSearchCriteria;
import ca.bc.gov.educ.api.trax.repository.TraxSchoolSearchSpecification;
import ca.bc.gov.educ.api.trax.util.EducGradTraxApiConstants;
import ca.bc.gov.educ.api.trax.util.ThreadLocalStateUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class SchoolService {

    @Autowired
    private SchoolRepository schoolRepository;  

    @Autowired
    private SchoolTransformer schoolTransformer;
    
    @Autowired
    private DistrictRepository districtRepository;  

    @Autowired
    private DistrictTransformer districtTransformer;

	@Autowired
	private CodeService codeService;

	@Autowired
	private WebClient webClient;

	@Autowired
	private EducGradTraxApiConstants constants;
    

    @SuppressWarnings("unused")
	private final Logger logger = LoggerFactory.getLogger(SchoolService.class);

     /**
     * Get all Schools in School DTO
     *
     * @return List of Schools
     */
    @Transactional(readOnly = true)
    public List<School> getSchoolList() {
        List<School> schoolList  = schoolTransformer.transformToDTO(schoolRepository.findAll());  
    	schoolList.forEach(sL -> {
    		District dist = districtTransformer.transformToDTO(districtRepository.findById(sL.getMinCode().substring(0, 3)));
    		if (dist != null) {
				sL.setDistrictName(dist.getDistrictName());
			}
			if(StringUtils.isNotBlank(sL.getCountryCode())) {
				GradCountry country = codeService.getSpecificCountryCode(sL.getCountryCode());
				if(country != null) {
					sL.setCountryName(country.getCountryName());
				}
			}
			if(StringUtils.isNotBlank(sL.getProvCode())) {
				GradProvince province = codeService.getSpecificProvinceCode(sL.getProvCode());
				if(province != null) {
					sL.setProvinceName(province.getProvName());
				}
			}
    	});
        return schoolList;
    }

	@Transactional(readOnly = true)
	public School getSchoolDetails(String minCode, String accessToken) {
		Optional<SchoolEntity> entOptional = schoolRepository.findById(minCode);
		if(entOptional.isPresent()) {
			School school = schoolTransformer.transformToDTO(entOptional.get());
			District dist = districtTransformer.transformToDTO(districtRepository.findById(school.getMinCode().substring(0, 3)));
			if(dist != null) {
				school.setDistrictName(dist.getDistrictName());
			}
			CommonSchool commonSchool = getCommonSchool(accessToken, school.getMinCode());
			adaptSchool(school, commonSchool);
			return school;
		}
		return null;
	}

	@Transactional(readOnly = true)
	public List<School> getSchoolsByParams(String schoolName, String minCode, String district, String accessToken) {
		String sName = !StringUtils.isBlank(schoolName) ? StringUtils.strip(schoolName,"*"):null;
		String sCode = !StringUtils.isBlank(minCode) ? StringUtils.strip(minCode,"*"):null;
		String sDist = !StringUtils.isBlank(district) ? StringUtils.strip(district,"*"):null;
		boolean sNameWc = StringUtils.endsWith(schoolName, "*");
		boolean sCodeWc = StringUtils.endsWith(minCode, "*");
		boolean sDistWc = StringUtils.endsWith(district, "*");
		TraxSchoolSearchCriteria searchCriteria = TraxSchoolSearchCriteria.builder()
				.district(sDist)
				.districtWildCard(sDistWc)
				.schoolName(sName)
				.schoolNameWildCard(sNameWc)
				.minCode(sCode)
				.minCodeWildCard(sCodeWc)
				.build();
		Specification<SchoolEntity> spec = new TraxSchoolSearchSpecification(searchCriteria);
		List<SchoolEntity> schoolEntities = schoolRepository.findAll(Specification.where(spec));
		List<School> schoolList = schoolTransformer.transformToDTO(schoolEntities);
    	schoolList.forEach(sL -> {
    		District dist = districtTransformer.transformToDTO(districtRepository.findById(sL.getMinCode().substring(0, 3)));
    		if (dist != null) {
				sL.setDistrictName(dist.getDistrictName());
			}
    		CommonSchool commonSchool = getCommonSchool(accessToken, sL.getMinCode());
    		adaptSchool(sL, commonSchool);
    	});
		sortSchools(schoolList);
		return schoolList;
	}

	private void sortSchools(List<School> result) {
		Arrays.sort(result.toArray());
	}

	@Transactional(readOnly = true)
	public boolean existsSchool(String minCode) {
		return schoolRepository.countTabSchools(minCode) > 0L;
	}

	public List<School> getSchoolsBySchoolCategory(String schoolCategoryCode, String accessToken) {
		List<School> result = new ArrayList<>();
		if(StringUtils.isBlank(schoolCategoryCode)) {
			return adaptSchools(getCommonSchools(accessToken));
		} else {
			List<CommonSchool> schools = getCommonSchools(accessToken);
			for (CommonSchool s : schools) {
				if (StringUtils.equalsIgnoreCase(schoolCategoryCode, s.getSchoolCategoryCode())) {
					result.add(adaptSchool(s));
				}
			}
		}
		return result;
	}

	public CommonSchool getCommonSchool(String accessToken, String mincode) {
    	if(StringUtils.isBlank(mincode)) {
    		return null;
		}
    	try {
			return webClient.get().uri(String.format(constants.getSchoolByMincodeSchoolApiUrl(), mincode))
					.headers(h -> {
						h.setBearerAuth(accessToken);
						h.set(EducGradTraxApiConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID());
					})
					.retrieve().bodyToMono(CommonSchool.class).block();
		} catch (WebClientResponseException e) {
			if(e.getStatusCode() == HttpStatus.NOT_FOUND){
				logger.warn(String.format("Common School not exists for Ministry Code: %s", mincode));
			} else {
				logger.error(String.format("Response error while calling school-api. Status was: %s", e.getStatusCode()));
			}
		} catch (Exception e) {
			logger.error(String.format("Error while calling school-api: %s", e.getMessage()));
		}
		return null;
	}

	public List<CommonSchool> getCommonSchools(String accessToken) {
		try {
			return webClient.get().uri(constants.getAllSchoolSchoolApiUrl())
					.headers(h -> {
						h.setBearerAuth(accessToken);
						h.set(EducGradTraxApiConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID());
					})
					.retrieve().bodyToMono(new ParameterizedTypeReference<List<CommonSchool>>() {
					}).block();
		} catch (Exception e) {
			logger.error("Common Schools API is not available {}", e.getCause().toString());
			return new ArrayList<>();
		}
	}

	private School adaptSchool(CommonSchool commonSchool) {
		School school = new School();
		school.setMinCode(commonSchool.getDistNo()+commonSchool.getSchlNo());
		school.setSchoolName(commonSchool.getSchoolName());
		school.setSchoolCategory(commonSchool.getSchoolCategoryCode());
		return school;
	}

	private List<School> adaptSchools(List<CommonSchool> commonSchools) {
		List<School> result = new ArrayList<>();
		for(CommonSchool sch: commonSchools) {
			result.add(adaptSchool(sch));
		}
		return result;
	}

	private void adaptSchool(School school, CommonSchool commonSchool) {
    	if(commonSchool == null) {
			return;
		}
    	if(school != null) {
    		school.setSchoolCategory(commonSchool.getSchoolCategoryCode());

    		if(StringUtils.isNotBlank(school.getCountryCode())) {
				GradCountry country = codeService.getSpecificCountryCode(school.getCountryCode());
				if(country != null) {
					school.setCountryName(country.getCountryName());
				}
			}
			if(StringUtils.isNotBlank(school.getProvCode())) {
				GradProvince province = codeService.getSpecificProvinceCode(school.getProvCode());
				if(province != null) {
					school.setProvinceName(province.getProvName());
				}
			}
		}
	}
}
