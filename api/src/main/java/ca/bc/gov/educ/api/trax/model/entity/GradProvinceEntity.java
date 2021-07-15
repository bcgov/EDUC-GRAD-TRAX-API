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
@Table(name = "TAB_PROV")
public class GradProvinceEntity {
   
	@Id
	@Column(name = "PROV_CODE", nullable = false)
    private String provCode; 
	
	@Column(name = "PROV_NAME", nullable = true)
    private String provName; 
	
	@Column(name = "CNTRY_CODE", nullable = true)
    private String countryCode;
}