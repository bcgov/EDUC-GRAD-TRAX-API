package ca.bc.gov.educ.api.trax.model.entity;

import lombok.Data;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Data
public class TranscriptStudentCourseKey implements Serializable {
    private static final long serialVersionUID = 1L;

    @Column(name = "STUD_NO", unique = true, updatable = false)
    private String studNo;

    @Column(name = "CRSE_CODE", insertable = false, updatable = false, length = 5)
    private String courseCode;

    @Column(name = "CRSE_LEVEL", insertable = false, updatable = false, length = 3)
    private String courseLevel;

    public TranscriptStudentCourseKey() {
    }

    public TranscriptStudentCourseKey(String studNo, String courseCode, String courseLevel) {
        this.studNo = studNo;
        this.courseCode = courseCode;
        this.courseLevel = courseLevel;
    }
}
