package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.constant.EventType;
import ca.bc.gov.educ.api.trax.service.institute.DistrictService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DistrictContactCreatedService extends DistrictContactEventBaseService {

    @Autowired
    public DistrictContactCreatedService(DistrictService districtService) {
        super(districtService);
    }

    @Override
    public String getEventType() {
        return EventType.CREATE_DISTRICT_CONTACT.toString();
    }

}
