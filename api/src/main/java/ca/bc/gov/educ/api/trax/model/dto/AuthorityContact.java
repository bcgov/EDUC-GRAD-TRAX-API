package ca.bc.gov.educ.api.trax.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AuthorityContact extends BaseModel {

    private String authorityContactId;

    private String independentAuthorityId;

    private String authorityContactTypeCode;

    private String phoneNumber;

    private String phoneExtension;

    private String alternatePhoneNumber;

    private String alternatePhoneExtension;

    private String email;

    private String firstName;

    private String lastName;

    private String effectiveDate;

    private String expiryDate;

}
