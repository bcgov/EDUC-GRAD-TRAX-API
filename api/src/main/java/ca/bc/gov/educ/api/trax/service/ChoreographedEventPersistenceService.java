package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.exception.BusinessError;
import ca.bc.gov.educ.api.trax.exception.BusinessException;
import ca.bc.gov.educ.api.trax.model.dto.ChoreographedEvent;
import ca.bc.gov.educ.api.trax.model.entity.EventEntity;
import ca.bc.gov.educ.api.trax.repository.EventRepository;
import ca.bc.gov.educ.api.trax.repository.TraxUpdatedPubEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static ca.bc.gov.educ.api.trax.constant.EventStatus.MESSAGE_PUBLISHED;
import static ca.bc.gov.educ.api.trax.util.EducGradTraxApiConstants.DEFAULT_CREATED_BY;
import static ca.bc.gov.educ.api.trax.util.EducGradTraxApiConstants.DEFAULT_UPDATED_BY;
import static ca.bc.gov.educ.api.trax.constant.EventStatus.DB_COMMITTED;


@Service
@Slf4j
public class ChoreographedEventPersistenceService {
  private final EventRepository eventRepository;
  private final TraxUpdatedPubEventRepository traxUpdatedPubEventRepository;

  @Autowired
  public ChoreographedEventPersistenceService(
          final EventRepository eventRepository,
          final TraxUpdatedPubEventRepository traxUpdatedPubEventRepository) {
    this.eventRepository = eventRepository;
    this.traxUpdatedPubEventRepository = traxUpdatedPubEventRepository;
  }

  public Optional<EventEntity> eventExistsInDB(final ChoreographedEvent choreographedEvent) {
    return eventRepository.findByEventId(choreographedEvent.getEventID());
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public EventEntity persistEventToDB(final ChoreographedEvent choreographedEvent) {
    final EventEntity eventEntity = EventEntity.builder()
              .eventType(choreographedEvent.getEventType().toString())
              .eventId(choreographedEvent.getEventID())
              .eventOutcome(choreographedEvent.getEventOutcome().toString())
              .activityCode(choreographedEvent.getActivityCode())
              .eventPayload(choreographedEvent.getEventPayload())
              .eventStatus(DB_COMMITTED.toString())
              .createUser(StringUtils.isBlank(choreographedEvent.getCreateUser()) ? DEFAULT_CREATED_BY : choreographedEvent.getCreateUser())
              .updateUser(StringUtils.isBlank(choreographedEvent.getUpdateUser()) ? DEFAULT_UPDATED_BY : choreographedEvent.getUpdateUser())
              .createDate(LocalDateTime.now())
              .updateDate(LocalDateTime.now())
              .build();
      return this.eventRepository.save(eventEntity);
  }

  /**
   * Update event status.
   *
   * @param choreographedEvent the choreographed event
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void updateEventStatus(ChoreographedEvent choreographedEvent) {
    if (choreographedEvent != null && choreographedEvent.getEventID() != null) {
      var eventOptional = traxUpdatedPubEventRepository.findByEventId(choreographedEvent.getEventID());
      if (eventOptional.isPresent()) {
        var traxUpdatedPubEvent = eventOptional.get();
        traxUpdatedPubEvent.setEventStatus(MESSAGE_PUBLISHED.toString());
        traxUpdatedPubEventRepository.save(traxUpdatedPubEvent);
      }
    }
  }
}
