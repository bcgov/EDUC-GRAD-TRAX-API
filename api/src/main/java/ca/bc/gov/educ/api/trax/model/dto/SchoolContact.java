package ca.bc.gov.educ.api.trax.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SchoolContact extends BaseModel {

    private String schoolContactId;

    private String schoolId;

    private String schoolContactTypeCode;

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
