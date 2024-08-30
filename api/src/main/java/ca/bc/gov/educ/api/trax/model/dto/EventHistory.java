package ca.bc.gov.educ.api.trax.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EventHistory extends BaseModel {
    @NotNull(message = "id can not be null.")
    private UUID id;
    private Event event;
    private String eventHistoryUrl;
    @NotBlank(message = "acknowledge flag must be present and be either Y or N.")
    @Pattern(regexp = "^[Yy|Nn]$", message = "acknowledge flag must be either Y or N.")
    private String acknowledgeFlag;

    public void setAcknowledgeFlag(String acknowledgeFlag){
        this.acknowledgeFlag = acknowledgeFlag.toUpperCase();
    }
}
