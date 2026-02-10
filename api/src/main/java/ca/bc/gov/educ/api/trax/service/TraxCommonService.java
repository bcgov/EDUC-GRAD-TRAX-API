package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.constant.ConversionResultType;
import ca.bc.gov.educ.api.trax.model.dto.*;
import ca.bc.gov.educ.api.trax.model.entity.TraxStudentNoEntity;
import ca.bc.gov.educ.api.trax.model.transformer.GradCourseTransformer;
import ca.bc.gov.educ.api.trax.model.transformer.TraxStudentNoTransformer;
import ca.bc.gov.educ.api.trax.repository.GradCourseRepository;
import ca.bc.gov.educ.api.trax.repository.TraxStudentNoRepository;
import ca.bc.gov.educ.api.trax.repository.TraxStudentRepository;
import ca.bc.gov.educ.api.trax.service.institute.SchoolService;
import ca.bc.gov.educ.api.trax.util.EducGradTraxApiConstants;
import ca.bc.gov.educ.api.trax.util.EducGradTraxApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@SuppressWarnings("ALL")
@Service
@Slf4j
public class TraxCommonService {

    private SchoolService schoolService;
    private final TraxStudentRepository traxStudentRepository;
    private final TraxStudentNoRepository traxStudentNoRepository;
    private final GradCourseRepository gradCourseRepository;

    private final TraxStudentNoTransformer traxStudentNoTransformer;
    private final GradCourseTransformer gradCourseTransformer;

    private final EducGradTraxApiConstants constants;

    @Autowired
    public TraxCommonService(SchoolService schoolService,
                             TraxStudentRepository traxStudentRepository,
                             TraxStudentNoRepository traxStudentNoRepository,
                             GradCourseRepository gradCourseRepository,
                             TraxStudentNoTransformer traxStudentNoTransformer,
                             GradCourseTransformer gradCourseTransformer,
                             EducGradTraxApiConstants constants) {
        this.schoolService = schoolService;
        this.traxStudentRepository = traxStudentRepository;
        this.traxStudentNoRepository = traxStudentNoRepository;
        this.gradCourseRepository = gradCourseRepository;
        this.traxStudentNoTransformer = traxStudentNoTransformer;
        this.gradCourseTransformer = gradCourseTransformer;

        this.constants = constants;
    }

    // Pagination
    @Transactional(readOnly = true)
    public List<TraxStudentNo> loadTraxStudentNoByPage(Pageable pageable) {
        List<TraxStudentNoEntity> list = traxStudentNoRepository.findAllByStatus(null, pageable).toList();
        return traxStudentNoTransformer.transformToDTO(list);
    }

    @Transactional(readOnly = true)
    public Integer getTotalNumberOfTraxStudentNo() {
        return traxStudentNoRepository.countAllByStatus(null);
    }

    // Student Master from TRAX
    @Transactional(readOnly = true)
    public List<ConvGradStudent> getStudentMasterDataFromTrax(String pen) {
        List<Object[]> results = traxStudentRepository.loadTraxStudent(StringUtils.rightPad(pen, 10));
        return buildConversionGradStudents(results);
    }

