package ca.bc.gov.educ.api.trax.model.dto;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Builder
@EqualsAndHashCode(callSuper = true)
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
