package ca.bc.gov.educ.api.trax.model.transformer.institute;

import ca.bc.gov.educ.api.trax.model.dto.institute.School;
import ca.bc.gov.educ.api.trax.model.dto.institute.SchoolDetail;
import ca.bc.gov.educ.api.trax.model.entity.institute.SchoolCategoryCodeEntity;
import ca.bc.gov.educ.api.trax.model.entity.institute.SchoolDetailEntity;
import ca.bc.gov.educ.api.trax.model.entity.institute.SchoolEntity;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component("InstituteSchoolTransformer")
public class SchoolTransformer {

    @Autowired
    ModelMapper modelMapper;

    public School transformToDTO (SchoolEntity schoolEntity) {
        return modelMapper.map(schoolEntity, School.class);
    }

    public School transformToDTO (Optional<SchoolEntity> schoolEntity ) {
        if (schoolEntity.isPresent()) {
            SchoolEntity se = schoolEntity.get();
	       return modelMapper.map(se, School.class);
        }
        return null;
    }

	public List<School> transformToDTO (Iterable<SchoolEntity> schoolEntities ) {
        return modelMapper.map(schoolEntities, new TypeToken<List<School>>(){}.getType());
    }

    public SchoolEntity transformToEntity(School school) {
        return modelMapper.map(school, SchoolEntity.class);
    }

    public List<SchoolEntity> transformToEntity (List<School> schools ) {
        if (schools == null) return null;
        return schools
                .stream()
                .map(school -> modelMapper.map(school, SchoolEntity.class))
                .collect(Collectors.toList());
    }
}
