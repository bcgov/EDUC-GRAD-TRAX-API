package ca.bc.gov.educ.api.trax.model.dto;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Data
@Component
public class School implements Comparable<School> {

	private String minCode;
	private String schoolId;
    private String schoolName;
    private String districtName;
    private String transcriptEligibility;    
    private String certificateEligibility;
    private String address1;    
    private String address2;    
    private String city;    
    private String provCode;
    private String countryCode;
    private String postal;
	private String openFlag;
	private String schoolCategoryCode;
	private String schoolCategoryLegacyCode;

	public String getSchoolName() {
		return  schoolName != null ? schoolName.trim(): "";
	}
	
	public String getDistrictName() {
		return districtName != null ? districtName.trim(): "";
	}
	
	public String getAddress1() {
		return address1 != null ? address1.trim(): "";
	}

	public String getAddress2() {
		return address2 != null ? address2.trim(): "";
	}

	public String getCity() {
		return city != null ? city.trim(): "";
	}
	
	public String getPostal() {
		return postal != null ? postal.trim(): "";
	}

	public String getMinCode() {
		return minCode != null ? minCode.trim(): "";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		School school = (School) o;
		return getMinCode().equals(school.getMinCode())
				&& getSchoolName().equals(school.getSchoolName());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getMinCode(), getSchoolName());
	}

	@Override
	public String toString() {
		return "School [minCode=" + minCode + ", schoolId=" + schoolId + ", schoolCategoryCode=" + schoolCategoryCode + ", schoolCategoryLegacyCode=" + schoolCategoryLegacyCode
				+ ", schoolName=" + schoolName + ", districtName=" + districtName + ", transcriptEligibility=" + transcriptEligibility + ", certificateEligibility=" + certificateEligibility
				+ ", address1=" + address1 + ", address2=" + address2 + ", city=" + city + ", provCode=" + provCode + ", countryCode=" + countryCode + ", postal=" + postal + ", openFlag=" + openFlag
				+ "]";
	}

	@Override
	public int compareTo(School o) {
		int result = 0;
		{
			if (result == 0) {
				result = getMinCode().compareToIgnoreCase(o.getMinCode());
			}
			if (result == 0) {
				result = getSchoolName().compareToIgnoreCase(o.getSchoolName());
			}
		}
		return result;
	}
}
