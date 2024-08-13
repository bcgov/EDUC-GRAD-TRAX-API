package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.constant.EventType;
import ca.bc.gov.educ.api.trax.model.dto.SchoolContact;
import ca.bc.gov.educ.api.trax.model.entity.EventEntity;
import ca.bc.gov.educ.api.trax.service.institute.SchoolService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SchoolContactCreatedService extends EventBaseService<SchoolContact> {

    SchoolService schoolService;

    @Autowired
    public SchoolContactCreatedService(SchoolService schoolService) {
        this.schoolService = schoolService;
    }

    @Override
    public void processEvent(final SchoolContact schoolContact, EventEntity eventEntity) {
        log.debug("Processing School Contact Created");
        schoolService.updateSchoolCache(schoolContact.getSchoolId());
        this.updateEvent(eventEntity);
    }

    @Override
    public String getEventType() {
        return EventType.CREATE_SCHOOL_CONTACT.toString();
    }

}
