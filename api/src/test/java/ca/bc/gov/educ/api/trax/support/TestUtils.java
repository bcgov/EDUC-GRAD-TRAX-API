package ca.bc.gov.educ.api.trax.support;

import ca.bc.gov.educ.api.trax.model.dto.AuthorityContact;
import ca.bc.gov.educ.api.trax.model.dto.DistrictContact;
import ca.bc.gov.educ.api.trax.model.dto.GradStatusEventPayloadDTO;
import ca.bc.gov.educ.api.trax.model.dto.SchoolContact;
import ca.bc.gov.educ.api.trax.model.entity.Event;
import ca.bc.gov.educ.api.trax.model.entity.TraxStudentEntity;
import ca.bc.gov.educ.api.trax.repository.EventRepository;
import ca.bc.gov.educ.api.trax.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static ca.bc.gov.educ.api.trax.util.EducGradTraxApiConstants.DEFAULT_CREATED_BY;
import static ca.bc.gov.educ.api.trax.util.EducGradTraxApiConstants.DEFAULT_UPDATED_BY;
import static ca.bc.gov.educ.api.trax.constant.EventStatus.DB_COMMITTED;

public class TestUtils {
    public static GradStatusEventPayloadDTO createGraduationStatus(boolean isGraduated) {
        GradStatusEventPayloadDTO graduationStatus = new GradStatusEventPayloadDTO();
        graduationStatus.setPen("123456789 ");
        graduationStatus.setProgram("2018-EN");
        graduationStatus.setStudentStatus("CUR");
        graduationStatus.setStudentGrade("12");
        graduationStatus.setSchoolOfRecord("111222333");
        graduationStatus.setStudentGrade("12");
        if (isGraduated) {
            graduationStatus.setSchoolAtGrad("111222333");
            graduationStatus.setProgramCompletionDate("2022-06-30");
        }
        return graduationStatus;
    }

    public static Event createEvent(String eventType, Object payload, EventRepository eventRepository) throws JsonProcessingException {
        var event = Event.builder()
                .eventType(eventType)
                .eventId(UUID.randomUUID())
                .eventOutcome("DB_COMMITTED")
                .eventPayload(JsonUtil.getJsonStringFromObject(payload))
                .eventStatus(DB_COMMITTED.toString())
                .createUser(DEFAULT_CREATED_BY)
                .updateUser(DEFAULT_UPDATED_BY)
                .createDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();
        eventRepository.save(event);
        return event;
    }

    public static AuthorityContact createAuthorityContact() {
        var auth = AuthorityContact.builder()
                .independentAuthorityId(UUID.randomUUID().toString())
                .firstName("Bud")
                .lastName("Weiser")
                .phoneNumber("3216549874")
                .phoneExtension("321")
                .alternatePhoneNumber("3216547894")
                .alternatePhoneExtension("555")
                .email("bud.weiser@beers.ca")
                .authorityContactTypeCode("DIRECTOR")
                .effectiveDate(LocalDateTime.now().toString())
                .expiryDate(LocalDateTime.now().plus(1, ChronoUnit.DAYS).toString())
                .build();
        auth.setCreateDate(LocalDateTime.now().toString());
        auth.setCreateUser("TEST");
        auth.setUpdateDate(LocalDateTime.now().toString());
        auth.setUpdateUser("TEST");
        return auth;
    }

    public static SchoolContact createSchoolContact() {
        var contact = SchoolContact.builder()
                .schoolId(UUID.randomUUID().toString())
                .firstName("Testy")
                .lastName("MacTesterton")
                .phoneNumber("3216549874")
                .phoneExtension("123")
                .alternatePhoneNumber("3216549874")
                .alternatePhoneExtension("321")
                .email("t.testerton@test.ca")
                .jobTitle("The Tester")
                .schoolContactTypeCode("PRINCIPAL")
                .effectiveDate(LocalDate.now().toString())
                .expiryDate(LocalDateTime.now().plus(1, ChronoUnit.DAYS).toString())
                .build();
        contact.setCreateDate(LocalDateTime.now().toString());
        contact.setCreateUser("TEST");
        contact.setUpdateDate(LocalDateTime.now().toString());
        contact.setUpdateUser("TEST");
        return contact;
    }

    public static DistrictContact createDistrictContact() {
        var contact = DistrictContact.builder()
                .districtId(UUID.randomUUID().toString())
                .firstName("Testy")
                .lastName("MacTesterton")
                .phoneNumber("3216549874")
                .phoneExtension("123")
                .alternatePhoneNumber("3216549874")
                .alternatePhoneExtension("321")
                .email("t.testerton@test.ca")
                .jobTitle("The Tester")
                .districtContactTypeCode("PRINCIPAL")
                .effectiveDate(LocalDate.now().toString())
                .expiryDate(LocalDateTime.now().plus(1, ChronoUnit.DAYS).toString())
                .build();
        contact.setCreateDate(LocalDateTime.now().toString());
        contact.setCreateUser("TEST");
        contact.setUpdateDate(LocalDateTime.now().toString());
        contact.setUpdateUser("TEST");
        return contact;
    }

    public static TraxStudentEntity createTraxStudent(boolean isGraduated) {
        TraxStudentEntity entity = TraxStudentEntity.builder()
                .studNo("123456789 ")
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
            traxStudent.setArchiveFlag("I");
        }
        return traxStudent;
    }
}
