package ca.bc.gov.educ.api.trax.model.transformer;

import ca.bc.gov.educ.api.trax.model.dto.District;
import ca.bc.gov.educ.api.trax.model.entity.DistrictEntity;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class DistrictTransformer {

    @Autowired
    ModelMapper modelMapper;

    public District transformToDTO (DistrictEntity districtEntity) {
        return modelMapper.map(districtEntity, District.class);
    }

    public District transformToDTO ( Optional<DistrictEntity> districtEntity ) {
        if (districtEntity.isPresent()) {
            DistrictEntity cae = districtEntity.get();
	        District district = modelMapper.map(cae, District.class);
	        return district;
        }
        return null;
    }

	public List<District> transformToDTO (Iterable<DistrictEntity> districtEntities ) {
        List<District> districtList = new ArrayList<>();

        for (DistrictEntity districtEntity : districtEntities) {
            District district = modelMapper.map(districtEntity, District.class);
            districtList.add(district);
        }

        return districtList;
    }

    public DistrictEntity transformToEntity(District district) {
        return modelMapper.map(district, DistrictEntity.class);
    }
}
