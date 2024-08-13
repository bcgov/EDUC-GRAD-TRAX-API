package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.constant.EventType;
import ca.bc.gov.educ.api.trax.exception.ServiceException;
import ca.bc.gov.educ.api.trax.model.dto.DistrictContact;
import ca.bc.gov.educ.api.trax.model.entity.EventEntity;
import ca.bc.gov.educ.api.trax.service.institute.DistrictService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DistrictContactCreatedService extends EventBaseService<DistrictContact> {

    DistrictService districtService;

    @Autowired
    public DistrictContactCreatedService(DistrictService districtService) {
        this.districtService = districtService;
    }

    @Override
    public void processEvent(final DistrictContact districtContact, EventEntity eventEntity) {
        log.debug("Processing District Contact Created");
        try {
            districtService.updateDistrictCache(districtContact.getDistrictId());
            this.updateEvent(eventEntity);
        } catch (ServiceException e) {
            // do not mark eventEntity as processed
            log.error(e.getMessage());
        }

    }

    @Override
    public String getEventType() {
        return EventType.CREATE_DISTRICT_CONTACT.toString();
    }

}