    @Transactional(readOnly = true)
    public List<Student> getStudentDemographicsDataFromTrax(String pen) {
        List<Student> students = new ArrayList<>();
        List<Object[]> results = traxStudentRepository.loadStudentDemographicsData(StringUtils.rightPad(pen, 10));
        results.forEach(result -> {
            String legalFirstName = (String) result[1];
            legalFirstName = StringUtils.isNotBlank(legalFirstName)? legalFirstName.trim() : null;
            String legalLastName = (String) result[2];
            legalLastName = StringUtils.isNotBlank(legalLastName)? legalLastName.trim() : null;
            String legalMiddleName = (String) result[3];
            legalMiddleName = StringUtils.isNotBlank(legalMiddleName)? legalMiddleName.trim() : null;

            Character studStatus = (Character) result[4];
            String studentStatusCode = studStatus != null? studStatus.toString() : null;
            if (StringUtils.equals(studentStatusCode, "T")) {
                studentStatusCode = "A";
            }
            log.debug(" TRAX - PEN mapping : stud_status [{}] => status code [{}]", studStatus,  studentStatusCode);

            String schoolOfRecord = (String) result[6];
            String studGrade = (String) result[7];
            String studentGrade;
            if (!NumberUtils.isCreatable(studGrade)) {
                studentGrade = "11";
            } else {
                studentGrade = studGrade;
            }
            log.debug(" TRAX - PEN mapping : stud_grade [{}] => grade code [{}]", studGrade,  studentGrade);
            String postal = (String) result[8];
            Character sexCode = (Character) result[9];
            String birthDate = (String) result[10];
            String formattedBirthDate = birthDate.substring(0, 4) + "-" + birthDate.substring(4, 6) + "-" + birthDate.substring(6, 8);

            String truePen = (String) result[12];
            truePen = StringUtils.isNotBlank(truePen)? truePen.trim() : null;

            String localID = (String) result[13];

            Student student = Student.builder()
                    .pen(pen)
                    .legalFirstName(legalFirstName)
                    .legalLastName(legalLastName)
                    .legalMiddleNames(legalMiddleName)
                    .usualFirstName(legalFirstName)
                    .usualLastName(legalLastName)
                    .usualMiddleNames(legalMiddleName)
                    .statusCode(studentStatusCode)
                    .genderCode(sexCode.toString())
                    .sexCode(sexCode.toString())
                    .mincode(schoolOfRecord)
                    .postalCode(postal)
                    .dob(formattedBirthDate)
                    .gradeCode(studentGrade)
                    .emailVerified("Y")
                    .truePen(truePen)
                    .localID(localID)
                    .build();
            students.add(student);
        });
        return students;
    }

    @Transactional(readOnly = true)
    public List<CourseRestriction> loadGradCourseRestrictionsDataFromTrax() {
        List<CourseRestriction> courseRestrictions = new ArrayList<>();
        List<Object[]> results = traxStudentRepository.loadInitialCourseRestrictionRawData();
        results.forEach(result -> {
            String mainCourse = (String) result[0];
            String mainCourseLevel = (String) result[1];
            String restrictedCourse = (String) result[2];
            String restrictedCourseLevel = (String) result[3];
            String startDate = (String) result[4];
            String endDate = (String) result[5];

            // check null value for course level and convert it to space
            if (StringUtils.isBlank(mainCourseLevel)) {
                mainCourseLevel = " ";
            }
            if (StringUtils.isBlank(restrictedCourseLevel)) {
                restrictedCourseLevel = " ";
            }
            CourseRestriction courseRestriction = new CourseRestriction(
                    null, mainCourse, mainCourseLevel, restrictedCourse, restrictedCourseLevel, startDate, endDate);
            courseRestrictions.add(courseRestriction);
        });
        return courseRestrictions;
    }

    @Transactional(readOnly = true)
    public List<GradCourse> loadGradCourseRequirementsDataFromTrax() {
        return gradCourseTransformer.transformToDTO(gradCourseRepository.findAll()); // .subList(0,1)
    }

    @Transactional
    public TraxStudentNo saveTraxStudentNo(TraxStudentNo traxStudentNo) {
        Optional<TraxStudentNoEntity> optional = traxStudentNoRepository.findById(traxStudentNo.getStudNo());
        if (optional.isPresent()) {
            TraxStudentNoEntity entity = optional.get();
            BeanUtils.copyProperties(traxStudentNo, entity);
            return traxStudentNoTransformer.transformToDTO(traxStudentNoRepository.save(entity));
        }
        return traxStudentNo;
    }

    @Transactional
    public TraxStudentNo deleteTraxStudentNo(String pen) {
        Optional<TraxStudentNoEntity> optional = traxStudentNoRepository.findById(pen);
        if (optional.isPresent()) {
            return traxStudentNoTransformer.transformToDTO(traxStudentNoRepository.deleteById(pen));
        }
        return null;
    }

    public UUID getSchoolIdFromRedisCache(String mincode) {
        ca.bc.gov.educ.api.trax.model.dto.institute.School school = schoolService.getSchoolByMinCodeFromRedisCache(mincode);
        return school != null && StringUtils.isNotBlank(school.getSchoolId())? UUID.fromString(school.getSchoolId()) : null;
    }

