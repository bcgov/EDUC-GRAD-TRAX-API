package ca.bc.gov.educ.api.trax.model.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class SnapshotResponse {
    private String pen;
    private String graduatedDate; // yyyyMM
    private BigDecimal gpa;
    private String honourFlag;
    private String schoolOfRecord;
    private String schoolOfRecordId;
    private String studentGrade;

    public SnapshotResponse(String pen, String graduatedDate, BigDecimal gpa, String honourFlag, String schoolOfRecord, String studentGrade) {
        this.pen = pen;
        this.graduatedDate = graduatedDate;
        this.gpa = gpa;
        this.honourFlag = honourFlag;
        this.schoolOfRecord = schoolOfRecord;
        this.studentGrade = studentGrade;
    }
}
