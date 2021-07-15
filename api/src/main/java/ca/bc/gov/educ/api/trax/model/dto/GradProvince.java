package ca.bc.gov.educ.api.trax.model.dto;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class GradProvince {

	private String provCode;	
	private String provName;	
	private String countryCode;
	
	@Override
	public String toString() {
		return "GradProvince [provCode=" + provCode + ", provName=" + provName + ", countryCode=" + countryCode + "]";
	}				
}
