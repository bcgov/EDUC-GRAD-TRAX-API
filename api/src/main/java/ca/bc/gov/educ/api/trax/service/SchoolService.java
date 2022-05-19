package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.model.dto.District;
import ca.bc.gov.educ.api.trax.model.dto.GradCountry;
import ca.bc.gov.educ.api.trax.model.dto.GradProvince;
import ca.bc.gov.educ.api.trax.model.dto.School;
import ca.bc.gov.educ.api.trax.model.entity.SchoolEntity;
import ca.bc.gov.educ.api.trax.model.transformer.DistrictTransformer;
import ca.bc.gov.educ.api.trax.model.transformer.SchoolTransformer;
import ca.bc.gov.educ.api.trax.repository.DistrictRepository;
import ca.bc.gov.educ.api.trax.repository.SchoolCriteriaQueryRepository;
import ca.bc.gov.educ.api.trax.repository.SchoolRepository;
import ca.bc.gov.educ.api.trax.util.criteria.CriteriaHelper;
import ca.bc.gov.educ.api.trax.util.criteria.GradCriteria.OperationEnum;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
    private SchoolCriteriaQueryRepository schoolCriteriaQueryRepository;
    
	@Autowired
	private CodeService codeService;
	
	private static final String SCHOOL_NAME= "schoolName";
    

    @SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(SchoolService.class);

     /**
     * Get all Schools in School DTO
     *
     * @return Course 
     * @throws Exception
     */
    public List<School> getSchoolList() {
        List<School> schoolList  = schoolTransformer.transformToDTO(schoolRepository.findAll());  
    	schoolList.forEach(sL -> {
    		District dist = districtTransformer.transformToDTO(districtRepository.findById(sL.getMinCode().substring(0, 3)));
    		if (dist != null) {
				sL.setDistrictName(dist.getDistrictName());
			}
    	});
        return schoolList;
    }

	public School getSchoolDetails(String minCode) {
		School school =  schoolTransformer.transformToDTO(schoolRepository.findById(minCode));
		if(school != null) {
			District dist = districtTransformer.transformToDTO(districtRepository.findById(school.getMinCode().substring(0, 3)));
			if(dist != null)
				school.setDistrictName(dist.getDistrictName());
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
		return school;
	}

	public List<School> getSchoolsByParams(String schoolName, String minCode) {    	
		CriteriaHelper criteria = new CriteriaHelper();
        getSearchCriteria("minCode", minCode, "minCode",criteria);
        getSearchCriteria("schoolName", schoolName,"schoolName" ,criteria);
		List<School> schoolList = schoolTransformer.transformToDTO(schoolCriteriaQueryRepository.findByCriteria(criteria, SchoolEntity.class));
    	schoolList.forEach(sL -> {
    		District dist = districtTransformer.transformToDTO(districtRepository.findById(sL.getMinCode().substring(0, 3)));
    		if (dist != null) {
				sL.setDistrictName(dist.getDistrictName());
			}
    	});
    	return schoolList;
	}
	public CriteriaHelper getSearchCriteria(String roolElement, String value, String parameterType, CriteriaHelper criteria) {
		if(parameterType.equalsIgnoreCase(SCHOOL_NAME)) {
        	if (StringUtils.isNotBlank(value)) {
                if (StringUtils.contains(value, "*")) {
                    criteria.add(roolElement, OperationEnum.LIKE, StringUtils.strip(value.toUpperCase(), "*"));
                } else {
                    criteria.add(roolElement, OperationEnum.EQUALS, value.toUpperCase());
                }
            }
		}else {	       
        	if (StringUtils.isNotBlank(value)) {
                if (StringUtils.contains(value, "*")) {
                    criteria.add(roolElement, OperationEnum.STARTS_WITH_IGNORE_CASE, StringUtils.strip(value.toUpperCase(), "*"));
                } else {
                    criteria.add(roolElement, OperationEnum.EQUALS, value.toUpperCase());
                }
            }	        	
	    }
        return criteria;
    }

	public boolean existsSchool(String minCode) {
		return schoolRepository.countTabSchools(minCode) > 0L;
	}
}
