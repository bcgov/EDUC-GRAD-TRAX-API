package ca.bc.gov.educ.api.trax.model.transformer;

import ca.bc.gov.educ.api.trax.model.dto.School;
import ca.bc.gov.educ.api.trax.model.entity.SchoolEntity;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class SchoolTransformer {

    @Autowired
    ModelMapper modelMapper;

    public School transformToDTO (SchoolEntity schoolEntity) {
        return modelMapper.map(schoolEntity, School.class);
    }

    public School transformToDTO ( Optional<SchoolEntity> schoolEntity ) {
        if (schoolEntity.isPresent()) {
            SchoolEntity cae = schoolEntity.get();
	       return modelMapper.map(cae, School.class);
        }
        return null;
    }

	public List<School> transformToDTO (Iterable<SchoolEntity> schoolEntities ) {
        List<School> schoolList = new ArrayList<>();

        for (SchoolEntity schoolEntity : schoolEntities) {
            School school = modelMapper.map(schoolEntity, School.class);
            schoolList.add(school);
        }

        return schoolList;
    }

    public SchoolEntity transformToEntity(School school) {
        return modelMapper.map(school, SchoolEntity.class);
    }
}
