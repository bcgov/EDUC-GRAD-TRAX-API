package ca.bc.gov.educ.api.trax.model.dto.institute;

import ca.bc.gov.educ.api.trax.model.dto.BaseModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

@Data
@EqualsAndHashCode(callSuper = true)
@Component("SchoolFundingGroup")
public class SchoolFundingGroup extends BaseModel {

    private String schoolFundingGroupID;
    private String schoolId;
    private String schoolGradeCode;
    private String schoolFundingGroupCode;

}
