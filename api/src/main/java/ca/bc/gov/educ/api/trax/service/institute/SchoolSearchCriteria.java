package ca.bc.gov.educ.api.trax.service.institute;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class SchoolSearchCriteria {
    private String districtId;
    private String mincode;
    private String displayName;
    private String distNo;
    private List<String> schoolCategoryCodes;

    public String toString() {
        return String.format("DistrictId: %s, Mincode: %s, DisplayName: %s, DisNo: %s, SchoolCategoryCodes: %s", districtId, mincode, displayName, distNo, schoolCategoryCodes);
    }
}