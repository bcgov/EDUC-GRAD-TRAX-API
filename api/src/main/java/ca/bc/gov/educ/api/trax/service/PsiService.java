package ca.bc.gov.educ.api.trax.service;


import ca.bc.gov.educ.api.trax.model.dto.GradCountry;
import ca.bc.gov.educ.api.trax.model.dto.GradProvince;
import ca.bc.gov.educ.api.trax.model.dto.Psi;
import ca.bc.gov.educ.api.trax.model.dto.StudentPsi;
import ca.bc.gov.educ.api.trax.model.entity.PsiEntity;
import ca.bc.gov.educ.api.trax.model.transformer.PsiTransformer;
import ca.bc.gov.educ.api.trax.repository.PsiRepository;
import ca.bc.gov.educ.api.trax.repository.StudentPsiRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class PsiService {

    @Autowired
    private PsiRepository psiRepository;

    @Autowired
    private PsiTransformer psiTransformer;

    @Autowired
    private StudentPsiRepository studentPsiRepository;
    
    @Autowired
    CodeService codeService;

    @SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(PsiService.class);

     /**
     * Get all Schools in PSI DTO
     */
    public List<Psi> getPSIList() {
        return psiTransformer.transformToDTO(psiRepository.findAll());
    }

	public Psi getPSIDetails(String psiCode) {
		Optional<PsiEntity> entOptional = psiRepository.findById(psiCode);
		if(entOptional.isPresent()) {
            Psi psi = psiTransformer.transformToDTO(entOptional.get());
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
            return psi;
		}
		return null;
	}

	public List<Psi> getPSIByParams(String psiName, String psiCode, String cslCode, String transmissionMode,String openFlag) {
        String pCode = psiCode != null ?StringUtils.strip(psiCode.toUpperCase(Locale.ROOT), "*"):null;
        String pName = psiName != null ?StringUtils.strip(psiName.toUpperCase(Locale.ROOT), "*"):null;
        String pcCode = cslCode != null ?StringUtils.strip(cslCode.toUpperCase(Locale.ROOT), "*"):null;
        String ptMode = transmissionMode != null ?StringUtils.strip(transmissionMode.toUpperCase(Locale.ROOT), "*"):null;
        return psiTransformer.transformToDTO(psiRepository.findPSIs(pCode,pName,pcCode,ptMode,openFlag));
	}

    public List<StudentPsi> getStudentPSIDetails(String transmissionMode, String psiYear, String psiCode) {
        String psiCodeProvided = "Yes";
        List<String> psiList = List.of(psiCode.split(",", -1));
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
