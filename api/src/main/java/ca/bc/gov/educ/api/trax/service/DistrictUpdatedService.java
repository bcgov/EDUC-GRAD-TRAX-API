package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.constant.EventType;
import ca.bc.gov.educ.api.trax.exception.ServiceException;
import ca.bc.gov.educ.api.trax.model.dto.institute.District;
import ca.bc.gov.educ.api.trax.model.entity.EventEntity;
import ca.bc.gov.educ.api.trax.service.institute.DistrictService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DistrictUpdatedService extends EventBaseService<District> {

    DistrictService districtService;

    @Autowired
    public DistrictUpdatedService(DistrictService districtService) {
        this.districtService = districtService;
    }

    @Override
    public void processEvent(final District district, EventEntity eventEntity) {
        log.debug("Processing District Updated");
        try{
            districtService.updateDistrictCache(district.getDistrictId());
            this.updateEventWithHistory(eventEntity);
        } catch (ServiceException e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public String getEventType() {
        return EventType.UPDATE_DISTRICT.toString();
    }

}
