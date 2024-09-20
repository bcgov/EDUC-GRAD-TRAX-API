package ca.bc.gov.educ.api.trax.mapper;

import ca.bc.gov.educ.api.trax.constant.EventType;
import ca.bc.gov.educ.api.trax.model.dto.AuthorityContact;
import ca.bc.gov.educ.api.trax.model.dto.DistrictContact;
import ca.bc.gov.educ.api.trax.model.dto.EventHistory;
import ca.bc.gov.educ.api.trax.model.dto.SchoolContact;
import ca.bc.gov.educ.api.trax.model.dto.institute.MoveSchoolData;
import ca.bc.gov.educ.api.trax.model.entity.EventEntity;
import ca.bc.gov.educ.api.trax.model.entity.EventHistoryEntity;
import ca.bc.gov.educ.api.trax.util.EducGradTraxApiConstants;
import ca.bc.gov.educ.api.trax.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@Mapper(componentModel = "spring", uses = {EventMapper.class, UUIDMapper.class})
public abstract class EventHistoryMapper {

    EducGradTraxApiConstants constants;

    @Autowired
    public void setConstants(EducGradTraxApiConstants constants){
        this.constants = constants;
    }

    @Mapping(source = "event", target = "eventHistoryUrl", qualifiedByName = "getUrlFromEventHistoryEntity")
    public abstract EventHistory toStructure(EventHistoryEntity eventHistoryEntity);

    @Mapping(target = "event.eventPayloadBytes", ignore = true)
    public abstract EventHistoryEntity toEntity(EventHistory eventHistory);

    @Named("getUrlFromEventHistoryEntity")
    String getUrlFromEventHistoryEntity(EventEntity eventEntity) {
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
                log.error(exception.getMessage());
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
