package ca.bc.gov.educ.api.trax.model.transformer.institute;

import ca.bc.gov.educ.api.trax.model.dto.institute.SchoolDetail;
import ca.bc.gov.educ.api.trax.model.entity.institute.SchoolDetailEntity;
import ca.bc.gov.educ.api.trax.model.entity.institute.SchoolEntity;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component("SchoolDetailTransformer")
public class SchoolDetailTransformer {

    ModelMapper modelMapper;

    @Autowired
    public SchoolDetailTransformer(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public SchoolEntity transformToSchoolEntity(final SchoolDetail schoolDetail) {
        return modelMapper.map(schoolDetail, SchoolEntity.class);
    }

    public SchoolDetail transformToDTO (SchoolDetailEntity schoolDetailEntity) {
        return modelMapper.map(schoolDetailEntity, SchoolDetail.class);
    }

    public SchoolDetail transformToDTO (Optional<SchoolDetailEntity> schoolDetailEntity) {
        if (schoolDetailEntity.isPresent()) {
            SchoolDetailEntity sde = schoolDetailEntity.get();
	       return modelMapper.map(sde, SchoolDetail.class);
        }
        return null;
    }

	public List<SchoolDetail> transformToDTO (Iterable<SchoolDetailEntity> schoolDetailEntities ) {
        return modelMapper.map(schoolDetailEntities, new TypeToken<List<SchoolDetail>>(){}.getType());
    }

    public SchoolDetailEntity transformToEntity(SchoolDetail schoolDetail) {
        return modelMapper.map(schoolDetail, SchoolDetailEntity.class);
    }

    public List<SchoolDetailEntity> transformToEntity (List<SchoolDetail> schoolDetails ) {
        if (schoolDetails == null) return null;
        return schoolDetails
                .stream()
                .map(sd -> modelMapper.map(sd, SchoolDetailEntity.class))
                .collect(Collectors.toList());
    }
}
