package ca.bc.gov.educ.api.trax.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * The type TSW Transcript Course.
 */
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TranscriptStudentCourse {
    private String studNo;
    private String courseCode;
    private String courseLevel;

    private String reportType;
    private String courseName;
    private String foundationReq;
    private String specialCase;
    private String courseSession;
    private String schoolPercentage;
    private String examPercentage;
    private String finalPercentage;
    private String finalLG;
    private String interimMark;
    private String numberOfCredits;
    private String courseType;
    private String usedForGrad;
    private String metLitNumReqt;
    private String relatedCourse;
    private String relatedCourseLevel;
    private Long updateDate; // yyyymmdd

    public String getReportType() {
        return reportType!=null?reportType.trim():null;
    }

    public String getFoundationReq() {
        return foundationReq!=null?reportType.trim():null;
    }

    public String getSpecialCase() {
        return specialCase!=null?reportType.trim():null;
    }

    public String getSchoolPercentage() {
        return schoolPercentage!=null?reportType.trim():null;
    }

    public String getFinalPercentage() {
        return finalPercentage!=null?reportType.trim():null;
    }

    public String getFinalLG() {
        return finalLG!=null?reportType.trim():null;
    }

    public String getInterimMark() {
        return interimMark!=null?reportType.trim():null;
    }

    public String getNumberOfCredits() {
        return numberOfCredits!=null?reportType.trim():null;
    }

    public String getCourseType() {
        return courseType!=null?reportType.trim():null;
    }

    public String getUsedForGrad() {
        return usedForGrad!=null?reportType.trim():null;
    }

    public String getMetLitNumReqt() {
        return metLitNumReqt!=null?reportType.trim():null;
    }

    public String getRelatedCourseLevel() {
        return relatedCourseLevel!=null?reportType.trim():null;
    }

    public String getStudNo() {
        return studNo != null ? studNo.trim():null;
    }

    public String getCourseCode() {
        return courseCode != null ? courseCode.trim(): null;
    }
    public String getCourseName() {
        return courseName != null ? courseName.trim(): null;
    }

    public String getCourseLevel() {
        return courseLevel != null ? courseLevel.trim(): null;
    }

}
