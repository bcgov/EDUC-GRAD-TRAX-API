package ca.bc.gov.educ.api.trax.support;

import ca.bc.gov.educ.api.trax.model.dto.*;
import ca.bc.gov.educ.api.trax.model.dto.DistrictContact;
import ca.bc.gov.educ.api.trax.model.dto.institute.*;
import ca.bc.gov.educ.api.trax.model.dto.institute.District;
import ca.bc.gov.educ.api.trax.model.dto.institute.School;
import ca.bc.gov.educ.api.trax.model.entity.EventEntity;
import ca.bc.gov.educ.api.trax.model.entity.EventHistoryEntity;
import ca.bc.gov.educ.api.trax.model.entity.TraxStudentEntity;
import ca.bc.gov.educ.api.trax.repository.EventHistoryRepository;
import ca.bc.gov.educ.api.trax.repository.EventRepository;
import ca.bc.gov.educ.api.trax.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.UUID;

import static ca.bc.gov.educ.api.trax.util.EducGradTraxApiConstants.DEFAULT_CREATED_BY;
import static ca.bc.gov.educ.api.trax.util.EducGradTraxApiConstants.DEFAULT_UPDATED_BY;
import static ca.bc.gov.educ.api.trax.constant.EventStatus.DB_COMMITTED;

public class TestUtils {
    public static GradStatusEventPayloadDTO createGraduationStatus(boolean isGraduated) {
        GradStatusEventPayloadDTO graduationStatus = new GradStatusEventPayloadDTO();
        graduationStatus.setPen("123456789");
        graduationStatus.setProgram("2018-EN");
        graduationStatus.setStudentStatus("CUR");
        graduationStatus.setStudentGrade("12");
        graduationStatus.setSchoolOfRecordId(UUID.randomUUID());
        graduationStatus.setStudentGrade("12");
        if (isGraduated) {
            graduationStatus.setSchoolAtGradId(UUID.randomUUID());
            graduationStatus.setProgramCompletionDate("2022-06-30");
        }
        return graduationStatus;
    }

    public static EventEntity createEvent(String eventType, Object payload, EventRepository eventRepository) throws JsonProcessingException {
        return createEvent(eventType, payload, LocalDateTime.now(), eventRepository);
    }

    public static EventEntity createEvent(String eventType, Object payload, LocalDateTime createDate, EventRepository eventRepository) throws JsonProcessingException {
        var event = EventEntity.builder()
                .eventType(eventType)
                .eventId(UUID.randomUUID())
                .eventOutcome("DB_COMMITTED")
                .eventPayload(JsonUtil.getJsonStringFromObject(payload))
                .eventStatus(DB_COMMITTED.toString())
                .createUser(DEFAULT_CREATED_BY)
                .updateUser(DEFAULT_UPDATED_BY)
                .createDate(createDate)
                .updateDate(LocalDateTime.now())
                .build();
        eventRepository.save(event);
        return event;
    }

    public static EventHistoryEntity createEventHistory(EventEntity event, LocalDateTime createdDate, EventHistoryRepository eventHistoryRepository) {
        var eventHistory = new EventHistoryEntity();
        eventHistory.setEvent(event);
        eventHistory.setAcknowledgeFlag("N");
        eventHistory.setCreateDate(createdDate);
        eventHistory.setCreateUser("TEST");
        eventHistory.setUpdateDate(LocalDateTime.now());
        eventHistory.setUpdateUser("TEST");
        eventHistoryRepository.save(eventHistory);
        return eventHistory;
    }

    public static AuthorityContact createAuthorityContact() {
        var auth = new AuthorityContact();
        auth.setIndependentAuthorityId(UUID.randomUUID().toString());
        auth.setFirstName("Bud");
        auth.setLastName("Weiser");
        auth.setPhoneNumber("3216549874");
        auth.setPhoneExtension("321");
        auth.setAlternatePhoneNumber("3216547894");
        auth.setAlternatePhoneExtension("555");
        auth.setEmail("bud.weiser@beers.ca");
        auth.setAuthorityContactTypeCode("DIRECTOR");
        auth.setEffectiveDate(LocalDateTime.now().toString());
        auth.setExpiryDate(LocalDateTime.now().plus(1, ChronoUnit.DAYS).toString());
        auth.setCreateDate(LocalDateTime.now().toString());
        auth.setCreateUser("TEST");
        auth.setUpdateDate(LocalDateTime.now().toString());
        auth.setUpdateUser("TEST");
        return auth;
    }

