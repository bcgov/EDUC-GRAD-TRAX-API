package ca.bc.gov.educ.api.trax.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * The type TRAX Student entity.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "SNAPSHOT")
@IdClass(SnapshotID.class)
public class SnapshotEntity {
    @Id
    @Column(name = "GRAD_YEAR", nullable = false, updatable = false)
    private Integer gradYear;

    @Id
    @Column(name = "STUD_NO", nullable = false, updatable = false)
    private String pen;

    @Column(name = "STUD_GRADE")
    private String studGrade;

    @Column(name = "GRADUATED")
    private String graduatedDate;

    @Column(name = "MINCODE")
    private String schoolOfRecord;

    @Column(name = "STUD_GPA")
    private BigDecimal gpa;

    @Column(name = "HONOUR_FLAG")
    private String honourFlag;

    @Column(name = "STUD_SEX")
    private String studSex;

    @Column(name = "PRGM_CODE")
    private String prgmCode;

    @Column(name = "PRGM_CODE2")
    private String prgmCode2;

    @Column(name = "PRGM_CODE3")
    private String prgmCode3;

    @Column(name = "PRGM_CODE4")
    private String prgmCode4;

    @Column(name = "PRGM_CODE5")
    private String prgmCode5;
}
