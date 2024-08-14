package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.service.institute.SchoolService;
import ca.bc.gov.educ.api.trax.constant.EventType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SchoolContactDeletedService extends SchoolContactEventBaseService {


    @Autowired
    public SchoolContactDeletedService(SchoolService schoolService) {
        super(schoolService);
    }

    @Override
    public String getEventType() {
        return EventType.DELETE_SCHOOL_CONTACT.toString();
    }

}
