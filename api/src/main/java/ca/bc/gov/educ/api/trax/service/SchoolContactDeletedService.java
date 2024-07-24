package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.constant.EventType;
import ca.bc.gov.educ.api.trax.model.dto.SchoolContact;
import ca.bc.gov.educ.api.trax.model.entity.EventEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SchoolContactDeletedService extends EventBaseService<SchoolContact> {

    @Override
    public void processEvent(final SchoolContact districtContact, EventEntity eventEntity) {
        log.debug("Processing School Contact Deleted");
        // process the eventEntity here as per https://eccbc.atlassian.net/browse/GRAD2-2648
        this.updateEvent(eventEntity);
    }

    @Override
    public String getEventType() {
        return EventType.DELETE_SCHOOL_CONTACT.toString();
    }

}
