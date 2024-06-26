package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.constant.EventType;
import ca.bc.gov.educ.api.trax.exception.ServiceException;
import ca.bc.gov.educ.api.trax.model.dto.SchoolContact;
import ca.bc.gov.educ.api.trax.model.entity.Event;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ca.bc.gov.educ.api.trax.service.institute.SchoolService;

@Service
@Slf4j
public class SchoolContactUpdatedService extends EventBaseService<SchoolContact> {

    SchoolService schoolService;

    @Autowired
    public SchoolContactUpdatedService(SchoolService schoolService) {
        this.schoolService = schoolService;
    }

    @Override
    public void processEvent(final SchoolContact schoolContact, Event event) {
        log.debug("Processing School Contact Updated");
        // process the event here as per https://eccbc.atlassian.net/browse/GRAD2-2648
        try{
            schoolService.updateSchoolCache(schoolContact.getSchoolId());
            this.updateEvent(event);
        } catch (ServiceException e) {
            // do not mark event as processed
            log.error(e.getMessage());
        }
    }

    @Override
    public String getEventType() {
        return EventType.UPDATE_SCHOOL_CONTACT.toString();
    }

}
