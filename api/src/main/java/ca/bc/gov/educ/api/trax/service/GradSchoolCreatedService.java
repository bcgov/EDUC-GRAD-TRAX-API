package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.constant.EventType;
import ca.bc.gov.educ.api.trax.exception.ServiceException;
import ca.bc.gov.educ.api.trax.model.dto.GradSchool;
import ca.bc.gov.educ.api.trax.model.dto.institute.School;
import ca.bc.gov.educ.api.trax.model.entity.EventEntity;
import ca.bc.gov.educ.api.trax.service.institute.SchoolService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GradSchoolCreatedService extends SchoolEventBaseService<GradSchool> {

    @Autowired
    public GradSchoolCreatedService(SchoolService schoolService) {
        super(schoolService);
    }

    @Override
    public void processEvent(final GradSchool gradSchool, EventEntity eventEntity) {
        log.debug("Processing GRAD School Created");
        try{
            schoolService.updateSchoolCache(gradSchool.getSchoolID());
            this.updateEvent(eventEntity, false);
        } catch (ServiceException e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public String getEventType() {
        return EventType.CREATE_GRAD_SCHOOL.toString();
    }

}
