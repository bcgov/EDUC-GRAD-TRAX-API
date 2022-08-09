package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.model.dto.District;
import ca.bc.gov.educ.api.trax.model.entity.DistrictEntity;
import ca.bc.gov.educ.api.trax.model.transformer.DistrictTransformer;
import ca.bc.gov.educ.api.trax.repository.DistrictRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DistrictService {

    @Autowired DistrictRepository districtRepository;
    @Autowired DistrictTransformer districtTransformer;

    @SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(DistrictService.class);

	public District getDistrictDetails(String districtCode) {
		Optional<DistrictEntity> distEntityOptional =  districtRepository.findById(districtCode);
		return distEntityOptional.map(districtEntity -> districtTransformer.transformToDTO(districtEntity)).orElse(null);
	}

}
