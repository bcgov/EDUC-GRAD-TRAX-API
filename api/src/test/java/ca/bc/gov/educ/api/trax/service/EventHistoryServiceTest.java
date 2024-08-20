package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.model.entity.EventEntity;
import ca.bc.gov.educ.api.trax.model.entity.EventHistoryEntity;
import ca.bc.gov.educ.api.trax.repository.EventHistoryRepository;
import ca.bc.gov.educ.api.trax.repository.EventRepository;
import ca.bc.gov.educ.api.trax.support.TestUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
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
}