package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.model.entity.EventEntity;

public interface EventService<T> {

  void processEvent(T request, EventEntity eventEntity);

  String getEventType();
}
