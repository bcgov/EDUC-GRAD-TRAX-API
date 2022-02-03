package ca.bc.gov.educ.api.trax.model.transformer;

import ca.bc.gov.educ.api.trax.model.dto.TraxUpdateInGrad;
import ca.bc.gov.educ.api.trax.model.entity.TraxUpdateInGradEntity;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class TraxUpdateInGradTransformer {

    @Autowired
    ModelMapper modelMapper;

    public TraxUpdateInGrad transformToDTO (TraxUpdateInGradEntity traxUpdateInGradEntity) {
        TraxUpdateInGrad traxUpdateInGrad = modelMapper.map(traxUpdateInGradEntity, TraxUpdateInGrad.class);
        traxUpdateInGrad.setUpdateType(traxUpdateInGradEntity.getUpdateType() != null? traxUpdateInGradEntity.getUpdateType().trim() : null);
        return traxUpdateInGrad;
    }

    public TraxUpdateInGrad transformToDTO ( Optional<TraxUpdateInGradEntity> traxUpdateInGradOptional ) {
        if (traxUpdateInGradOptional.isPresent()) {
            TraxUpdateInGradEntity traxUpdateInGradEntity = traxUpdateInGradOptional.get();
	        return transformToDTO(traxUpdateInGradEntity);
        }
        return null;
    }

	public List<TraxUpdateInGrad> transformToDTO (Iterable<TraxUpdateInGradEntity> traxUpdateInGradEntities ) {
        List<TraxUpdateInGrad> traxUpdateInGradList = new ArrayList<>();

        for (TraxUpdateInGradEntity traxUpdateInGradEntity : traxUpdateInGradEntities) {
            TraxUpdateInGrad traxUpdateInGrad = transformToDTO(traxUpdateInGradEntity);
            traxUpdateInGradList.add(traxUpdateInGrad);
        }

        return traxUpdateInGradList;
    }

    public TraxUpdateInGradEntity transformToEntity(TraxUpdateInGrad traxUpdateInGrad) {
        return modelMapper.map(traxUpdateInGrad, TraxUpdateInGradEntity.class);
    }
}
