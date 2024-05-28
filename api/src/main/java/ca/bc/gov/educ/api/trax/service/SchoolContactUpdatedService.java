package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.constant.EventType;
import ca.bc.gov.educ.api.trax.model.dto.SchoolContact;
import ca.bc.gov.educ.api.trax.model.entity.Event;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SchoolContactUpdatedService extends EventBaseService<SchoolContact> {

    @Override
    public void processEvent(final SchoolContact districtContact, Event event) {
        log.debug("Processing School Contact Updated");
        // process the event here as per https://eccbc.atlassian.net/browse/GRAD2-2648
        this.updateEvent(event);
    }

    @Override
    public String getEventType() {
        return EventType.UPDATE_SCHOOL_CONTACT.toString();
    }

}
