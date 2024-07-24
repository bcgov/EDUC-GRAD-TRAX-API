package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.constant.EventStatus;
import ca.bc.gov.educ.api.trax.model.entity.EventEntity;
import ca.bc.gov.educ.api.trax.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

public abstract class EventBaseService<T> implements EventService<T> {

    @Autowired
    protected EventRepository eventRepository;

    protected void updateEvent(final EventEntity eventEntity) {
        this.eventRepository.findByEventId(eventEntity.getEventId()).ifPresent(existingEvent -> {
            existingEvent.setEventStatus(EventStatus.PROCESSED.toString());
            existingEvent.setUpdateDate(LocalDateTime.now());
            this.eventRepository.save(existingEvent);
        });
    }

    protected void updateEventWithHistory(final EventEntity eventEntity) {

    }

}
