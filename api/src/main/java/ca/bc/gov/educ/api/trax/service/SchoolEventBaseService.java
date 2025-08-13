package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.model.dto.GradSchool;
import ca.bc.gov.educ.api.trax.model.dto.institute.School;
import ca.bc.gov.educ.api.trax.model.dto.institute.SchoolDetail;
import ca.bc.gov.educ.api.trax.service.institute.GradSchoolService;
import ca.bc.gov.educ.api.trax.service.institute.SchoolService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public abstract class SchoolEventBaseService<T> extends EventBaseService<T> {

    protected SchoolService schoolService;
    protected GradSchoolService gradSchoolService;

    protected SchoolEventBaseService(@Qualifier("instituteSchoolService") SchoolService schoolService, GradSchoolService gradSchoolService) {
        this.schoolService = schoolService;
        this.gradSchoolService = gradSchoolService;
    }


    /**
     * Acts as a filter. Add rules here for if a school event should be added to history table
     * @param school a school object
     * @return true if the school object passes eligibility for history table
     */
    protected boolean shouldCreateHistory(School school) {
        return shouldCreateHistory(school.getSchoolId());
    }

    protected boolean shouldCreateHistory(String schoolId) {
        // currently only schools that can issue transcripts qualify
        SchoolDetail schoolDetail = this.schoolService.getSchoolDetailBySchoolIdFromRedisCache(UUID.fromString(schoolId));
        if(schoolDetail != null){
            try{
                return schoolDetail.isCanIssueTranscripts() != this.gradSchoolService.isGradSchoolTranscriptIssuer(schoolId);
            } catch (Exception e){
                log.error("Error checking if school can issue transcripts: {}", e.getMessage());
            }
        }
        return false;
    }
}
