package ca.bc.gov.educ.api.trax.mapper;

import ca.bc.gov.educ.api.trax.constant.EventType;
import ca.bc.gov.educ.api.trax.model.dto.*;
import ca.bc.gov.educ.api.trax.model.dto.institute.MoveSchoolData;
import ca.bc.gov.educ.api.trax.model.entity.EventEntity;
import ca.bc.gov.educ.api.trax.model.entity.EventHistoryEntity;
import ca.bc.gov.educ.api.trax.util.EducGradTraxApiConstants;
import ca.bc.gov.educ.api.trax.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

@Slf4j
public abstract class EventHistoryDecorator implements EventHistoryMapper {


    private final EventHistoryMapper delegate;

    EducGradTraxApiConstants constants;

    protected EventHistoryDecorator(EventHistoryMapper delegate) {
        this.delegate = delegate;
    }

    @Autowired
    public void setConstants(EducGradTraxApiConstants constants){
        this.constants = constants;
    }

    @Override
    public EventHistory toStructure(EventHistoryEntity eventHistoryEntity) {
        EventHistory eventHistory = this.delegate.toStructure(eventHistoryEntity);
        eventHistory.setEventHistoryUrl(this.getUrlFromEventHistoryEntity(eventHistoryEntity));
        return eventHistory;
    }

    private String getUrlFromEventHistoryEntity(EventHistoryEntity eventHistoryEntity) {
        EventEntity eventEntity = eventHistoryEntity.getEvent();
        String url = null;
        if (eventEntity != null) {
            try {
                switch (EventType.valueOf(eventEntity.getEventType())) {
                    case CREATE_SCHOOL, UPDATE_SCHOOL -> {
                        val school = JsonUtil.getJsonObjectFromString(ca.bc.gov.educ.api.trax.model.dto.institute.School.class, eventEntity.getEventPayload());
                        url = this.getStudentAdminSchoolDetailsUrl(school.getSchoolId());
                    }
                    case MOVE_SCHOOL -> {
                        val schoolMoved = JsonUtil.getJsonObjectFromString(MoveSchoolData.class, eventEntity.getEventPayload());
                        url = this.getStudentAdminSchoolDetailsUrl(schoolMoved.getToSchool().getSchoolId());
                    }
                    case CREATE_SCHOOL_CONTACT, UPDATE_SCHOOL_CONTACT, DELETE_SCHOOL_CONTACT -> {
                        val schoolContact = JsonUtil.getJsonObjectFromString(SchoolContact.class, eventEntity.getEventPayload());
                        url = this.getStudentAdminSchoolDetailsUrl(schoolContact.getSchoolId());
                    }
                    case CREATE_DISTRICT, UPDATE_DISTRICT -> {
                        val district = JsonUtil.getJsonObjectFromString(ca.bc.gov.educ.api.trax.model.dto.institute.District.class, eventEntity.getEventPayload());
                        url = this.getStudentAdminDistrictDetailsUrl(district.getDistrictId());
                    }
                    case CREATE_AUTHORITY_CONTACT, UPDATE_AUTHORITY_CONTACT, DELETE_AUTHORITY_CONTACT -> {
                        val authorityContact = JsonUtil.getJsonObjectFromString(AuthorityContact.class, eventEntity.getEventPayload());
                        url = this.getStudentAdminAuthorityDetailsUrl(authorityContact.getIndependentAuthorityId());
                    }
                    case CREATE_DISTRICT_CONTACT, UPDATE_DISTRICT_CONTACT, DELETE_DISTRICT_CONTACT -> {
                        val districtContact = JsonUtil.getJsonObjectFromString(DistrictContact.class, eventEntity.getEventPayload());
                        url = this.getStudentAdminDistrictDetailsUrl(districtContact.getDistrictId());
                    }
                    default -> {
                        return null;
                    }
                }
            } catch (final Exception exception) {
                log.error("Exception while processing eventEntity :: {}", eventEntity, exception);
            }
        }
    return url;

    }

    private String getStudentAdminSchoolDetailsUrl(String schoolId) {
        return String.format(constants.getStudentAdminSchoolDetailsUrl(), schoolId);
    }

    private String getStudentAdminAuthorityDetailsUrl(String authorityId) {
        return String.format(constants.getStudentAdminAuthorityDetailsUrl(), authorityId);
    }

    private String getStudentAdminDistrictDetailsUrl(String districtId) {
        return String.format(constants.getStudentAdminDistrictDetailsUrl(), districtId);
    }

}
