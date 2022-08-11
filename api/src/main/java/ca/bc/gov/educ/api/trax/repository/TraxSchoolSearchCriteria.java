package ca.bc.gov.educ.api.trax.repository;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Data
@Getter
@Setter
@Builder
public class TraxSchoolSearchCriteria implements Serializable {

    private static final long serialVersionUID = 1L;

    private String district;
    private String minCode;
    private String schoolName;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TraxSchoolSearchCriteria that = (TraxSchoolSearchCriteria) o;
        return Objects.equals(district, that.district) && Objects.equals(minCode, that.minCode) && Objects.equals(schoolName, that.schoolName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(district, minCode, schoolName);
    }
}
