package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.constant.EventType;
import ca.bc.gov.educ.api.trax.exception.ServiceException;
import ca.bc.gov.educ.api.trax.model.dto.SchoolContact;
import ca.bc.gov.educ.api.trax.model.dto.institute.SchoolDetail;
import ca.bc.gov.educ.api.trax.model.entity.EventEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ca.bc.gov.educ.api.trax.service.institute.SchoolService;

@Service
@Slf4j
public class SchoolContactUpdatedService extends SchoolEventBaseService<SchoolContact> {

    @Autowired
    public SchoolContactUpdatedService(SchoolService schoolService) {
        super(schoolService);
    }

    @Override
    public void processEvent(final SchoolContact schoolContact, EventEntity eventEntity) {
        log.debug("Processing School Contact Updated");
        try{
            SchoolDetail schoolDetail = this.schoolService.getSchoolDetailByIdFromInstituteApi(schoolContact.getSchoolId());
            schoolService.updateSchoolCache(schoolDetail);
            this.updateEvent(eventEntity, schoolDetail.isCanIssueTranscripts());
        } catch (ServiceException e) {
            // do not mark eventEntity as processed
            log.error(e.getMessage());
        }
    }

    @Override
    public String getEventType() {
        return EventType.UPDATE_SCHOOL_CONTACT.toString();
    }

}
