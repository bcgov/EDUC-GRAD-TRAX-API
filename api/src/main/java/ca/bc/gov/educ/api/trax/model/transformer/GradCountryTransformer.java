package ca.bc.gov.educ.api.trax.model.transformer;

import ca.bc.gov.educ.api.trax.model.dto.GradCountry;
import ca.bc.gov.educ.api.trax.model.entity.GradCountryEntity;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Component
public class GradCountryTransformer {

    @Autowired
    ModelMapper modelMapper;

    public GradCountry transformToDTO (GradCountryEntity gradProgramEntity) {
    	GradCountry gradCountry = modelMapper.map(gradProgramEntity, GradCountry.class);
        return gradCountry;
    }

    public GradCountry transformToDTO ( Optional<GradCountryEntity> gradProgramEntity ) {
    	GradCountryEntity cae = new GradCountryEntity();
        if (gradProgramEntity.isPresent())
            cae = gradProgramEntity.get();

        GradCountry gradCountry = modelMapper.map(cae, GradCountry.class);
        return gradCountry;
    }

	public List<GradCountry> transformToDTO (List<GradCountryEntity> gradCountryEntities ) {
		List<GradCountry> gradCountryList = new ArrayList<GradCountry>();
        for (GradCountryEntity gradCountryEntity : gradCountryEntities) {
        	GradCountry gradCountry = new GradCountry();
        	gradCountry = modelMapper.map(gradCountryEntity, GradCountry.class);            
        	gradCountryList.add(gradCountry);
        }
        return gradCountryList;
    }

    public GradCountryEntity transformToEntity(GradCountry gradCountry) {
        GradCountryEntity gradCountryEntity = modelMapper.map(gradCountry, GradCountryEntity.class);
        return gradCountryEntity;
    }
}
