package ca.bc.gov.educ.api.trax.model.transformer.institute;

import ca.bc.gov.educ.api.trax.model.dto.institute.District;
import ca.bc.gov.educ.api.trax.model.entity.institute.DistrictEntity;
import ca.bc.gov.educ.api.trax.model.entity.institute.SchoolDetailEntity;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component("InstituteDistrictTransformer")
public class DistrictTransformer {

    @Autowired
    ModelMapper modelMapper;

    public District transformToDTO (DistrictEntity districtEntity) {
        return modelMapper.map(districtEntity, District.class);
    }

    public District transformToDTO (Optional<DistrictEntity> districtEntity ) {
        if (districtEntity.isPresent()) {
            DistrictEntity de = districtEntity.get();
	       return modelMapper.map(de, District.class);
        }
        return null;
    }

	public List<District> transformToDTO (Iterable<DistrictEntity> districtEntities ) {
        return modelMapper.map(districtEntities, new TypeToken<List<District>>(){}.getType());
    }

    public DistrictEntity transformToEntity(District district) {
        return modelMapper.map(district, DistrictEntity.class);
    }

    public List<DistrictEntity> transformToEntity (List<District> districts ) {
        if (districts == null) return null;
        return districts
                .stream()
                .map(district -> modelMapper.map(district, DistrictEntity.class))
                .collect(Collectors.toList());
    }
}
