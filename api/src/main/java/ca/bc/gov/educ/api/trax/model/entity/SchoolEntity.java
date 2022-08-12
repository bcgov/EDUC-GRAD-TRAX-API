package ca.bc.gov.educ.api.trax.model.entity;

import lombok.Data;
import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Immutable
@Entity
@Table(name = "TAB_SCHOOL")
public class SchoolEntity {
   
	@Id
	@Column(name = "MINCODE", nullable = true)
    private String minCode;   

    @Column(name = "SCHL_NAME", nullable = true)
    private String schoolName;  
    
    @Column(name = "XCRIPT_ELIG", nullable = true)
    private String transcriptEligibility;  
    
    @Column(name = "DOGWOOD_ELIG", nullable = true)
    private String certificateEligibility;  
    
    @Column(name = "SCHL_IND_TYPE", nullable = true)
    private String independentDesignation;
    
    @Column(name = "MAILER_TYPE", nullable = true)
    private String mailerType;
    
    @Column(name = "ADDRESS1", nullable = true)
    private String address1;
    
    @Column(name = "ADDRESS2", nullable = true)
    private String address2;
    
    @Column(name = "CITY", nullable = true)
    private String city;
    
    @Column(name = "PROV_CODE", nullable = true)
    private String provCode;
    
    @Column(name = "CNTRY_CODE", nullable = true)
    private String countryCode;
    
    @Column(name = "POSTAL", nullable = true)
    private String postal;
    
    @Column(name = "SCHL_IND_AFFIL", nullable = true)
    private String independentAffiliation;
    
    @Column(name = "OPEN_FLAG", nullable = true)
    private String openFlag;  
    
    @Column(name = "SIGNATURE_DISTNO", nullable = true)
    private String signatureDistrict;
    
    @Column(name = "NEW_MINCODE", nullable = true)
    private String newMinCode;
    
    @Column(name = "SCHL_ORG", nullable = true)
    private String schoolOrg;

    @Column(name = "APPEND_TRANS", nullable = true)
    private String appendTrans;
    
    @Column(name = "MINISTRY_CONTACT", nullable = true)
    private String ministryContact;
    
    @Column(name = "PRINC_NAME", nullable = true)
    private String principalName;

    @Column(name = "PHONE", nullable = true)
    private String schoolPhone;
    
    @Column(name = "SCHL_FAX", nullable = true)
    private String schoolFax;
    
    @Column(name = "EMAIL", nullable = true)
    private String schoolEmail;
    
}
