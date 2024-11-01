package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.exception.TraxAPIRuntimeException;
import ca.bc.gov.educ.api.trax.model.entity.EventEntity;
import ca.bc.gov.educ.api.trax.model.entity.EventHistoryEntity;
import ca.bc.gov.educ.api.trax.repository.EventHistoryRepository;
import ca.bc.gov.educ.api.trax.repository.EventRepository;
import ca.bc.gov.educ.api.trax.support.TestUtils;
import ca.bc.gov.educ.api.trax.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EventHistoryServiceTest extends BaseReplicationServiceTest {
    @Autowired
    private EventHistoryService eventHistoryService;
    final LocalDateTime purgeTimeInDays = LocalDateTime.now().minusDays(2);

    @Test
    public void testDeleteEvent_giventEventHistory_ShouldCascadeDelete() throws JsonProcessingException {
        // set up
        EventRepository eventRepository = this.replicationTestUtils.getEventRepository();
        EventHistoryRepository eventHistoryRepository = this.replicationTestUtils.getEventHistoryRepository();
        var event = TestUtils.createEvent("DELETE_DISTRICT_CONTACT", TestUtils.createDistrictContact(), LocalDateTime.now(), eventRepository);
        var eventHistory = TestUtils.createEventHistory(event, LocalDateTime.now(), eventHistoryRepository);
        eventRepository.deleteById(event.getReplicationEventId());
        Optional<EventEntity> eventThatShouldBePurgedOptional = eventRepository.findById(event.getReplicationEventId());
        Optional<EventHistoryEntity> eventHistoryThatShouldBePurgedAlso = eventHistoryRepository.findById(eventHistory.getId());
        Assert.assertTrue(eventHistoryThatShouldBePurgedAlso.isEmpty() && eventThatShouldBePurgedOptional.isEmpty());
    }

    @Test
    public void purgeOldEventAndEventHistoryRecords_givenNoExceptionAndOldRecord_shouldPurgeRecords() throws JsonProcessingException {
        // set up
        EventRepository eventRepository = this.replicationTestUtils.getEventRepository();
        EventHistoryRepository eventHistoryRepository = this.replicationTestUtils.getEventHistoryRepository();
        var eventAge = LocalDateTime.now().minusDays(3);
        var eventThatShouldBePurged = TestUtils.createEvent("DELETE_DISTRICT_CONTACT", TestUtils.createDistrictContact(), eventAge, eventRepository);
        // set up event history for eventThatShouldBePurged
        var eventHistory = TestUtils.createEventHistory(eventThatShouldBePurged, eventAge, eventHistoryRepository);
        // call purge
        eventHistoryService.purgeOldEventAndEventHistoryRecords(purgeTimeInDays);
        // check repo and ensure that older record purged
        Optional<EventEntity> eventThatShouldBePurgedOptional = eventRepository.findById(eventThatShouldBePurged.getReplicationEventId());
        Optional<EventHistoryEntity> eventHistoryThatShouldBePurgedAlso = eventHistoryRepository.findById(eventHistory.getId());
        Assert.assertTrue(eventHistoryThatShouldBePurgedAlso.isEmpty() && eventThatShouldBePurgedOptional.isEmpty());
    }

    @Test
    public void purgeOldEventAndEventHistoryRecords_givenNoExceptionAndNewRecord_shouldNotPurgeRecords() throws JsonProcessingException {
        EventRepository eventRepository = this.replicationTestUtils.getEventRepository();
        EventHistoryRepository eventHistoryRepository = this.replicationTestUtils.getEventHistoryRepository();
        var eventThatShouldNotBePurged = TestUtils.createEvent("DELETE_DISTRICT_CONTACT", TestUtils.createDistrictContact(), eventRepository);
        var eventHistory = TestUtils.createEventHistory(eventThatShouldNotBePurged, LocalDateTime.now(), eventHistoryRepository);
        // call purge
        eventHistoryService.purgeOldEventAndEventHistoryRecords(purgeTimeInDays);
        // check repo and ensure that new record is not purged
        Optional<EventEntity> eventThatShouldNotBePurgedOptional = eventRepository.findById(eventThatShouldNotBePurged.getReplicationEventId());
        Optional<EventHistoryEntity> eventHistoryThatShouldNotBePurgedAlso = eventHistoryRepository.findById(eventHistory.getId());
        Assert.assertTrue(eventHistoryThatShouldNotBePurgedAlso.isPresent() && eventThatShouldNotBePurgedOptional.isPresent());
    }

    @Test
    public void setSpecificationAndSortCriteria_givenValidData_shouldReturnOk() throws TraxAPIRuntimeException{
        String sort = "{ \"schoolNumber\": \"ASC\" }";
        String searchParams = "[{\"condition\":null,\"searchCriteriaList\":[{\"key\":\"openedDate\",\"operation\":\"lte\",\"value\":\"2024-08-26T09:05:51.782\",\"valueType\":\"DATE_TIME\",\"condition\":\"AND\"},{\"key\":\"strAnd\",\"operation\":\"eq\",\"value\":\"Test String\",\"valueType\":\"STRING\",\"condition\":\"AND\"},{\"key\":\"longOr\",\"operation\":\"gt\",\"value\":\"1230\",\"valueType\":\"LONG\",\"condition\":\"OR\"},{\"key\":\"intOr\",\"operation\":\"gte\",\"value\":\"12\",\"valueType\":\"INTEGER\",\"condition\":\"OR\"},{\"key\":\"dateAnd\",\"operation\":\"eq\",\"value\":\"2024-08-26\",\"valueType\":\"DATE\",\"condition\":\"AND\"},{\"key\":\"uuidOr\",\"operation\":\"eq\",\"value\":\"6f84aa52-ad90-4f04-be66-04614ed24c37\",\"valueType\":\"UUID\",\"condition\":\"OR\"}]}]";
        Specification<EventHistoryEntity> eventHistorySpecs = eventHistoryService.setSpecificationAndSortCriteria(sort, searchParams, JsonUtil.mapper, new ArrayList<>());
        Assert.assertNotNull(eventHistorySpecs);
    }

    @Test
    public void setSpecificationAndSortCriteria_givenInvalisData_shouldThrowTraxAPIRuntimeException() {
        final List<Sort.Order> sorts = new ArrayList<>();
        Assert.assertThrows(TraxAPIRuntimeException.class, () -> eventHistoryService.setSpecificationAndSortCriteria(null, "{ \"bunkjunk\": \"ASC\" }", JsonUtil.mapper, sorts));
    }
}