    public static SchoolContact createSchoolContact() {
        var contact = new SchoolContact();
        contact.setSchoolId(UUID.randomUUID().toString());
        contact.setFirstName("Testy");
        contact.setLastName("MacTesterton");
        contact.setPhoneNumber("3216549874");
        contact.setPhoneExtension("123");
        contact.setAlternatePhoneNumber("3216549874");
        contact.setAlternatePhoneExtension("321");
        contact.setEmail("t.testerton@test.ca");
        contact.setJobTitle("The Tester");
        contact.setSchoolContactTypeCode("PRINCIPAL");
        contact.setEffectiveDate(LocalDate.now().toString());
        contact.setExpiryDate(LocalDateTime.now().plus(1, ChronoUnit.DAYS).toString());
        contact.setCreateDate(LocalDateTime.now().toString());
        contact.setCreateUser("TEST");
        contact.setUpdateDate(LocalDateTime.now().toString());
        contact.setUpdateUser("TEST");
        return contact;
    }

    public static GradSchool createGradSchool() {
        var gradSchool = new GradSchool();
        gradSchool.setSchoolID(UUID.randomUUID().toString());
        gradSchool.setCanIssueCertificates("Y");
        gradSchool.setCanIssueTranscripts("Y");
        return gradSchool;
    }

    public static School createSchool() {
        var school = new School();
        school.setSchoolId(UUID.randomUUID().toString());
        school.setDistrictId(UUID.randomUUID().toString());
        school.setMincode("07996006");
        school.setIndependentAuthorityId(UUID.randomUUID().toString());
        school.setSchoolNumber("96006");
        school.setFaxNumber("2507436200");
        school.setPhoneNumber("2507435516");
        school.setEmail("executiveoffice@shawnigan.ca");
        school.setWebsite(null);
        school.setDisplayName("Shawnigan Lake");
        school.setDisplayNameNoSpecialChars("Shawnigan Lake");
        school.setSchoolReportingRequirementCode("REGULAR");
        school.setSchoolOrganizationCode("QUARTER");
        school.setSchoolCategoryCode("INDEPEND");
        school.setFacilityTypeCode("STANDARD");
        school.setOpenedDate("1989-09-01T00:00:00");
        school.setCanIssueCertificates(true);
        school.setCanIssueTranscripts(true);
        return school;
    }

    public static MoveSchoolData createMoveSchoolData() {
        var move = new MoveSchoolData();
        move.setToSchool(createSchool());
        move.setMoveDate(LocalDateTime.now().toString());
        move.setFromSchoolId(UUID.randomUUID().toString());
        return move;
    }

    public static DistrictContact createDistrictContact() {
        var contact = new DistrictContact();
        contact.setDistrictId(UUID.randomUUID().toString());
        contact.setFirstName("Testy");
        contact.setLastName("MacTesterton");
        contact.setPhoneNumber("3216549874");
        contact.setPhoneExtension("123");
        contact.setAlternatePhoneNumber("3216549874");
        contact.setAlternatePhoneExtension("321");
        contact.setEmail("t.testerton@test.ca");
        contact.setJobTitle("The Tester");
        contact.setDistrictContactTypeCode("PRINCIPAL");
        contact.setEffectiveDate(LocalDate.now().toString());
        contact.setExpiryDate(LocalDateTime.now().plus(1, ChronoUnit.DAYS).toString());
        contact.setCreateDate(LocalDateTime.now().toString());
        contact.setCreateUser("TEST");
        contact.setUpdateDate(LocalDateTime.now().toString());
        contact.setUpdateUser("TEST");
        return contact;
    }

    public static TraxStudentEntity createTraxStudent(boolean isGraduated) {
        TraxStudentEntity entity = TraxStudentEntity.builder()
                .studNo("123456789")
                .gradReqtYear("2018")
                .studGrade("12")
                .mincode("111222333")
                .studStatus("A")
                .archiveFlag("A")
                .build();
        if (isGraduated) {
            entity.setGradReqtYearAtGrad("2018");
            entity.setMincodeGrad("111222333");
            entity.setStudGradeAtGrad("12");
            entity.setGradDate(Long.valueOf("202206"));
            if (StringUtils.equals(entity.getGradReqtYear(), "SCCP")) {
                entity.setSlpDate(Long.valueOf("20220601"));
            }
        }
        return entity;
    }

