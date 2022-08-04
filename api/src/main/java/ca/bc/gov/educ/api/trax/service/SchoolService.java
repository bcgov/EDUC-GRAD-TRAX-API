package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.model.dto.District;
import ca.bc.gov.educ.api.trax.model.dto.GradCountry;
import ca.bc.gov.educ.api.trax.model.dto.GradProvince;
import ca.bc.gov.educ.api.trax.model.dto.School;
import ca.bc.gov.educ.api.trax.model.transformer.DistrictTransformer;
import ca.bc.gov.educ.api.trax.model.transformer.SchoolTransformer;
import ca.bc.gov.educ.api.trax.repository.DistrictRepository;
import ca.bc.gov.educ.api.trax.repository.SchoolRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

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
    

    @SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(SchoolService.class);

     /**
     * Get all Schools in School DTO
     *
     * @return List of Schools
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
		String sName = schoolName != null? StringUtils.strip(schoolName.toUpperCase(Locale.ROOT),"*"):null;
		String sCode = minCode != null? StringUtils.strip(minCode,"*"):null;
		List<School> schoolList = schoolTransformer.transformToDTO(schoolRepository.findSchools(sName, sCode));
    	schoolList.forEach(sL -> {
    		District dist = districtTransformer.transformToDTO(districtRepository.findById(sL.getMinCode().substring(0, 3)));
    		if (dist != null) {
				sL.setDistrictName(dist.getDistrictName());
			}
    	});
    	return schoolList;
	}

	public boolean existsSchool(String minCode) {
		return schoolRepository.countTabSchools(minCode) > 0L;
	}
}
