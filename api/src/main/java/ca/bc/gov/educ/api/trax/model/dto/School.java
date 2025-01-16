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
	private String districtId;
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
		return "School [minCode=" + minCode + ", schoolId=" + schoolId + ", schoolName=" + schoolName + ", schoolCategoryCode=" + schoolCategoryCode + ", schoolCategoryLegacyCode=" + schoolCategoryLegacyCode
				+ ", districtId=" + districtId + ", districtName=" + districtName + ", transcriptEligibility=" + transcriptEligibility + ", certificateEligibility=" + certificateEligibility
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
