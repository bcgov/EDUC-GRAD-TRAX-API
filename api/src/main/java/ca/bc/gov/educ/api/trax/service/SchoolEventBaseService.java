package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.model.dto.institute.School;
import ca.bc.gov.educ.api.trax.service.institute.SchoolService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public abstract class SchoolEventBaseService<T> extends EventBaseService<T> {

    protected SchoolService schoolService;

    protected SchoolEventBaseService(@Qualifier("instituteSchoolService") SchoolService schoolService) {
        this.schoolService = schoolService;
    }

    /**
     * Acts as a filter. Add rules here for if a school event should be added to history table
     * @param school a school object
     * @return true if the school object passes eligibility for history table
     */
    protected boolean shouldCreateHistory(School school) {
        // currently only schools that can issue transcripts qualify
        return school.isCanIssueTranscripts();
    }
}
