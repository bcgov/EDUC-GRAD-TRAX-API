package ca.bc.gov.educ.api.trax.model.transformer;

import ca.bc.gov.educ.api.trax.model.dto.TranscriptStudentDemog;
import ca.bc.gov.educ.api.trax.model.entity.TranscriptStudentDemogEntity;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class TranscriptStudentDemogTransformer {

    @Autowired
    ModelMapper modelMapper;

    public TranscriptStudentDemog transformToDTO (TranscriptStudentDemogEntity transcriptStudentDemogEntity) {
        return modelMapper.map(transcriptStudentDemogEntity, TranscriptStudentDemog.class);
    }

    public TranscriptStudentDemog transformToDTO ( Optional<TranscriptStudentDemogEntity> traxUpdateInGradOptional ) {
        if (traxUpdateInGradOptional.isPresent()) {
            TranscriptStudentDemogEntity transcriptStudentDemogEntity = traxUpdateInGradOptional.get();
	        return transformToDTO(transcriptStudentDemogEntity);
        }
        return null;
    }

	public List<TranscriptStudentDemog> transformToDTO (Iterable<TranscriptStudentDemogEntity> transcriptStudentDemogEntities ) {
        List<TranscriptStudentDemog> transcriptStudentDemogList = new ArrayList<>();

        for (TranscriptStudentDemogEntity transcriptStudentDemogEntity : transcriptStudentDemogEntities) {
            TranscriptStudentDemog traxUpdateInGrad = transformToDTO(transcriptStudentDemogEntity);
            transcriptStudentDemogList.add(traxUpdateInGrad);
        }

        return transcriptStudentDemogList;
    }

    public TranscriptStudentDemogEntity transformToEntity(TranscriptStudentDemog transcriptStudentDemog) {
        return modelMapper.map(transcriptStudentDemog, TranscriptStudentDemogEntity.class);
    }
}
