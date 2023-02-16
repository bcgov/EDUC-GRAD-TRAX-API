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

    @Column(name = "LOGO_TYPE")
    private String logoType;

    @Column(name = "ARCHIVE_FLAG")
    private String archiveFlag;

    // School info
    @Column(name = "MINCODE")
    private String mincode;

    @Column(name = "SCHOOL_NM")
    private String schoolName;

    @Column(name = "SCH_ADDRESS1")
    private String address1;

    @Column(name = "SCH_ADDRESS2")
    private String city;

    @Column(name = "SCH_ADDRESS3")
    private String provCode;

    @Column(name = "SCH_ADDRESS4")
    private String postal;

    @Column(name = "LOCAL_ID")
    private String localId;

    @Column(name = "EARLY_ADMIT")
    private String earlyAdmission;

    // Student Demographics
    @Column(name = "BIRTHDATE")
    private String birthDate;  // yyyymmdd

    @Column(name = "GRAD_REQT_YEAR")
    private String gradReqtYear; // yyyy

    @Column(name = "STUD_GRADE")
    private String studentGrade;

    @Column(name = "STUD_CITIZ")
    private String studCitiz;

    @Column(name = "STUD_GENDER")
    private String studGender;

    @Column(name = "PROGRAM_CODE")
    private String programCode;

    @Column(name = "LAST_NAME")
    private String lastName;

    @Column(name = "FIRST_NAME")
    private String firstName;

    @Column(name = "MIDDLE_NAME")
    private String middleName;

    @Column(name = "GRAD_DATE")
    private Long gradDate; // yyyymm

    @Column(name = "GRAD_FLAG")
    private String gradFlag;

    @Column(name = "TOTAL_CREDITS")
    private String totalCredits;

    @Column(name = "GRAD_MSG")
    private String gradMessage;

    @Column(name = "GRAD_MSG_TXT")
    private String gradTextMessage;

    @Column(name = "CURRENT_FORMER_FLAG")
    private String currentFormerFlag;

    @Column(name = "UPDATE_DT")
    private Long updateDate; // yyyymmdd
}
