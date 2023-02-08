package ca.bc.gov.educ.api.trax.repository;

import ca.bc.gov.educ.api.trax.model.entity.TraxStudentEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
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
            "m.stud_citiz as CITIZENSHIP, m.french_dogwood as FRENCH_DOGWOOD, null as ALLOWED_ADULT \n" +
            "from student_master m\n" +
            "where m.stud_no = :pen", nativeQuery=true)
    @Transactional(readOnly = true)
    List<Object[]> loadTraxStudent(@Param("pen") String pen);

    // Student Load for the graduated
    @Query(value="select trim(gs.stud_no) as PEN, gs.mincode as SCHOOL_OF_RECORD, m.mincode_grad as SCHOOL_AT_GRADUATION, gs.stud_grade as STUDENT_GRADE, m.stud_status as STUDENT_STATUS_CODE,\n" +
            "m.archive_flag as ARCHIVE_FLAG, gs.grad_reqt_year as GRAD_REQT_YEAR, gs.grad_date as GRAD_DATE, m.slp_date as SLP_DATE, m.scc_date as SCC_DATE,\n" +
            "trim(m.prgm_code) as PRGM_CODE1, trim(m.prgm_code2) as PRGM_CODE2, trim(m.prgm_code3) as PRGM_CODE3, trim(m.prgm_code4) as PRGM_CODE4, trim(m.prgm_code5) as PRGM_CODE5,\n" +
            "trim(m.french_cert) as FRENCH_CERT, trim(m.stud_consed_flag) as STUD_CONSED_FLAG, trim(m.english_cert) as ENGLISH_CERT, m.honour_flag as HONOUR_FLAG, \n" +
            "m.stud_citiz as CITIZENSHIP, m.french_dogwood as FRENCH_DOGWOOD, m.allowed_adult as ALLOWED_ADULT \n" +
            "from student_master m, tsw_tran_demog gs\n" +
            "where 1 = 1\n" +
            "and m.stud_no = gs.stud_no\n" +
            "and m.stud_no = :pen", nativeQuery=true)
    @Transactional(readOnly = true)
    List<Object[]> loadTraxGraduatedStudent(@Param("pen") String pen);

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

    @Query(value="select count(*) from TAB_SCHOOL sc \n" +
            "where sc.mincode = :minCode \n", nativeQuery=true)
    long countTabSchools(@Param("minCode") String minCode);

    @Query(value = "SELECT COUNT(*) FROM student_master WHERE stud_no = :pen AND grad_reqt_year = 'SCCP' AND scc_date > 0", nativeQuery = true)
    Integer countSccDateByPen(@Param("pen")String pen);

    @Query(value = "SELECT COUNT(*) FROM student_master WHERE stud_no = :pen AND grad_reqt_year <> 'SCCP' AND grad_date > 0 AND (slp_date is null OR slp_date = 0)", nativeQuery = true)
    Integer countGradDateByPen(@Param("pen")String pen);

    @Query(value = "SELECT COUNT(*) FROM tsw_tran_nongrad WHERE stud_no = :pen AND non_grad_code = 'T'", nativeQuery = true)
    Integer countAdult19RuleByPen(@Param("pen")String pen);

    @Query(value = "SELECT MAX(actv_date) FROM stud_actv WHERE stud_no = :pen AND actv_program = 'TRAX2040E1' AND actv_type = 'DOG' GROUP BY stud_no", nativeQuery = true)
    Date getTheLatestDistributionDate(@Param("pen")String pen);

}
