package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.constant.EventType;
import ca.bc.gov.educ.api.trax.exception.ServiceException;
import ca.bc.gov.educ.api.trax.model.dto.DistrictContact;
import ca.bc.gov.educ.api.trax.model.entity.EventEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ca.bc.gov.educ.api.trax.service.institute.DistrictService;

@Service
@Slf4j
public class DistrictContactUpdatedService extends EventBaseService<DistrictContact> {

    private final DistrictService districtService;

    @Autowired
    public DistrictContactUpdatedService(DistrictService districtService) {
        super();
        this.districtService = districtService;
    }

    @Override
    public void processEvent(final DistrictContact districtContact, EventEntity eventEntity) {
        log.debug("Processing District Contact Deleted");
        try{
            districtService.updateDistrictCache(districtContact.getDistrictId());
            this.updateEvent(eventEntity, true);
        } catch (ServiceException e) {
            // do not mark eventEntity as processed
            log.error(e.getMessage());
        }

    }

    @Override
    public String getEventType() {
        return EventType.UPDATE_DISTRICT_CONTACT.toString();
    }

}
