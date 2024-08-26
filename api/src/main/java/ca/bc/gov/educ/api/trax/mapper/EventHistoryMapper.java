package ca.bc.gov.educ.api.trax.mapper;

import ca.bc.gov.educ.api.trax.model.dto.EventHistory;
import ca.bc.gov.educ.api.trax.model.entity.EventHistoryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {EventMapper.class, UUIDMapper.class})
public interface EventHistoryMapper {

    EventHistoryMapper mapper = Mappers.getMapper(EventHistoryMapper.class);

    EventHistory toStructure(EventHistoryEntity eventHistoryEntity);

    @Mapping(target = "event.eventPayloadBytes", ignore = true)
    EventHistoryEntity toEntity(EventHistory eventHistory);

}
