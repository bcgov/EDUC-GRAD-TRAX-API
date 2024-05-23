package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.constant.EventType;
import ca.bc.gov.educ.api.trax.constant.FieldType;
import ca.bc.gov.educ.api.trax.model.dto.GradStatusEventPayloadDTO;
import ca.bc.gov.educ.api.trax.model.dto.SchoolContact;
import ca.bc.gov.educ.api.trax.model.entity.Event;
import ca.bc.gov.educ.api.trax.model.entity.TraxStudentEntity;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class SchoolContactDeletedService extends EventCommonService<SchoolContact> {

    @Override
    public void processEvent(final SchoolContact districtContact, Event event) {
        log.debug("Processing School Contact Deleted");
        // TODO: process the event here as per https://eccbc.atlassian.net/browse/GRAD2-2648
        this.updateEvent(event);
    }

    @Override
    public String getEventType() {
        return EventType.DELETE_SCHOOL_CONTACT.toString();
    }

    @Override
    public void specialHandlingOnUpdateFieldsMap(Map<String, Pair<FieldType, Object>> updateFieldsMap, TraxStudentEntity traxStudentEntity, GradStatusEventPayloadDTO gradStatusUpdate) {

    }

    @Override
    public EntityManager getEntityManager() {
        return null;
    }
}
