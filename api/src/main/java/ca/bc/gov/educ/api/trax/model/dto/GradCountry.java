package ca.bc.gov.educ.api.trax.model.dto;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class GradCountry {

	private String countryCode; 	
	private String countryName;	
	private String srbCountryCode;
	
	@Override
	public String toString() {
		return "GradCountry [countryCode=" + countryCode + ", countryName=" + countryName + ", srbCountryCode="
				+ srbCountryCode + "]";
	}
	
}
