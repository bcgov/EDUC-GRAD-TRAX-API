package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.choreographer.ChoreographEventHandler;
import ca.bc.gov.educ.api.trax.exception.BusinessException;
import ca.bc.gov.educ.api.trax.model.dto.ChoreographedEvent;
import io.nats.client.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static ca.bc.gov.educ.api.trax.constant.Topics.TRAX_UPDATE_EVENT_TOPIC;

@Service
@Slf4j
public class EventHandlerDelegatorService {
    private final ChoreographedEventPersistenceService choreographedEventPersistenceService;
    private final ChoreographEventHandler choreographer;

    /**
     * Instantiates a new Event handler delegator service.
     *
     * @param choreographedEventPersistenceService the choreographed event persistence service
     * @param choreographer                        the choreographer
     */
    @Autowired
    public EventHandlerDelegatorService(final ChoreographedEventPersistenceService choreographedEventPersistenceService, final ChoreographEventHandler choreographer) {
        this.choreographedEventPersistenceService = choreographedEventPersistenceService;
        this.choreographer = choreographer;
    }

    /**
     * this method will do the following.
     * 1. Call service to store the event in oracle DB.
     * 2. Acknowledge to STAN only when the service call is completed. since it uses manual acknowledgement.
     * 3. Hand off the task to update RDB onto a different executor.
     *
     * @param choreographedEvent the choreographed event
     * @param message            the message
     * @throws IOException the io exception
     */
    public void handleChoreographyEvent(@NonNull final ChoreographedEvent choreographedEvent, final Message message) throws IOException {
        try {
            if (message.getSubject().equalsIgnoreCase(TRAX_UPDATE_EVENT_TOPIC.toString())) {
                this.choreographedEventPersistenceService.updateEventStatus(choreographedEvent);
                message.ack();
                log.debug("acknowledged to Jet Stream for TRAX UPDATE EVENT sent...");
            } else {
                final var persistedEvent = this.choreographedEventPersistenceService.persistEventToDB(choreographedEvent);
                message.ack(); // acknowledge to Jet Stream that api got the message and it is now in DB.
                log.debug("acknowledged to Jet Stream for EVENT received: {}", persistedEvent.getEventType());
                this.choreographer.handleEvent(persistedEvent);
            }
        } catch (final BusinessException businessException) {
            message.ack(); // acknowledge to Jet Stream that api got the message already...
            log.error("acknowledged to Jet Stream for exception...");
        }
    }
}
