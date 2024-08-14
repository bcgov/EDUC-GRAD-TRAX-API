package ca.bc.gov.educ.api.trax.service;


import ca.bc.gov.educ.api.trax.exception.ServiceException;
import ca.bc.gov.educ.api.trax.model.dto.SchoolContact;
import ca.bc.gov.educ.api.trax.model.entity.EventEntity;
import ca.bc.gov.educ.api.trax.service.institute.SchoolService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public abstract class SchoolContactEventBaseService extends EventBaseService<SchoolContact> {

    protected SchoolService schoolService;

    protected SchoolContactEventBaseService(SchoolService schoolService) {
        this.schoolService = schoolService;
    }

    @Override
    public void processEvent(SchoolContact schoolContact, EventEntity eventEntity) {
        log.debug("Processing {}", eventEntity.getEventType());
        try {
            schoolService.updateSchoolCache(schoolContact.getSchoolId());
            this.updateEvent(eventEntity);
        } catch (ServiceException e) {
            // do not mark eventEntity as processed
            log.error(e.getMessage());
        }
    }

}
