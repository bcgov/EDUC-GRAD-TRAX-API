package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.model.dto.*;
import ca.bc.gov.educ.api.trax.model.entity.SchoolEntity;
import ca.bc.gov.educ.api.trax.model.transformer.DistrictTransformer;
import ca.bc.gov.educ.api.trax.model.transformer.SchoolTransformer;
import ca.bc.gov.educ.api.trax.repository.DistrictRepository;
import ca.bc.gov.educ.api.trax.repository.SchoolRepository;
import ca.bc.gov.educ.api.trax.repository.TraxSchoolSearchCriteria;
import ca.bc.gov.educ.api.trax.repository.TraxSchoolSearchSpecification;
import ca.bc.gov.educ.api.trax.util.CommonSchoolCache;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class SchoolService {

    private SchoolRepository schoolRepository;
    private SchoolTransformer schoolTransformer;
    private DistrictRepository districtRepository;
    private DistrictTransformer districtTransformer;
	private CodeService codeService;
	private CommonSchoolCache commonSchoolCache;

	@Autowired
	public SchoolService(SchoolRepository schoolRepository, SchoolTransformer schoolTransformer, DistrictRepository districtRepository, DistrictTransformer districtTransformer, CodeService codeService, CommonSchoolCache commonSchoolCache) {
		this.schoolRepository = schoolRepository;
		this.schoolTransformer = schoolTransformer;
		this.districtRepository = districtRepository;
		this.districtTransformer = districtTransformer;
		this.codeService = codeService;
		this.commonSchoolCache = commonSchoolCache;
	}

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
			CommonSchool commonSchool = getCommonSchool(school.getMinCode());
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
    		CommonSchool commonSchool = getCommonSchool(sL.getMinCode());
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

	public List<School> getSchoolsBySchoolCategory(String schoolCategoryCode) {
		List<School> result = new ArrayList<>();
		if(StringUtils.isBlank(schoolCategoryCode)) {
			return adaptSchools(getCommonSchools());
		} else {
			List<CommonSchool> schools = getCommonSchools();
			for (CommonSchool s : schools) {
				if (StringUtils.equalsIgnoreCase(schoolCategoryCode, s.getSchoolCategoryCode())) {
					result.add(adaptSchool(s));
				}
			}
		}
		return result;
	}

	public CommonSchool getCommonSchool(String mincode) {
    	return (StringUtils.isBlank(mincode)) ? null : commonSchoolCache.getSchoolByMincode(mincode);
	}

	public List<CommonSchool> getCommonSchools() {
		return commonSchoolCache.getAllCommonSchools();
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
