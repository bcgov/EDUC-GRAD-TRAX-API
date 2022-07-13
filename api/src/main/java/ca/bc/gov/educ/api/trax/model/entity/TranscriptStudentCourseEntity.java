package ca.bc.gov.educ.api.trax.model.entity;

import lombok.Data;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;

/**
 * The type TSW Transcript Course entity.
 */
@Data
@Immutable
@Entity
@Table(name = "TSW_TRAN_CRSE")
public class TranscriptStudentCourseEntity {

    @EmbeddedId
    private TranscriptStudentCourseKey studentCourseKey;

    @Column(name = "RPT_CRS_TYPE")
    private String reportType;

    @Column(name = "COURSE_NAME")
    private String courseName;

    // School info
    @Column(name = "FOUNDATION_REQ")
    private String foundationReq;

    @Column(name = "SPECIAL_CASE")
    private String specialCase;

    @Column(name = "COURSE_SESSION")
    private String courseSession;

    @Column(name = "SCHOOL_PCT")
    private String schoolPercentage;

    @Column(name = "EXAM_PCT")
    private String examPercentage;

    @Column(name = "FINAL_PCT")
    private String finalPercentage;

    @Column(name = "FINAL_LG")
    private String finalLG;

    @Column(name = "INTERIM_MARK")
    private String interimMark;

    @Column(name = "NUM_CREDITS")
    private String numberOfCredits;

    @Column(name = "CRSE_TYPE")
    private String courseType;

    @Column(name = "USED_FOR_GRAD")
    private String usedForGrad;

    @Column(name = "MET_LIT_NUM_REQT")
    private String metLitNumReqt;

    @Column(name = "RELATED_CRSE")
    private String relatedCourse;

    @Column(name = "RELATED_LEVEL")
    private String relatedCourseLevel;

    @Column(name = "UPDATE_DT")
    private Long updateDate; // yyyymmdd
}
