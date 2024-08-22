package ca.bc.gov.educ.api.trax.model.transformer;

import ca.bc.gov.educ.api.trax.model.dto.EventHistory;
import ca.bc.gov.educ.api.trax.model.entity.EventHistoryEntity;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EventHistoryTransformer {

    private final ModelMapper modelMapper;

    @Autowired
    public EventHistoryTransformer(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public EventHistory toDTO(EventHistoryEntity eventHistoryEntity) {
        return modelMapper.map(eventHistoryEntity, EventHistory.class);
    }

    public EventHistoryEntity toEntity(EventHistory eventHistory) {
        return modelMapper.map(eventHistory, EventHistoryEntity.class);
    }
}
