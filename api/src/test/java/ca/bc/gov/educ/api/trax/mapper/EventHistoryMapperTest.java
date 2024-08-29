package ca.bc.gov.educ.api.trax.mapper;

import ca.bc.gov.educ.api.trax.EducGradTraxApiApplication;
import ca.bc.gov.educ.api.trax.constant.EventType;
import ca.bc.gov.educ.api.trax.model.dto.AuthorityContact;
import ca.bc.gov.educ.api.trax.model.dto.EventHistory;
import ca.bc.gov.educ.api.trax.model.dto.institute.District;
import ca.bc.gov.educ.api.trax.model.dto.institute.MoveSchoolData;
import ca.bc.gov.educ.api.trax.model.dto.institute.School;
import ca.bc.gov.educ.api.trax.model.entity.EventEntity;
import ca.bc.gov.educ.api.trax.model.entity.EventHistoryEntity;
import ca.bc.gov.educ.api.trax.support.TestUtils;
import ca.bc.gov.educ.api.trax.util.BaseEventHistoryTest;
import ca.bc.gov.educ.api.trax.util.EducGradTraxApiConstants;
import ca.bc.gov.educ.api.trax.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = { EducGradTraxApiApplication.class })
@ActiveProfiles("test")
class EventHistoryMapperTest extends BaseEventHistoryTest {

    @Autowired
    private EventHistoryMapper eventHistoryMapper;
    @Autowired
    private EducGradTraxApiConstants constants;

    @Test
    void testGetMapper_shouldNotBeNull() {
        Assertions.assertNotNull(EventMapper.mapper);
    }

    @Test
    void testToEventHistory_givenSchoolEvent_shouldReturnCorrectUrl() throws JsonProcessingException {
        School school = TestUtils.createSchool();
        Pair<String, EventHistoryEntity> pair = createUrlAndEntity("CREATE_SCHOOL", school, school.getSchoolId());
        EventHistory eventHistory = eventHistoryMapper.toStructure(pair.getRight());
        Assertions.assertEquals(true, eventHistory.getEventHistoryUrl().equals(pair.getLeft()));
    }

    @Test
    void testToEventHistory_givenMoveSchoolEvent_shouldReturnCorrectUrl() throws JsonProcessingException {
        MoveSchoolData school = TestUtils.createMoveSchoolData();
        Pair<String, EventHistoryEntity> pair = createUrlAndEntity("MOVE_SCHOOL", school, school.getToSchool().getSchoolId());
        EventHistory eventHistory = eventHistoryMapper.toStructure(pair.getRight());
        Assertions.assertEquals(true, eventHistory.getEventHistoryUrl().equals(pair.getLeft()));
    }

    @Test
    void testToEventHistory_givenCreateDistrictEvent_shouldReturnCorrectUrl() throws JsonProcessingException {
        District district = TestUtils.createDistrict();
        Pair<String, EventHistoryEntity> pair = createUrlAndEntity("CREATE_DISTRICT", district, district.getDistrictId());
        EventHistory eventHistory = eventHistoryMapper.toStructure(pair.getRight());
        Assertions.assertEquals(true, eventHistory.getEventHistoryUrl().equals(pair.getLeft()));
    }

    @Test
    void testToEventHistory_givenCreateDistrictContact_shouldReturnCorrectUrl() throws JsonProcessingException {
        District district = TestUtils.createDistrict();
        Pair<String, EventHistoryEntity> pair = createUrlAndEntity("CREATE_DISTRICT_CONTACT", district, district.getDistrictId());
        EventHistory eventHistory = eventHistoryMapper.toStructure(pair.getRight());
        Assertions.assertEquals(true, eventHistory.getEventHistoryUrl().equals(pair.getLeft()));
    }

    @Test
    void testToEventHistory_givenCreateAuthorityContact_shouldReturnCorrectUrl() throws JsonProcessingException {
        AuthorityContact authorityContact = TestUtils.createAuthorityContact();
        Pair<String, EventHistoryEntity> pair = createUrlAndEntity("CREATE_AUTHORITY_CONTACT", authorityContact, authorityContact.getIndependentAuthorityId());
        EventHistory eventHistory = eventHistoryMapper.toStructure(pair.getRight());
        Assertions.assertEquals(true, eventHistory.getEventHistoryUrl().equals(pair.getLeft()));
    }

    private Pair<String, EventHistoryEntity> createUrlAndEntity(String eventType, Object eventPayload, String id) throws JsonProcessingException {
        EventEntity eventEntity = this.createEventData();
        eventEntity.setEventType(eventType);
        eventEntity.setEventPayload(JsonUtil.getJsonStringFromObject(eventPayload));
        EventHistoryEntity eventHistoryEntity = this.createEventHistoryData(eventEntity);
        String url = String.format(resolveURL(eventType), id);
        return Pair.of(url, eventHistoryEntity);
    }

    private String resolveURL(String eventType) {
        switch (EventType.valueOf(eventType)) {
            case CREATE_SCHOOL, UPDATE_SCHOOL, MOVE_SCHOOL, CREATE_SCHOOL_CONTACT, UPDATE_SCHOOL_CONTACT, DELETE_SCHOOL_CONTACT -> {
                return constants.getStudentAdminSchoolDetailsUrl();
            }
            case CREATE_DISTRICT, UPDATE_DISTRICT, CREATE_DISTRICT_CONTACT, UPDATE_DISTRICT_CONTACT, DELETE_DISTRICT_CONTACT  -> {
                return constants.getStudentAdminDistrictDetailsUrl();
            }
            case CREATE_AUTHORITY_CONTACT, UPDATE_AUTHORITY_CONTACT, DELETE_AUTHORITY_CONTACT -> {
                return constants.getStudentAdminAuthorityDetailsUrl();
            }
            default -> {
                return null;
            }
        }
    }

}
