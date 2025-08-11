package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.constant.EventType;
import ca.bc.gov.educ.api.trax.service.institute.SchoolService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GradSchoolUpdatedService extends GradSchoolCreatedService {

    @Autowired
    public GradSchoolUpdatedService(SchoolService schoolService) {
        super(schoolService);
    }

    @Override
    public String getEventType() {
        return EventType.UPDATE_GRAD_SCHOOL.toString();
    }

}
