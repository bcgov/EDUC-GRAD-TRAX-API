package ca.bc.gov.educ.api.trax.config;

import ca.bc.gov.educ.api.trax.model.dto.District;
import ca.bc.gov.educ.api.trax.model.dto.Psi;
import ca.bc.gov.educ.api.trax.model.dto.School;
import ca.bc.gov.educ.api.trax.model.entity.DistrictEntity;
import ca.bc.gov.educ.api.trax.model.entity.PsiEntity;
import ca.bc.gov.educ.api.trax.model.entity.SchoolEntity;
import ca.bc.gov.educ.api.trax.util.EducGradTraxApiConstants;
import ca.bc.gov.educ.api.trax.util.LogHelper;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@Configuration
@Profile("!test")
public class GradTraxConfig {

	LogHelper logHelper;
	EducGradTraxApiConstants constants;

	@Autowired
	public GradTraxConfig(LogHelper logHelper, EducGradTraxApiConstants constants) {
		this.logHelper = logHelper;
		this.constants = constants;
	}

	@Bean
	public ModelMapper modelMapper() {

		ModelMapper modelMapper = new ModelMapper();
		modelMapper.typeMap(SchoolEntity.class, School.class);
		modelMapper.typeMap(School.class, SchoolEntity.class);

		modelMapper.typeMap(DistrictEntity.class, District.class);
		modelMapper.typeMap(District.class, DistrictEntity.class);

		modelMapper.typeMap(PsiEntity.class, Psi.class);
		modelMapper.typeMap(Psi.class, PsiEntity.class);
		return modelMapper;
	}

}
