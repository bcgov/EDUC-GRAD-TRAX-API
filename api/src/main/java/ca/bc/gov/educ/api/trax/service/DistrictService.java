package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.model.dto.CommonSchool;
import ca.bc.gov.educ.api.trax.model.dto.District;
import ca.bc.gov.educ.api.trax.model.entity.DistrictEntity;
import ca.bc.gov.educ.api.trax.model.transformer.DistrictTransformer;
import ca.bc.gov.educ.api.trax.repository.DistrictRepository;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class DistrictService {

    DistrictRepository districtRepository;
    DistrictTransformer districtTransformer;

	private SchoolService schoolService;

    @SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(DistrictService.class);

	public District getDistrictDetails(String districtCode) {
		Optional<DistrictEntity> distEntityOptional =  districtRepository.findById(districtCode);
		return distEntityOptional.map(districtEntity -> districtTransformer.transformToDTO(districtEntity)).orElse(null);
	}

	public District getDistrictsByDistrictCodeAndActiveFlag(String districtCode) {
		Optional<DistrictEntity> distEntityOptional =  districtRepository.findByDistrictNumberAndActiveFlag(districtCode, "Y");
		return distEntityOptional.map(districtEntity -> districtTransformer.transformToDTO(districtEntity)).orElse(null);
	}

	public List<District> getDistrictsByActiveFlag(String activeFlag) {
		return districtTransformer.transformToDTO(districtRepository.findByActiveFlag(activeFlag));
	}

	public List<District> getDistrictBySchoolCategory(String schoolCategoryCode) {
		List<District> result = new ArrayList<>();
		if(StringUtils.isBlank(schoolCategoryCode)) {
			return getDistrictsByActiveFlag("Y");
		} else {
			List<String> controlList = new ArrayList<>();
			List<CommonSchool> schools = schoolService.getCommonSchools();
			for (CommonSchool s : schools) {
				if (StringUtils.equalsIgnoreCase(schoolCategoryCode, s.getSchoolCategoryCode()) && !controlList.contains(s.getDistNo())) {
					controlList.add(s.getDistNo());
					District district = getDistrictsByDistrictCodeAndActiveFlag(s.getDistNo());
					if (district != null) {
						result.add(district);
					}
				}
			}
		}
		return result;
	}

}
