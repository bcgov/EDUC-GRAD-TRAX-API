package ca.bc.gov.educ.api.trax.model.dto.institute;

import ca.bc.gov.educ.api.trax.model.dto.BaseModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

@Data
@EqualsAndHashCode(callSuper = true)
@Component("SchoolMove")
public class SchoolMove extends BaseModel {

    private String schoolMoveId;
    private String toSchoolId;
    private String fromSchoolId;
    private String moveDate;

}