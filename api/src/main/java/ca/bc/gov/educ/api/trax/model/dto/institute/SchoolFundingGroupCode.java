package ca.bc.gov.educ.api.trax.model.dto.institute;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component("SchoolFundingGroupCode")
public class SchoolFundingGroupCode {

    private String schoolFundingGroupCode;
    private String label;
    private String description;
    private String displayOrder;
    private String effectiveDate;
    private String expiryDate;

}
