package ca.bc.gov.educ.api.trax.model.dto;

import ca.bc.gov.educ.api.trax.model.entity.EventEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EventHistory extends BaseModel {
    private UUID id;
    private EventEntity event;
    private String acknowledgeFlag;
}
