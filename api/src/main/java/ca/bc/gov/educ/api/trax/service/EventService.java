package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.model.entity.Event;

public interface EventService<T> {

  void processEvent(T request, Event event);

  String getEventType();
}
