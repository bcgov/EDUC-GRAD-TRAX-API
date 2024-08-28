package ca.bc.gov.educ.api.trax.mapper;

import ca.bc.gov.educ.api.trax.model.dto.Event;
import ca.bc.gov.educ.api.trax.model.entity.EventEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {UUIDMapper.class})
public interface EventMapper {

    EventMapper mapper = Mappers.getMapper(EventMapper.class);

    @Mapping(target = "eventPayload", ignore = true)
    Event toStructure(EventEntity eventEntity);

    EventEntity toEntity(Event event);

}
