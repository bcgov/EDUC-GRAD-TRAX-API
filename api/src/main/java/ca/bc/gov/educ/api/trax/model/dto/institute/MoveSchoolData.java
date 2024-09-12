package ca.bc.gov.educ.api.trax.model.dto.institute;

import ca.bc.gov.educ.api.trax.model.dto.BaseModel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MoveSchoolData extends BaseModel implements Serializable {

    /**
     * The constant serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    @NotNull(message = "toSchool cannot be null.")
    @Valid
    private School toSchool;

    @NotNull(message = "fromSchoolId cannot be null.")
    private String fromSchoolId;

    @NotNull(message = "moveDate cannot be null.")
    private String moveDate;
}
