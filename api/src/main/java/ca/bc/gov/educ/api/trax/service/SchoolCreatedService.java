package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.constant.EventType;
import ca.bc.gov.educ.api.trax.exception.ServiceException;
import ca.bc.gov.educ.api.trax.model.dto.institute.School;
import ca.bc.gov.educ.api.trax.model.entity.EventEntity;
import ca.bc.gov.educ.api.trax.service.institute.SchoolService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SchoolCreatedService extends SchoolEventBaseService<School> {

    @Autowired
    public SchoolCreatedService(SchoolService schoolService) {
        super(schoolService);
    }

    @Override
    public void processEvent(final School school, EventEntity eventEntity) {
        log.debug("Processing School Created");
        try{
            schoolService.updateSchoolCache(school.getSchoolId());
            this.updateEvent(eventEntity, this.shouldCreateHistory(school));
        } catch (ServiceException e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public String getEventType() {
        return EventType.CREATE_SCHOOL.toString();
    }

}
