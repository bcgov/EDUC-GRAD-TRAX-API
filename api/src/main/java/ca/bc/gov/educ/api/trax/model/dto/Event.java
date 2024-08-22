package ca.bc.gov.educ.api.trax.model.dto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Event extends BaseModel {
    private UUID replicationEventId;
    private UUID eventId;
    private String eventPayload;
    private String eventStatus;
    private String eventType;
    private String eventOutcome;
    private String activityCode;
}
