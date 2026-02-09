package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.choreographer.ChoreographEventHandler;
import ca.bc.gov.educ.api.trax.constant.EventActivityCode;
import ca.bc.gov.educ.api.trax.constant.Topics;
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
     * Instantiates a new EventEntity handler delegator service.
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
    public void handleChoreographyEvent(@NonNull ChoreographedEvent choreographedEvent, final Message message) throws IOException {
        // some messages come in already with an activity code, some do not.
        // set the activity code early in the process
        setActivityCode(choreographedEvent, message);
        if (message.getSubject().equalsIgnoreCase(TRAX_UPDATE_EVENT_TOPIC.toString())) {
            this.choreographedEventPersistenceService.updateEventStatus(choreographedEvent);
            message.ack();
            log.debug("acknowledged to Jet Stream for TRAX UPDATE EVENT sent...");
        } else {
            if(!this.choreographedEventPersistenceService.eventExistsInDB(choreographedEvent).isPresent()) {
                final var persistedEvent = this.choreographedEventPersistenceService.persistEventToDB(choreographedEvent);
                if(persistedEvent != null) {
                    message.ack(); // acknowledge to Jet Stream that api got the message and it is now in DB.
                    log.debug("acknowledged to Jet Stream for EVENT received: {}", persistedEvent.getEventType());
                    log.info("Processing event {} and event payload is {}", persistedEvent.getEventType(), persistedEvent.getEventPayload());
                    this.choreographer.handleEvent(persistedEvent);
                }
            } else {
                message.ack(); // acknowledge to Jet Stream that api got the message and it is already in DB.
                log.debug("Event with ID {} already exists in the database. No further action taken.", choreographedEvent.getEventID());
            }
        }
    }

    /**
     * Applies the correct activity code to ChoreographedEvents.
     * Add new activity codes here
     * @param choreographedEvent the choreographed event object
     * @param message message received from nats
     */
    private void setActivityCode(@NonNull final ChoreographedEvent choreographedEvent, final Message message) {
        Topics topics;
        try {
            topics = Topics.valueOf(message.getSubject());
            switch (topics) {
                case GRAD_SCHOOL_EVENTS_TOPIC:
                    choreographedEvent.setActivityCode(EventActivityCode.GRAD_SCHOOL_EVENT.toString());
                    break;
                case INSTITUTE_EVENTS_TOPIC:
                    choreographedEvent.setActivityCode(EventActivityCode.INSTITUTE_EVENT.toString());
                    break;
                case COREG_EVENTS_TOPIC:
                    choreographedEvent.setActivityCode(EventActivityCode.COREG_EVENT.toString());
                    break;
                case PEN_EVENTS_TOPIC:
                    choreographedEvent.setActivityCode(EventActivityCode.PEN_EVENT.toString());
                    break;
                default: // do nothing
                    break;
            }
        } catch (Exception e) {
            log.error("{} is not a valid topic", message.getSubject());
        }

    }
}
