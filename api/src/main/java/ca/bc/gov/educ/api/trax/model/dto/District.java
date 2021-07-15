package ca.bc.gov.educ.api.trax.model.dto;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class District {

	private String districtNumber;
    private String districtName;    
    private String districtSeq;
    private String schoolETPSystem;
    private String superIntendent;    
    private String djdeFlash;    
    private String activeFlag;    
    private String address1;    
    private String address2;    
    private String city;    
    private String provCode;    
    private String countryCode;
    private String postal;
    
    
	@Override
	public String toString() {
		return "District [districtNumber=" + districtNumber + ", districtName=" + districtName + ", districtSeq="
				+ districtSeq + ", schoolETPSystem=" + schoolETPSystem + ", superIntendent=" + superIntendent
				+ ", djdeFlash=" + djdeFlash + ", activeFlag=" + activeFlag + ", address1=" + address1 + ", address2="
				+ address2 + ", city=" + city + ", provCode=" + provCode + ", countryCode=" + countryCode + ", postal="
				+ postal + "]";
	}
    
    
}
