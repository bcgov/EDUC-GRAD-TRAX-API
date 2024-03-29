package ca.bc.gov.educ.api.trax.model.entity;

import lombok.Data;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Data
public class GradCourseKey implements Serializable {
    private static final long serialVersionUID = 1L;

    @Column(name = "CRSE_CODE", insertable = false, updatable = false, length = 5)
    private String courseCode;

    @Column(name = "CRSE_LEVEL", insertable = false, updatable = false, length = 3)
    private String courseLevel;

    @Column(name = "GRAD_REQT_YEAR", insertable = false, updatable = false, length = 4)
    private String gradReqtYear;


    public GradCourseKey() {
    }

    public GradCourseKey(String courseCode, String courseLevel, String gradReqtYear) {
        this.courseCode = courseCode;
        this.courseLevel = courseLevel;
        this.gradReqtYear = gradReqtYear;
    }
}
