package ca.bc.gov.educ.api.trax.model.transformer.institute;

import ca.bc.gov.educ.api.trax.model.dto.institute.SchoolCategoryCode;
import ca.bc.gov.educ.api.trax.model.dto.institute.SchoolFundingGroupCode;
import ca.bc.gov.educ.api.trax.model.entity.institute.SchoolCategoryCodeEntity;
import ca.bc.gov.educ.api.trax.model.entity.institute.SchoolFundingGroupCodeEntity;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component("SchoolFundingGroupCodeTransformer")
public class SchoolFundingGroupCodeTransformer {

    @Autowired
    ModelMapper modelMapper;

    public SchoolFundingGroupCode transformToDTO (SchoolFundingGroupCodeEntity schoolFundingGroupCodeEntity) {
        return modelMapper.map(schoolFundingGroupCodeEntity, SchoolFundingGroupCode.class);
    }

    public SchoolFundingGroupCode transformToDTO (Optional<SchoolFundingGroupCodeEntity> schoolFundingGroupCodeEntity ) {
        if (schoolFundingGroupCodeEntity.isPresent()) {
            SchoolFundingGroupCodeEntity sfgce = schoolFundingGroupCodeEntity.get();
	       return modelMapper.map(sfgce, SchoolFundingGroupCode.class);
        }
        return null;
    }

	public List<SchoolFundingGroupCode> transformToDTO (Iterable<SchoolFundingGroupCodeEntity> schoolFundingGroupCodeEntities ) {
        return modelMapper.map(schoolFundingGroupCodeEntities, new TypeToken<List<SchoolFundingGroupCode>>(){}.getType());
    }

    public SchoolFundingGroupCodeEntity transformToEntity(SchoolFundingGroupCode schoolFundingGroupCode) {
        return modelMapper.map(schoolFundingGroupCode, SchoolFundingGroupCodeEntity.class);
    }

    public List<SchoolFundingGroupCodeEntity> transformToEntity (Iterable<SchoolFundingGroupCode> schoolFundingGroupCodes ) {
        return modelMapper.map(schoolFundingGroupCodes, new TypeToken<List<SchoolFundingGroupCodeEntity>>(){}.getType());
    }
}
