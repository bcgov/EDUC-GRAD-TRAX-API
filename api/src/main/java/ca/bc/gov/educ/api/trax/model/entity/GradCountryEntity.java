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
@Table(name = "TAB_CNTRY")
public class GradCountryEntity {
   	
	@Id
	@Column(name = "CNTRY_CODE", nullable = false)
    private String countryCode; 
	
	@Column(name = "CNTRY_NAME", nullable = true)
    private String countryName; 
	
	@Column(name = "SRB_CNTRY_CODE", nullable = true)
    private String srbCountryCode; 	
		
}