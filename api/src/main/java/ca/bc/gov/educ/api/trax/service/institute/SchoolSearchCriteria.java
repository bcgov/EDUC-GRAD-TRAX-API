package ca.bc.gov.educ.api.trax.service.institute;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SchoolSearchCriteria {
    private String districtId;
    private String mincode;
    private String displayName;
    private String distNo;

    public String toString() {
        return String.format("DistrictId: %s, Mincode: %s, DisplayName: %s, DisNo: %s", districtId, mincode, displayName, distNo);
    }
}