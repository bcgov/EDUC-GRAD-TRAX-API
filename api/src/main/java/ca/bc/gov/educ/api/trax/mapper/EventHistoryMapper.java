package ca.bc.gov.educ.api.trax.mapper;

import ca.bc.gov.educ.api.trax.constant.EventHistoryType;
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
import org.apache.commons.lang3.tuple.Pair;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

@Slf4j
@Mapper(componentModel = "spring", uses = {EventMapper.class, UUIDMapper.class})
public abstract class EventHistoryMapper {

    EducGradTraxApiConstants constants;

    @Autowired
    public void setConstants(EducGradTraxApiConstants constants){
        this.constants = constants;
    }

    @Mapping(source = "event", target = "eventHistoryUrl", qualifiedByName = "getUrlFromEventHistoryEntity")
    @Mapping(source = "event", target = "instituteId", qualifiedByName = "getInstituteIdFromEventEntity")
    public abstract EventHistory toStructure(EventHistoryEntity eventHistoryEntity);

    @Mapping(target = "event.eventPayloadBytes", ignore = true)
    public abstract EventHistoryEntity toEntity(EventHistory eventHistory);

    @Named("getInstituteIdFromEventEntity")
    UUID getInstituteIdFromEventEntity(EventEntity eventEntity){
        return this.getInstituteIdFromEvent(eventEntity).getRight();
    }

    @Named("getUrlFromEventHistoryEntity")
    String getUrlFromEventHistoryEntity(EventEntity eventEntity) {
        Pair<EventHistoryType, UUID> evenHistoryPair = this.getInstituteIdFromEvent(eventEntity);
        try {
            switch (evenHistoryPair.getLeft()) {
                case SCHOOL -> {
                    val school = JsonUtil.getJsonObjectFromString(ca.bc.gov.educ.api.trax.model.dto.institute.School.class, eventEntity.getEventPayload());
                    return this.getStudentAdminSchoolDetailsUrl(school.getSchoolId());
                }
                case DISTRICT -> {
                    val district = JsonUtil.getJsonObjectFromString(ca.bc.gov.educ.api.trax.model.dto.institute.District.class, eventEntity.getEventPayload());
                    return this.getStudentAdminDistrictDetailsUrl(district.getDistrictId());
                }
                case INDEPENDENT_AUTHORITY -> {
                    val authorityContact = JsonUtil.getJsonObjectFromString(AuthorityContact.class, eventEntity.getEventPayload());
                    return this.getStudentAdminAuthorityDetailsUrl(authorityContact.getIndependentAuthorityId());
                }
                default -> {
                    return null;
                }
            }
        } catch (final Exception exception) {
            log.error(exception.getMessage());
        }
        return null;
    }

    private Pair<EventHistoryType, UUID> getInstituteIdFromEvent(EventEntity eventEntity) {
        if (eventEntity != null) {
            try {
                switch (EventType.valueOf(eventEntity.getEventType())) {
                    case CREATE_SCHOOL, UPDATE_SCHOOL -> {
                        val school = JsonUtil.getJsonObjectFromString(ca.bc.gov.educ.api.trax.model.dto.institute.School.class, eventEntity.getEventPayload());
                        return Pair.of(EventHistoryType.SCHOOL, UUID.fromString(school.getSchoolId()));
                    }
                    case MOVE_SCHOOL -> {
                        val schoolMoved = JsonUtil.getJsonObjectFromString(MoveSchoolData.class, eventEntity.getEventPayload());
                        return Pair.of(EventHistoryType.SCHOOL, UUID.fromString(schoolMoved.getToSchool().getSchoolId()));
                    }
                    case CREATE_SCHOOL_CONTACT, UPDATE_SCHOOL_CONTACT, DELETE_SCHOOL_CONTACT -> {
                        val schoolContact = JsonUtil.getJsonObjectFromString(SchoolContact.class, eventEntity.getEventPayload());
                        return Pair.of(EventHistoryType.SCHOOL, UUID.fromString(schoolContact.getSchoolId()));
                    }
                    case CREATE_DISTRICT, UPDATE_DISTRICT -> {
                        val district = JsonUtil.getJsonObjectFromString(ca.bc.gov.educ.api.trax.model.dto.institute.District.class, eventEntity.getEventPayload());
                        return Pair.of(EventHistoryType.DISTRICT, UUID.fromString(district.getDistrictId()));
                    }
                    case CREATE_AUTHORITY_CONTACT, UPDATE_AUTHORITY_CONTACT, DELETE_AUTHORITY_CONTACT -> {
                        val authorityContact = JsonUtil.getJsonObjectFromString(AuthorityContact.class, eventEntity.getEventPayload());
                        return Pair.of(EventHistoryType.INDEPENDENT_AUTHORITY, UUID.fromString(authorityContact.getIndependentAuthorityId()));
                    }
                    case CREATE_DISTRICT_CONTACT, UPDATE_DISTRICT_CONTACT, DELETE_DISTRICT_CONTACT -> {
                        val districtContact = JsonUtil.getJsonObjectFromString(DistrictContact.class, eventEntity.getEventPayload());
                        return Pair.of(EventHistoryType.DISTRICT, UUID.fromString(districtContact.getDistrictId()));
                    }
                    default -> {
                        return null;
                    }
                }
            } catch (final Exception exception) {
                log.error(exception.getMessage());
            }
        }
        return null;
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