    public static TraxStudentEntity createTraxStudent(String program, String studentStatus, boolean isGraduated) {
        final var traxStudent = TestUtils.createTraxStudent(isGraduated);
        String reqtYear = " ";
        if (StringUtils.startsWith(program, "2018")) {
            reqtYear = "2018";
        } else if (StringUtils.startsWith(program, "2004")) {
            reqtYear = "2004";
        } else if (StringUtils.startsWith(program, "1996")) {
            reqtYear = "1996";
        } else if (StringUtils.startsWith(program, "1986")) {
            reqtYear = "1986";
        } else if (StringUtils.startsWith(program, "1950")
                || StringUtils.startsWith(program, "NOPROG")) {
            reqtYear = "1950";
        } else if (StringUtils.startsWith(program, "SCCP")) {
            reqtYear = "SCCP";
        }
        traxStudent.setGradReqtYear(reqtYear);
        if (isGraduated) {
            if (StringUtils.equals(reqtYear, "SCCP")) {
                traxStudent.setGradReqtYearAtGrad(reqtYear);
                traxStudent.setMincodeGrad("111222333");
                traxStudent.setStudGradeAtGrad("12");
                traxStudent.setGradDate(Long.valueOf("202206"));
                traxStudent.setSlpDate(Long.valueOf("20220601"));
            } else {
                traxStudent.setGradReqtYearAtGrad(reqtYear);
            }
        }
        if (StringUtils.equals(studentStatus, "CUR")) {
            traxStudent.setStudStatus("A");
            traxStudent.setArchiveFlag("A");
        } else if (StringUtils.equals(studentStatus, "ARC")) {
            traxStudent.setStudStatus("A");
            traxStudent.setArchiveFlag("I");
        } else if (StringUtils.equals(studentStatus, "DEC")) {
            traxStudent.setStudStatus("D");
            traxStudent.setArchiveFlag("I");
        } else if (StringUtils.equals(studentStatus, "MER")) {
            traxStudent.setStudStatus("M");
            traxStudent.setArchiveFlag("I");
        } else if (StringUtils.equals(studentStatus, "TER")) {
            traxStudent.setStudStatus("T");
            traxStudent.setArchiveFlag("A");
        }
        return traxStudent;
    }

    public static District createDistrict() {
        District district = new District();
        district.setDistrictId(UUID.randomUUID().toString());
        district.setDistrictNumber("002");
        district.setFaxNumber("1233216547");
        district.setPhoneNumber("3216549874");
        district.setEmail("district@district.ca");
        district.setWebsite("www.district.ca");
        district.setDisplayName("Test Display Name");
        district.setDistrictRegionCode("NOT_APPLIC");
        district.setDistrictStatusCode("INACTIVE");
        return district;
    }

    public static SchoolDetail createSchoolDetail(){
        String schoolId = UUID.randomUUID().toString();
        SchoolAddress schoolAddress = new SchoolAddress();
        schoolAddress.setSchoolId(schoolId);
        schoolAddress.setAddressLine1("123 Fake St");
        schoolAddress.setCity("Vancouverland");
        schoolAddress.setCountryCode("CAN");
        schoolAddress.setPostal("VQV2L2");
        SchoolDetail schoolDetail = new SchoolDetail();
        schoolDetail.setSchoolId(schoolId);
        schoolDetail.setSchoolNumber("96006");
        schoolDetail.setDistrictId(UUID.randomUUID().toString());
        schoolDetail.setAddresses(Arrays.asList(schoolAddress));
        schoolDetail.setCreateDate(LocalDateTime.now().toString());
        schoolDetail.setCanIssueCertificates(true);
        schoolDetail.setDisplayName("Blah");
        schoolDetail.setCreateUser("Test");
        schoolDetail.setUpdateDate(LocalDateTime.now().toString());
        schoolDetail.setUpdateUser("Test");
        schoolDetail.setCreateDate(LocalDateTime.now().toString());
        schoolDetail.setCanIssueTranscripts(true);
        schoolDetail.setDisplayNameNoSpecialChars("blah blah");
        return schoolDetail;
    }
}
