package ca.bc.gov.educ.api.trax.model.entity;

import lombok.Data;
import org.hibernate.annotations.Immutable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * The type TSW Transcript Demographics entity.
 */
@Data
@Immutable
@Entity
@Table(name = "TSW_TRAN_DEMOG")
public class TranscriptStudentDemogEntity {
    @Id
    @Column(name = "STUD_NO", unique = true, updatable = false)
    private String studNo;

    // School info
    @Column(name = "MINCODE")
    private String mincode;

    // Student Demographics
    @Column(name = "BIRTHDATE")
    private String birthDate;  // yyyymmdd

    @Column(name = "GRAD_REQT_YEAR")
    private String gradReqtYear; // yyyy

    @Column(name = "STUD_GRADE")
    private String studentGrade;

    @Column(name = "GRAD_DATE")
    private Long gradDate; // yyyymm

    @Column(name = "GRAD_MSG")
    private String gradMessage;

    @Column(name = "UPDATE_DT")
    private Long updateDate; // yyyymmdd
}
