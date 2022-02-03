package ca.bc.gov.educ.api.trax.support;

import ca.bc.gov.educ.api.trax.model.entity.Event;
import ca.bc.gov.educ.api.trax.model.dto.GraduationStatus;
import ca.bc.gov.educ.api.trax.repository.EventRepository;
import ca.bc.gov.educ.api.trax.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.time.LocalDateTime;
import java.util.UUID;

import static ca.bc.gov.educ.api.trax.util.EducGradTraxApiConstants.DEFAULT_CREATED_BY;
import static ca.bc.gov.educ.api.trax.util.EducGradTraxApiConstants.DEFAULT_UPDATED_BY;
import static ca.bc.gov.educ.api.trax.constant.EventStatus.DB_COMMITTED;

public class TestUtils {
    public static GraduationStatus createGraduationStatus() {
        GraduationStatus graduationStatus = new GraduationStatus();
        graduationStatus.setPen("123456789 ");
        graduationStatus.setStudentID(UUID.randomUUID());
        graduationStatus.setProgram("2018-EN");
        graduationStatus.setStudentStatus("A");
        graduationStatus.setRecalculateGradStatus("Y");
        graduationStatus.setSchoolOfRecord("111222333");
        return graduationStatus;
    }

    public static Event createEvent(String eventType, Object payload, EventRepository eventRepository) throws JsonProcessingException {
        var event = Event.builder()
                .eventType(eventType)
                .eventId(UUID.randomUUID())
                .eventOutcome("PROCESSED")
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
}