    private List<ConvGradStudent> buildConversionGradStudents(List<Object[]> traxStudents) {
        List<ConvGradStudent> students = new ArrayList<>();
        traxStudents.forEach(result -> {
            ConvGradStudent student = populateConvGradStudent(result);
            students.add(student);
        });
        return students;
    }

    private void populateProgramCode(String code, List<String> optionalProgramCodes) {
        if (StringUtils.isNotBlank(code)) {
            if (code.length() > 2) {
                optionalProgramCodes.add(StringUtils.substring(code,2));
            } else {
                optionalProgramCodes.add(code);
            }
        }
    }

    private ConvGradStudent populateConvGradStudent(Object[] fields) {
        String pen = (String) fields[0];
        String schoolOfRecord = (String) fields[1];
        String schoolAtGrad = (String) fields[2];
        String studentGrade = (String) fields[3];
        Character studentStatus = (Character) fields[4];
        Character archiveFlag = (Character) fields[5];
        String graduationRequirementYear = (String) fields[6];

        UUID schoolOfRecordId = this.getSchoolIdFromRedisCache(schoolOfRecord);
        UUID schoolAtGradId = this.getSchoolIdFromRedisCache(schoolAtGrad);

        // grad or non-grad
        Date programCompletionDate = null;
        String gradDateStr = null;
        Integer gradDate = (Integer) fields[7]; // from student_master in trax
        if (gradDate != null && !gradDate.equals(Integer.valueOf(0))) {
            gradDateStr = gradDate.toString();
        }

        if (StringUtils.isNotBlank(gradDateStr) && !StringUtils.equals(gradDateStr, "0")) {
            try {
                programCompletionDate = EducGradTraxApiUtils.parseDate(gradDateStr, "yyyyMM");
            } catch (Exception e) {
                log.error("graduated date conversion is failed for gradDate = " + gradDateStr);
            }
        }

        // slp date
        Integer slpDate = (Integer) fields[8];
        String slpDateStr = slpDate != null && !slpDate.equals(Integer.valueOf(0)) ? slpDate.toString() : null;

        // scc date
        Integer sccDate = (Integer) fields[9];
        String sccDateStr = sccDate != null && !sccDate.equals(Integer.valueOf(0)) ? sccDate.toString() : null;

        List<String> programCodes = new ArrayList<>();
        // student optional/career programs
        populateProgramCode((String) fields[10], programCodes);
        populateProgramCode((String) fields[11], programCodes);
        populateProgramCode((String) fields[12], programCodes);
        populateProgramCode((String) fields[13], programCodes);
        populateProgramCode((String) fields[14], programCodes);

        // french cert
        String frenchCert = (String) fields[15];

        // consumer education requirement met
        Character consumerEducationRequirementMet = (Character) fields[16];

        // english cert
        String englishCert = (String) fields[17];

        // honour_flag
        Character honourFlag = (Character) fields[18];

        // stud_citiz
        Character citizenship = (Character) fields[19];

        // french dogwood
        Character frenchDogwood = (Character) fields[20];

        ConvGradStudent student = null;
        try {
            student = ConvGradStudent.builder()
                    .pen(pen)
                    .slpDate(slpDateStr)
                    .sccDate(sccDateStr)
                    .schoolOfRecordId(schoolOfRecordId)
                    .schoolAtGradId(schoolAtGradId)
                    .studentGrade(studentGrade)
                    .studentStatus(studentStatus != null? studentStatus.toString() : null)
                    .archiveFlag(archiveFlag != null? archiveFlag.toString() : null)
                    .frenchCert(frenchCert)
                    .englishCert(englishCert)
                    .honoursStanding(honourFlag != null? honourFlag.toString() : null)
                    .graduationRequirementYear(graduationRequirementYear)
                    .programCodes(programCodes)
                    .programCompletionDate(programCompletionDate)
                    .consumerEducationRequirementMet(consumerEducationRequirementMet != null? (StringUtils.equalsIgnoreCase(consumerEducationRequirementMet.toString(), "Y")? "Y" : null) : null)
                    .studentCitizenship(citizenship != null? citizenship.toString() : null)
                    .frenchDogwood(frenchDogwood != null? frenchDogwood.toString() : null)
                    .adult19Rule(false)
                    .result(ConversionResultType.SUCCESS)
                    .build();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return student;
    }

}
