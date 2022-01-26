package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.model.dto.District;
import ca.bc.gov.educ.api.trax.model.dto.GradCountry;
import ca.bc.gov.educ.api.trax.model.dto.GradProvince;
import ca.bc.gov.educ.api.trax.model.dto.School;
import ca.bc.gov.educ.api.trax.model.entity.DistrictEntity;
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
import java.util.Optional;

@Service
public class DistrictService {

    @Autowired DistrictRepository districtRepository;
    @Autowired DistrictTransformer districtTransformer;

    @SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(DistrictService.class);

	public District getDistrictDetails(String districtCode) {
		Optional<DistrictEntity> distEntityOptional =  districtRepository.findById(districtCode);
		if(distEntityOptional.isPresent()) {
			return districtTransformer.transformToDTO(distEntityOptional.get());
		}
		return null;
	}

}
