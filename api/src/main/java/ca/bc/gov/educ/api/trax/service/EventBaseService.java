package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.constant.EventStatus;
import ca.bc.gov.educ.api.trax.model.entity.EventEntity;
import ca.bc.gov.educ.api.trax.model.entity.EventHistoryEntity;
import ca.bc.gov.educ.api.trax.repository.EventHistoryRepository;
import ca.bc.gov.educ.api.trax.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

public abstract class EventBaseService<T> implements EventService<T> {

    @Autowired
    protected EventRepository eventRepository;
    @Autowired
    protected EventHistoryRepository eventHistoryRepository;

    protected void updateEvent(final EventEntity eventEntity, boolean includeHistory) {
        this.eventRepository.findByEventId(eventEntity.getEventId()).ifPresent(existingEvent -> {
            existingEvent.setEventStatus(EventStatus.PROCESSED.toString());
            existingEvent.setUpdateDate(LocalDateTime.now());
            this.eventRepository.save(existingEvent);
            if(includeHistory){
                EventHistoryEntity eventHistoryEntity = new EventHistoryEntity();
                eventHistoryEntity.setEvent(existingEvent);
                this.eventHistoryRepository.save(eventHistoryEntity);
            }
        });
    }

    /**
     * Adds the event to the EventHistory table. Implementing classes may want to
     * use this method if they are interested in history tracking
     * @param eventEntity the event entity
     */
    // TODO: Delete this
    protected void updateEventWithHistory(final EventEntity eventEntity) {
        this.updateEvent(eventEntity, true);
    }

}
