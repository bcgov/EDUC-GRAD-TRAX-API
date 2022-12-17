package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.choreographer.ChoreographEventHandler;
import ca.bc.gov.educ.api.trax.constant.EventOutcome;
import ca.bc.gov.educ.api.trax.constant.EventType;
import ca.bc.gov.educ.api.trax.constant.Topics;
import ca.bc.gov.educ.api.trax.exception.BusinessException;
import ca.bc.gov.educ.api.trax.messaging.NatsConnection;
import ca.bc.gov.educ.api.trax.messaging.jetstream.Publisher;
import ca.bc.gov.educ.api.trax.messaging.jetstream.Subscriber;
import ca.bc.gov.educ.api.trax.model.dto.ChoreographedEvent;
import ca.bc.gov.educ.api.trax.model.entity.Event;
import ca.bc.gov.educ.api.trax.model.entity.TraxUpdatedPubEvent;
import io.nats.client.Message;
import io.nats.client.impl.NatsMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.UUID;

import static ca.bc.gov.educ.api.trax.constant.EventStatus.DB_COMMITTED;
import static org.assertj.core.api.Assertions.assertThatNoException;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class EventHandlerDelegatorServiceTest {

    @Autowired
    EventHandlerDelegatorService eventHandlerDelegatorService;

    @MockBean
    ChoreographedEventPersistenceService choreographedEventPersistenceService;

    @MockBean
    ChoreographEventHandler choreographer;

    // NATS
    @MockBean
    private NatsConnection natsConnection;

    @MockBean
    private Publisher publisher;

    @MockBean
    private Subscriber subscriber;

    @Test
    public void testProcessEvent_givenEventAndMessage() throws IOException, BusinessException {
        UUID eventId = UUID.randomUUID();

        ChoreographedEvent choreographedEvent = new ChoreographedEvent();
        choreographedEvent.setEventID(eventId);
        choreographedEvent.setEventType(EventType.GRAD_STUDENT_UPDATED);

        Event savedEvent = new Event();
        savedEvent.setEventType(EventType.GRAD_STUDENT_UPDATED.toString());
        savedEvent.setEventStatus(DB_COMMITTED.toString());
        savedEvent.setEventId(eventId);
        savedEvent.setEventOutcome(EventOutcome.GRAD_STATUS_UPDATED.toString());
        savedEvent.setReplicationEventId(UUID.randomUUID());

        Mockito.when(choreographedEventPersistenceService.persistEventToDB(choreographedEvent)).thenReturn(savedEvent);

        Message reply = NatsMessage.builder()
                .subject(Topics.GRAD_STATUS_EVENT_TOPIC.toString())
                .data(savedEvent.getEventPayloadBytes())
                .build();

        this.eventHandlerDelegatorService.handleChoreographyEvent(choreographedEvent, reply);
        assertThatNoException();
    }

    @Test
    public void testProcessEvent_givenTraxUpdatedEventAndMessage() throws IOException {
        UUID eventId = UUID.randomUUID();

        ChoreographedEvent choreographedEvent = new ChoreographedEvent();
        choreographedEvent.setEventID(eventId);
        choreographedEvent.setEventType(EventType.TRAX_STUDENT_UPDATED);

        TraxUpdatedPubEvent savedEvent = new TraxUpdatedPubEvent();
        savedEvent.setEventType(EventType.TRAX_STUDENT_UPDATED.toString());
        savedEvent.setEventStatus(DB_COMMITTED.toString());
        savedEvent.setEventId(eventId);
        savedEvent.setEventOutcome(EventOutcome.TRAX_STUDENT_MASTER_UPDATED.toString());

        Mockito.doNothing().when(choreographedEventPersistenceService).updateEventStatus(choreographedEvent);

        Message reply = NatsMessage.builder()
                .subject(Topics.TRAX_UPDATE_EVENT_TOPIC.toString())
                .data(savedEvent.getEventPayloadBytes())
                .build();


        this.eventHandlerDelegatorService.handleChoreographyEvent(choreographedEvent, reply);
        assertThatNoException();
    }

}
