package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.constant.EventType;
import ca.bc.gov.educ.api.trax.service.institute.SchoolService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SchoolUpdatedService extends SchoolCreatedService {

    @Autowired
    public SchoolUpdatedService(SchoolService schoolService) {
        super(schoolService);
    }

    @Override
    public String getEventType() {
        return EventType.UPDATE_SCHOOL.toString();
    }

}
