package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.model.dto.*;
import ca.bc.gov.educ.api.trax.model.entity.SchoolEntity;
import ca.bc.gov.educ.api.trax.model.transformer.DistrictTransformer;
import ca.bc.gov.educ.api.trax.model.transformer.SchoolTransformer;
import ca.bc.gov.educ.api.trax.repository.DistrictRepository;
import ca.bc.gov.educ.api.trax.repository.SchoolRepository;
import ca.bc.gov.educ.api.trax.util.EducGradTraxApiConstants;
import ca.bc.gov.educ.api.trax.util.ThreadLocalStateUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

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
	private static Logger logger = LoggerFactory.getLogger(SchoolService.class);

     /**
     * Get all Schools in School DTO
     *
     * @return List of Schools
     */
    public List<School> getSchoolList(String accessToken) {
        List<School> schoolList  = schoolTransformer.transformToDTO(schoolRepository.findAll());  
    	schoolList.forEach(sL -> {
    		District dist = districtTransformer.transformToDTO(districtRepository.findById(sL.getMinCode().substring(0, 3)));
    		if (dist != null) {
				sL.setDistrictName(dist.getDistrictName());
			}
			//CommonSchool commonSchool = getCommonSchool(accessToken, sL.getMinCode());
			//adaptSchool(sL, commonSchool);
    	});
        return schoolList;
    }

	public School getSchoolDetails(String minCode, String accessToken) {
		Optional<SchoolEntity> entOptional = schoolRepository.findById(minCode);
		if(entOptional.isPresent()) {
			School school = schoolTransformer.transformToDTO(entOptional.get());
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
			CommonSchool commonSchool = getCommonSchool(accessToken, minCode);
			adaptSchool(school, commonSchool);
			return school;
		}
		return null;
	}

	public List<School> getSchoolsByParams(String schoolName, String minCode, String district, String authorityNumber, String accessToken) {
		String sName = !StringUtils.isBlank(schoolName) ? StringUtils.strip(schoolName.toUpperCase(Locale.ROOT),"*"):null;
		String sCode = !StringUtils.isBlank(minCode) ? StringUtils.strip(minCode,"*"):null;
		String sDist = !StringUtils.isBlank(district) ? StringUtils.strip(district,"*"):null;
		String sAuth = !StringUtils.isBlank(authorityNumber) ? StringUtils.strip(authorityNumber,"*"):null;
		List<School> schoolList = schoolTransformer.transformToDTO(schoolRepository.findSchools(sName, sCode, sDist));
    	schoolList.forEach(sL -> {
    		District dist = districtTransformer.transformToDTO(districtRepository.findById(sL.getMinCode().substring(0, 3)));
    		if (dist != null) {
				sL.setDistrictName(dist.getDistrictName());
			}
    		CommonSchool commonSchool = getCommonSchool(accessToken, minCode);
    		adaptSchool(sL, commonSchool);
    	});
		List<School> result = filterByAuthorityNumber(schoolList, sAuth);
		sortSchools(result);
		return result;
	}

	private void sortSchools(List<School> result) {
		Arrays.sort(result.toArray());
	}

	public boolean existsSchool(String minCode, String accessToken) {
		return schoolRepository.countTabSchools(minCode) > 0L && getCommonSchool(accessToken, minCode) != null;
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
		} catch (Exception e) {
			logger.warn("Common School not exists for Ministry Code: " + mincode);
    		return null;
		}
	}

	private List<School> filterByAuthorityNumber(List<School> schools, String authorityNumber) {
    	if(StringUtils.isBlank(authorityNumber)) {
    		return schools;
		}
		List<School> result = new ArrayList<>();
		for (School sL : schools) {
			String sLAuthorityNumber = sL.getAuthorityNumber();
			if (!StringUtils.isBlank(sLAuthorityNumber) && StringUtils.startsWith(sLAuthorityNumber, authorityNumber)) {
				result.add(sL);
			}
		}
		return result;
	}

	private void adaptSchool(School school, CommonSchool commonSchool) {
    	if(commonSchool == null) {
			return;
		}
    	if(school != null) {
    		school.setDistrictNumber(commonSchool.getDistNo());
    		school.setSchoolType(commonSchool.getSchoolTypeCode());
    		school.setReportingFlag(commonSchool.getClosedDate() != null ? "N" : "Y");
    		school.setOpenDate(commonSchool.getOpenedDate());
    		school.setClosedDate(commonSchool.getClosedDate());
    		school.setAuthorityNumber(commonSchool.getAuthNumber());
    		school.setSchoolCategory(commonSchool.getSchoolCategoryCode());
    		school.setPrincipalTitle(commonSchool.getPrTitleCode());
    		school.setPrincipalFirstName(commonSchool.getPrGivenName());
    		school.setPrincipalLastName(commonSchool.getPrSurname());
    		school.setPrincipalName(commonSchool.getPrSurname() + ", " + commonSchool.getPrGivenName() + " " + commonSchool.getPrMiddleName());

    		//overwrite trax school with common school values
			school.setSchoolName(commonSchool.getSchoolName());
			school.setAddress1(commonSchool.getScAddressLine1());
			school.setAddress2(commonSchool.getScAddressLine2());
			school.setCity(commonSchool.getScCity());
			school.setProvCode(commonSchool.getScProvinceCode());
			school.setPostal(commonSchool.getScPostalCode());
			school.setCountryCode(commonSchool.getScCountryCode());
			school.setSchoolPhone(commonSchool.getScPhoneNumber());
			school.setSchoolFax(commonSchool.getScFaxNumber());
			school.setSchoolEmail(commonSchool.getScEMailId());
		}
	}
}
