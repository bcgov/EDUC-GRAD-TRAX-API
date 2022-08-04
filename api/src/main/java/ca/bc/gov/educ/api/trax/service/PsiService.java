package ca.bc.gov.educ.api.trax.service;


import ca.bc.gov.educ.api.trax.model.dto.GradCountry;
import ca.bc.gov.educ.api.trax.model.dto.GradProvince;
import ca.bc.gov.educ.api.trax.model.dto.Psi;
import ca.bc.gov.educ.api.trax.model.dto.StudentPsi;
import ca.bc.gov.educ.api.trax.model.entity.PsiEntity;
import ca.bc.gov.educ.api.trax.model.transformer.PsiTransformer;
import ca.bc.gov.educ.api.trax.repository.PsiCriteriaQueryRepository;
import ca.bc.gov.educ.api.trax.repository.PsiRepository;
import ca.bc.gov.educ.api.trax.repository.StudentPsiRepository;
import ca.bc.gov.educ.api.trax.util.criteria.CriteriaHelper;
import ca.bc.gov.educ.api.trax.util.criteria.GradCriteria.OperationEnum;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PsiService {

    @Autowired
    private PsiRepository psiRepository;

    @Autowired
    private PsiTransformer psiTransformer;

    @Autowired
    private StudentPsiRepository studentPsiRepository;
    
    @Autowired
    private PsiCriteriaQueryRepository  psiCriteriaQueryRepository;
    
    @Autowired
    CodeService codeService;

    @SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(PsiService.class);


    private static final String PSI_NAME = "psiName";
     /**
     * Get all Schools in PSI DTO
     */
    public List<Psi> getPSIList() {
        return psiTransformer.transformToDTO(psiRepository.findAll());
    }

	public Psi getPSIDetails(String psiCode) {
		Psi psi =  psiTransformer.transformToDTO(psiRepository.findById(psiCode));
		if(psi != null) {
			if(StringUtils.isNotBlank(psi.getCountryCode())) {
			    GradCountry country = codeService.getSpecificCountryCode(psi.getCountryCode());
		        if(country != null) {
		        	psi.setCountryName(country.getCountryName());
				}
			}
			if(StringUtils.isNotBlank(psi.getProvinceCode())) {
                GradProvince province = codeService.getSpecificProvinceCode(psi.getProvinceCode());
		        if(province != null) {
		        	psi.setProvinceName(province.getProvName());
				}
			}
		}
		return psi;
	}

	public List<Psi> getPSIByParams(String psiName, String psiCode, String cslCode, String transmissionMode,String openFlag,String psiGrouping) {
		CriteriaHelper criteria = new CriteriaHelper();
        getSearchCriteria("psiCode", psiCode,"psiCode", criteria);
        getSearchCriteria(PSI_NAME, psiName,PSI_NAME, criteria);
        getSearchCriteria("cslCode", cslCode,"cslCode", criteria);
        getSearchCriteria("transmissionMode",transmissionMode,"transmissionMode", criteria);
        getSearchCriteria("openFlag",openFlag,"openFlag", criteria);
        getSearchCriteria("psiGrouping",psiGrouping,"psiGrouping", criteria);
        return psiTransformer.transformToDTO(psiCriteriaQueryRepository.findByCriteria(criteria, PsiEntity.class));
	}
	
	public CriteriaHelper getSearchCriteria(String roolElement, String value, String parameterType, CriteriaHelper criteria) {
        if(parameterType.equalsIgnoreCase(PSI_NAME)) {
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

    public List<StudentPsi> getStudentPSIDetails(String transmissionMode, String psiYear, String psiCode) {
        String psiCodeProvided = "Yes";
        List<String> psiList = List.of(psiCode);
        if(psiCode.equalsIgnoreCase("all")) {
            psiCodeProvided = null;
        }
        List<StudentPsi> studentPsiList = new ArrayList<>();
        List<Object[]> results = studentPsiRepository.findStudentsUsingPSI(transmissionMode,psiYear,psiCodeProvided,psiList);
        results.forEach(result -> {
            StudentPsi studPsi = new StudentPsi();
            String pen = (String) result[0];
            String pCode = (String) result[1];
            String pYear = (String) result[2];
            studPsi.setPsiCode(pCode);
            studPsi.setPen(pen);
            studPsi.setPsiYear(pYear);
            studentPsiList.add(studPsi);
        });
        return studentPsiList;
    }
}
