package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.choreographer.ChoreographEventHandler;
import ca.bc.gov.educ.api.trax.constant.EventActivityCode;
import ca.bc.gov.educ.api.trax.model.dto.ChoreographedEvent;
import ca.bc.gov.educ.api.trax.model.entity.EventEntity;
import io.nats.client.Message;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.io.IOException;

import static ca.bc.gov.educ.api.trax.constant.Topics.INSTITUTE_EVENTS_TOPIC;
import static org.mockito.Mockito.*;

public class EventHandlerDelegatorServiceTest extends BaseReplicationServiceTest {

    @SpyBean
    private EventHandlerDelegatorService eventHandlerDelegatorService;

    @MockBean
    private ChoreographedEventPersistenceService choreographedEventPersistenceService;

    @MockBean
    private ChoreographEventHandler choreographer;

    private ChoreographedEvent choreographedEvent;

    @MockBean
    private Message message;

    private EventEntity eventEntity;

    @Before
    public void setUp() throws Exception {
        choreographedEvent = new ChoreographedEvent();
        eventEntity = new EventEntity();
        eventEntity.setEventType("UPDATE_SCHOOL");
        when(message.getSubject()).thenReturn(INSTITUTE_EVENTS_TOPIC.toString());
        when(choreographedEventPersistenceService.persistEventToDB(any())).thenReturn(eventEntity);
    }

    @Test
    public void whenHandleChoreographyEvent_thenVerified() throws IOException {
        this.eventHandlerDelegatorService.handleChoreographyEvent(this.choreographedEvent, this.message);
        verify(eventHandlerDelegatorService, times(1)).handleChoreographyEvent(this.choreographedEvent, this.message);
    }

    @Test
    public void testSetActivityCode() throws IOException {
        this.eventHandlerDelegatorService.handleChoreographyEvent(this.choreographedEvent, this.message);
        Assert.assertTrue(choreographedEvent.getActivityCode().equalsIgnoreCase(EventActivityCode.INSTITUTE_EVENT.toString()));
    }

}
