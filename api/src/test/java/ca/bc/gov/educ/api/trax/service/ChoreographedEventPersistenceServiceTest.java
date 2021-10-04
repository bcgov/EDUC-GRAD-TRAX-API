package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.constant.EventOutcome;
import ca.bc.gov.educ.api.trax.constant.EventType;
import ca.bc.gov.educ.api.trax.exception.BusinessException;
import ca.bc.gov.educ.api.trax.messaging.NatsConnection;
import ca.bc.gov.educ.api.trax.messaging.jetstream.Subscriber;
import ca.bc.gov.educ.api.trax.model.dto.ChoreographedEvent;
import ca.bc.gov.educ.api.trax.model.entity.Event;
import ca.bc.gov.educ.api.trax.repository.EventRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;
import java.util.UUID;

import static ca.bc.gov.educ.api.trax.constant.EventStatus.DB_COMMITTED;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class ChoreographedEventPersistenceServiceTest {

    @Autowired
    ChoreographedEventPersistenceService choreographedEventPersistenceService;

    @MockBean
    EventRepository eventRepository;

    // NATS
    @MockBean
    private NatsConnection natsConnection;

    @MockBean
    private Subscriber subscriber;

    @Test(expected = BusinessException.class)
    public void testPersistEventToDB_givenTheExistingEvent() throws BusinessException {
        UUID eventId = UUID.randomUUID();

        ChoreographedEvent choreographedEvent = new ChoreographedEvent();
        choreographedEvent.setEventID(eventId);
        choreographedEvent.setEventType(EventType.UPDATE_GRAD_STATUS);
        choreographedEvent.setEventOutcome(EventOutcome.GRAD_STATUS_UPDATED);

        Event event = new Event();
        event.setEventType(EventType.UPDATE_GRAD_STATUS.toString());
        event.setEventStatus(DB_COMMITTED.toString());
        event.setEventId(eventId);
        event.setEventOutcome(EventOutcome.GRAD_STATUS_UPDATED.toString());
        event.setReplicationEventId(UUID.randomUUID());

        Mockito.when(eventRepository.findByEventId(eventId)).thenReturn(Optional.of(event));

        choreographedEventPersistenceService.persistEventToDB(choreographedEvent);
    }

    @Test
    public void testPersistEventToDB_givenTheNewEvent() throws BusinessException {
        UUID eventId = UUID.randomUUID();

        ChoreographedEvent choreographedEvent = new ChoreographedEvent();
        choreographedEvent.setEventID(eventId);
        choreographedEvent.setEventType(EventType.UPDATE_GRAD_STATUS);
        choreographedEvent.setEventOutcome(EventOutcome.GRAD_STATUS_UPDATED);
        choreographedEvent.setEventPayload("{ test: 'event'}");

        Event event = new Event();
        event.setEventType(EventType.UPDATE_GRAD_STATUS.toString());
        event.setEventStatus(DB_COMMITTED.toString());
        event.setEventId(eventId);
        event.setEventOutcome(EventOutcome.GRAD_STATUS_UPDATED.toString());
        event.setReplicationEventId(UUID.randomUUID());

        Mockito.when(eventRepository.findByEventId(eventId)).thenReturn(Optional.empty());
        Mockito.when(eventRepository.save(event)).thenReturn(event);

        choreographedEventPersistenceService.persistEventToDB(choreographedEvent);
    }

}