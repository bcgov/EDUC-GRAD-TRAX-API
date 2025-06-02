package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.choreographer.ChoreographEventHandler;
import ca.bc.gov.educ.api.trax.constant.EventActivityCode;
import ca.bc.gov.educ.api.trax.messaging.NatsConnection;
import ca.bc.gov.educ.api.trax.messaging.jetstream.Publisher;
import ca.bc.gov.educ.api.trax.messaging.jetstream.Subscriber;
import ca.bc.gov.educ.api.trax.model.dto.ChoreographedEvent;
import ca.bc.gov.educ.api.trax.model.entity.EventEntity;
import io.nats.client.Message;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import redis.clients.jedis.JedisCluster;


import java.io.IOException;
import java.util.Optional;

import static ca.bc.gov.educ.api.trax.constant.Topics.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(OutputCaptureExtension.class)
class EventHandlerDelegatorServiceTest {

    @SpyBean
    private EventHandlerDelegatorService eventHandlerDelegatorService;
    @MockBean
    private ChoreographedEventPersistenceService choreographedEventPersistenceService;
    @MockBean
    private ChoreographEventHandler choreographer;
    @MockBean
    private Message message;
    @MockBean
    private JedisConnectionFactory jedisConnectionFactoryMock;
    @MockBean
    private JedisCluster jedisClusterMock;
    @MockBean
    public Publisher publisher;
    @MockBean
    public Subscriber subscriber;
    @MockBean
    public NatsConnection natsConnection;
    @MockBean
    public ClientRegistrationRepository clientRegistrationRepository;
    @MockBean
    public OAuth2AuthorizedClientRepository oAuth2AuthorizedClientRepository;
    @MockBean
    public OAuth2AuthorizedClientService oAuth2AuthorizedClientService;
    private EventEntity eventEntity;
    private ChoreographedEvent choreographedEvent;

    private void setUpDefaultEvent() {
        choreographedEvent = new ChoreographedEvent();
        eventEntity = new EventEntity();
        eventEntity.setEventType("UPDATE_SCHOOL");
        when(message.getSubject()).thenReturn(INSTITUTE_EVENTS_TOPIC.toString());
        when(choreographedEventPersistenceService.persistEventToDB(any())).thenReturn(eventEntity);
    }

    @Test
    void whenHandleChoreographyEvent_thenVerified() throws IOException {
        setUpDefaultEvent();
        this.eventHandlerDelegatorService.handleChoreographyEvent(this.choreographedEvent, this.message);
        verify(eventHandlerDelegatorService, times(1)).handleChoreographyEvent(this.choreographedEvent, this.message);
    }

    @Test
    void testSetActivityCode_givenINSTITUTE_EVENT_shouldSetINSTITUTE_EVENTActivityCode() throws IOException {
        setUpDefaultEvent();
        this.eventHandlerDelegatorService.handleChoreographyEvent(this.choreographedEvent, this.message);
        assertTrue(choreographedEvent.getActivityCode().equalsIgnoreCase(EventActivityCode.INSTITUTE_EVENT.toString()));
    }

    @Test
    void testSetActivityCode_givenPEN_EVENT_shouldSetPEN_EVENTActivityCode() throws IOException {
        setUpDefaultEvent();
        when(message.getSubject()).thenReturn(PEN_EVENTS_TOPIC.toString());
        this.eventHandlerDelegatorService.handleChoreographyEvent(this.choreographedEvent, this.message);
        assertTrue(choreographedEvent.getActivityCode().equalsIgnoreCase(EventActivityCode.PEN_EVENT.toString()));
    }

    @Test
    void testSetActivityCode_givenCOREG_EVENT_shouldSetCOREG_EVENTActivityCode() throws IOException {
        setUpDefaultEvent();
        when(message.getSubject()).thenReturn(COREG_EVENTS_TOPIC.toString());
        this.eventHandlerDelegatorService.handleChoreographyEvent(this.choreographedEvent, this.message);
        assertTrue(choreographedEvent.getActivityCode().equalsIgnoreCase(EventActivityCode.COREG_EVENT.toString()));
    }

    @Test
    void testSetActivityCode_givenInvalidCode_shouldLogError(CapturedOutput capturedOutput) throws IOException {
        setUpDefaultEvent();
        final String topic = "SILLY_EVENTS_TOPIC";
        final String errorMessage = String.format("%s is not a valid topic", topic);
        when(message.getSubject()).thenReturn(topic);
        this.eventHandlerDelegatorService.handleChoreographyEvent(this.choreographedEvent, this.message);
        assertTrue(capturedOutput.getAll().contains(errorMessage));
    }

    @Test
    void testSetActivityCode_givenTRAX_UPDATE_EVENT() throws IOException {
        choreographedEvent = new ChoreographedEvent();
        eventEntity = new EventEntity();
        eventEntity.setEventType("UPDATE_SCHOOL");
        when(message.getSubject()).thenReturn(TRAX_UPDATE_EVENT_TOPIC.toString());
        this.eventHandlerDelegatorService.handleChoreographyEvent(this.choreographedEvent, this.message);
        verify(eventHandlerDelegatorService, times(1)).handleChoreographyEvent(this.choreographedEvent, this.message);
    }

    @Test
    void testSetActivityCode_givenCOREG_EVENTS_TOPIC_duplicateEvent() throws IOException {
        choreographedEvent = new ChoreographedEvent();
        eventEntity = new EventEntity();
        eventEntity.setEventType("UPDATE_SCHOOL");
        when(message.getSubject()).thenReturn(COREG_EVENTS_TOPIC.toString());
        when(choreographedEventPersistenceService.eventExistsInDB(any())).thenReturn(Optional.of(eventEntity));
        this.eventHandlerDelegatorService.handleChoreographyEvent(this.choreographedEvent, this.message);
        verify(eventHandlerDelegatorService, times(1)).handleChoreographyEvent(this.choreographedEvent, this.message);
    }


}
