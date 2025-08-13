package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.constant.EventType;
import ca.bc.gov.educ.api.trax.exception.ServiceException;
import ca.bc.gov.educ.api.trax.model.dto.institute.MoveSchoolData;
import ca.bc.gov.educ.api.trax.model.dto.institute.SchoolDetail;
import ca.bc.gov.educ.api.trax.model.entity.EventEntity;
import ca.bc.gov.educ.api.trax.service.institute.GradSchoolService;
import ca.bc.gov.educ.api.trax.service.institute.SchoolService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@Slf4j
public class SchoolMovedService extends SchoolEventBaseService<MoveSchoolData> {

    @Autowired
    public SchoolMovedService(@Qualifier("instituteSchoolService") SchoolService schoolService, GradSchoolService gradSchoolService) {
        super(schoolService, gradSchoolService);
    }

    @Override
    public void processEvent(final MoveSchoolData moveSchoolData, EventEntity eventEntity) {
        log.debug("Processing School Moved");
        try{
            // have to check event history eligibility on from and to schools for move.
            // if one can issue transcripts, set history eligibility
            SchoolDetail schoolDetail = this.schoolService.getSchoolDetailByIdFromInstituteApi(moveSchoolData.getFromSchoolId());
            boolean shouldCreateHistory = (moveSchoolData.getToSchool().isCanIssueTranscripts() || this.shouldCreateHistory(schoolDetail));
            schoolService.updateSchoolCache(Arrays.asList(moveSchoolData.getFromSchoolId(), moveSchoolData.getToSchool().getSchoolId()));
            this.updateEvent(eventEntity, shouldCreateHistory);
        } catch (ServiceException e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public String getEventType() {
        return EventType.MOVE_SCHOOL.toString();
    }

}
