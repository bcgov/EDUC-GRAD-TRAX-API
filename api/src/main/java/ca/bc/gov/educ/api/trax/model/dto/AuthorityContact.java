package ca.bc.gov.educ.api.trax.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthorityContact extends BaseModel implements Serializable {

    /**
     * The constant serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

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
