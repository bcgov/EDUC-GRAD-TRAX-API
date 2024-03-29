package ca.bc.gov.educ.api.trax.repository;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Data
@Getter
@Setter
@Builder
public class TraxSchoolSearchCriteria implements Serializable {

    private static final long serialVersionUID = 1L;

    private String district;
    private boolean districtWildCard;
    private String minCode;
    private boolean minCodeWildCard;
    private String schoolName;
    private boolean schoolNameWildCard;

}
