package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.constant.EventStatus;
import ca.bc.gov.educ.api.trax.model.entity.Event;
import ca.bc.gov.educ.api.trax.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

public abstract class EventBaseService<T> implements EventService<T> {

    @Autowired
    protected EventRepository eventRepository;

    protected void updateEvent(final Event event) {
        this.eventRepository.findByEventId(event.getEventId()).ifPresent(existingEvent -> {
            existingEvent.setEventStatus(EventStatus.PROCESSED.toString());
            existingEvent.setUpdateDate(LocalDateTime.now());
            this.eventRepository.save(existingEvent);
        });
    }

}
