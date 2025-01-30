package ca.bc.gov.educ.api.trax.model.dto.institute;

import ca.bc.gov.educ.api.trax.model.dto.BaseModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

@Data
@EqualsAndHashCode(callSuper = true)
@Component("districtContact")
public class DistrictContact extends BaseModel {

    private String districtContactId;
    private String districtId;
    private String districtContactTypeCode;
    private String phoneNumber;
    private String jobTitle;
    private String phoneExtension;
    private String alternatePhoneNumber;
    private String alternatePhoneExtension;
    private String email;
    private String firstName;
    private String lastName;
    private String effectiveDate;
    private String expiryDate;
}
