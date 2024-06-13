package ca.bc.gov.educ.api.trax.model.dto.institute;

import ca.bc.gov.educ.api.trax.model.dto.BaseModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

@Data
@EqualsAndHashCode(callSuper = true)
@Component("NeighborhoodLearning")
public class NeighborhoodLearning extends BaseModel {

    private String neighborhoodLearningId;
    private String schoolId;
    private String neighborhoodLearningTypeCode;

}
