package ca.bc.gov.educ.api.trax.model.transformer.institute;

import ca.bc.gov.educ.api.trax.model.dto.institute.SchoolCategoryCode;
import ca.bc.gov.educ.api.trax.model.entity.institute.SchoolCategoryCodeEntity;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component("SchoolCategoryCodeTransformer")
public class SchoolCategoryCodeTransformer {

    @Autowired
    ModelMapper modelMapper;

    public SchoolCategoryCode transformToDTO (SchoolCategoryCodeEntity schoolCategoryCodeEntity) {
        return modelMapper.map(schoolCategoryCodeEntity, SchoolCategoryCode.class);
    }

    public SchoolCategoryCode transformToDTO (Optional<SchoolCategoryCodeEntity> schoolCategoryCodeEntity ) {
        if (schoolCategoryCodeEntity.isPresent()) {
            SchoolCategoryCodeEntity scce = schoolCategoryCodeEntity.get();
	       return modelMapper.map(scce, SchoolCategoryCode.class);
        }
        return null;
    }

	public List<SchoolCategoryCode> transformToDTO (Iterable<SchoolCategoryCodeEntity> schoolCategoryCodeEntities ) {
        return modelMapper.map(schoolCategoryCodeEntities, new TypeToken<List<SchoolCategoryCode>>(){}.getType());
    }

    public SchoolCategoryCodeEntity transformToEntity(SchoolCategoryCode schoolCategoryCode) {
        return modelMapper.map(schoolCategoryCode, SchoolCategoryCodeEntity.class);
    }

    public List<SchoolCategoryCodeEntity> transformToEntity (List<SchoolCategoryCode> schoolCategoryCodes ) {
        if (schoolCategoryCodes == null) return null;
        return schoolCategoryCodes
                .stream()
                .map(scc -> modelMapper.map(scc, SchoolCategoryCodeEntity.class))
                .collect(Collectors.toList());
    }
}
