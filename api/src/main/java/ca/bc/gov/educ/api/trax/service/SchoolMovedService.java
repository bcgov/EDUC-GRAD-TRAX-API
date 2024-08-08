package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.constant.EventType;
import ca.bc.gov.educ.api.trax.exception.ServiceException;
import ca.bc.gov.educ.api.trax.model.dto.institute.MoveSchoolData;
import ca.bc.gov.educ.api.trax.model.entity.EventEntity;
import ca.bc.gov.educ.api.trax.service.institute.SchoolService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@Slf4j
public class SchoolMovedService extends EventBaseService<MoveSchoolData> {

    SchoolService schoolService;

    @Autowired
    public SchoolMovedService(SchoolService schoolService) {
        this.schoolService = schoolService;
    }

    @Override
    public void processEvent(final MoveSchoolData moveSchoolData, EventEntity eventEntity) {
        log.debug("Processing School Moved");
        try{
            schoolService.updateSchoolCache(Arrays.asList(moveSchoolData.getFromSchoolId(), moveSchoolData.getToSchool().getSchoolId()));
            this.updateEventWithHistory(eventEntity);
        } catch (ServiceException e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public String getEventType() {
        return EventType.MOVE_SCHOOL.toString();
    }

}
