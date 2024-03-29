package ca.bc.gov.educ.api.trax.repository;

import ca.bc.gov.educ.api.trax.model.entity.TraxStudentEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * The interface TRAX Student repository.
 */
@Repository
public interface TraxStudentRepository extends CrudRepository<TraxStudentEntity, String> {

    // Student Load for the non-graduated
    @Query(value="select trim(m.stud_no) as PEN, m.mincode as SCHOOL_OF_RECORD, m.mincode_grad as SCHOOL_AT_GRADUATION, m.stud_grade as STUDENT_GRADE, m.stud_status as STUDENT_STATUS_CODE,\n" +
            "m.archive_flag as ARCHIVE_FLAG, m.grad_reqt_year as GRAD_REQT_YEAR, m.grad_date as GRAD_DATE, m.slp_date as SLP_DATE, m.scc_date as SCC_DATE,\n" +
            "trim(m.prgm_code) as PRGM_CODE1, trim(m.prgm_code2) as PRGM_CODE2, trim(m.prgm_code3) as PRGM_CODE3, trim(m.prgm_code4) as PRGM_CODE4, trim(m.prgm_code5) as PRGM_CODE5,\n" +
            "trim(m.french_cert) as FRENCH_CERT, trim(m.stud_consed_flag) as STUD_CONSED_FLAG, trim(m.english_cert) as ENGLISH_CERT, m.honour_flag as HONOUR_FLAG, \n" +
            "m.stud_citiz as CITIZENSHIP, m.french_dogwood as FRENCH_DOGWOOD \n" +
            "from student_master m\n" +
            "where m.stud_no = :pen", nativeQuery=true)
    @Transactional(readOnly = true)
    List<Object[]> loadTraxStudent(@Param("pen") String pen);

    // Get Student Demographics Info from TRAX
    @Query(value="select trim(m.stud_no) as PEN, m.stud_given as LEGAL_FIRST_NAME, m.stud_surname as LEGAL_LAST_NAME, m.stud_middle as LEGAL_MIDDLE_NAME,\n" +
            "m.stud_status as STUDENT_STATUS_CODE, m.archive_flag as ARCHIVE_FLAG, m.mincode as SCHOOL_OF_RECORD, m.stud_grade as STUDENT_GRADE, m.postal as POSTAL_CODE,\n" +
            "m.stud_sex as SEX_CODE, m.stud_birth as BIRTH_DATE, m.grad_date as GRAD_DATE, m.stud_true_no as TRUE_PEN, m.stud_local_id as LOCAL_ID \n" +
            "from student_master m\n" +
            "where m.stud_no = :pen", nativeQuery=true)
    @Transactional(readOnly = true)
    List<Object[]> loadStudentDemographicsData(@Param("pen") String pen);

    @Query(value="select trim(c1.crse_code) as CRSE_MAIN, trim(c1.crse_level) as CRSE_MAIN_LVL,\n" +
            " trim(c2.crse_code) as CRSE_RESTRICTED, trim(c2.crse_level) as CRSE_RESTRICTED_LVL,\n" +
            " trim(c1.start_restrict_session) as RESTRICTION_START_DT, trim(c1.end_restrict_session) as RESTRICTION_END_DT\n" +
            "from tab_crse c1\n" +
            "join tab_crse c2\n" +
            "on c1.restriction_code = c2.restriction_code\n" +
            "and (c1.crse_code  <> c2.crse_code or c1.crse_level <> c2.crse_level)\n" +
            "and c1.restriction_code <> ' '", nativeQuery=true)
    @Transactional(readOnly = true)
    List<Object[]> loadInitialCourseRestrictionRawData();

}
