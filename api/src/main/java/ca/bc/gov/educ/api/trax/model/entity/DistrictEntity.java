package ca.bc.gov.educ.api.trax.model.entity;

import lombok.Data;
import org.hibernate.annotations.Immutable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Data
@Immutable
@Entity
@Table(name = "TAB_DISTRICT")
public class DistrictEntity {
   
	@Id
	@Column(name = "DISTNO", nullable = true)
    private String districtNumber;   

    @Column(name = "DIST_NAME", nullable = true)
    private String districtName;  
    
    @Column(name = "DIST_SHIP_SEQ", nullable = true)
    private String districtSeq;  

    @Column(name = "SCHL_ETP_SYS", nullable = true)
    private String schoolETPSystem;
    
    @Column(name = "SUPERINTENDENT", nullable = true)
    private String superIntendent;
    
    @Column(name = "DJDE_FLASH", nullable = true)
    private String djdeFlash;
    
    @Column(name = "ACTIVE_FLAG", nullable = true)
    private String activeFlag;
    
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

}
