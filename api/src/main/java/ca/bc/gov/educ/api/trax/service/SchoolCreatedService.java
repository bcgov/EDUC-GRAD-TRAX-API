package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.constant.EventType;
import ca.bc.gov.educ.api.trax.exception.ServiceException;
import ca.bc.gov.educ.api.trax.model.dto.institute.School;
import ca.bc.gov.educ.api.trax.model.entity.EventEntity;
import ca.bc.gov.educ.api.trax.service.institute.GradSchoolService;
import ca.bc.gov.educ.api.trax.service.institute.SchoolService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SchoolCreatedService extends SchoolEventBaseService<School> {

    @Autowired
    public SchoolCreatedService(@Qualifier("instituteSchoolService") SchoolService schoolService, GradSchoolService gradSchoolService) {
        super(schoolService, gradSchoolService);
    }

    @Override
    public void processEvent(final School school, EventEntity eventEntity) {
        log.debug("Processing School Created");
        try{
            boolean shouldCreateHistory = this.shouldCreateHistory(school);
            schoolService.updateSchoolCache(school.getSchoolId());
            this.updateEvent(eventEntity, shouldCreateHistory);
        } catch (ServiceException e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public String getEventType() {
        return EventType.CREATE_SCHOOL.toString();
    }

}
