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