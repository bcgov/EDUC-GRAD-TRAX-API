package ca.bc.gov.educ.api.trax.service;


import ca.bc.gov.educ.api.trax.exception.ServiceException;
import ca.bc.gov.educ.api.trax.model.dto.DistrictContact;
import ca.bc.gov.educ.api.trax.model.entity.EventEntity;
import ca.bc.gov.educ.api.trax.service.institute.DistrictService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public abstract class DistrictContactEventBaseService extends EventBaseService<DistrictContact> {

    protected DistrictService districtService;

    protected DistrictContactEventBaseService(DistrictService districtService) {
        this.districtService = districtService;
    }

    @Override
    public void processEvent(DistrictContact districtContact, EventEntity eventEntity) {log.debug("Processing {}", eventEntity.getEventType());
        try {
            districtService.updateDistrictCache(districtContact.getDistrictId());
            this.updateEvent(eventEntity);
        } catch (ServiceException e) {
            // do not mark eventEntity as processed
            log.error(e.getMessage());
        }
    }

}
