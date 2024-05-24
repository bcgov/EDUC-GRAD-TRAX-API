package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.model.dto.ChoreographedEvent;
import io.nats.client.Message;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.io.IOException;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class EventHandlerDelegatorServiceTest extends BaseReplicationServiceTest {

    @MockBean
    private EventHandlerDelegatorService eventHandlerDelegatorService;

    @MockBean
    private ChoreographedEvent choreographedEvent;

    @MockBean
    private Message message;

    @Test
    public void whenHandleChoreographyEvent_thenVerified() throws IOException {
        this.eventHandlerDelegatorService.handleChoreographyEvent(this.choreographedEvent, this.message);
        verify(eventHandlerDelegatorService, times(1)).handleChoreographyEvent(this.choreographedEvent, this.message);
    }

}
