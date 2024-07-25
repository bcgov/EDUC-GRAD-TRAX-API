package ca.bc.gov.educ.api.trax.choreographer;

import ca.bc.gov.educ.api.trax.constant.EventStatus;
import ca.bc.gov.educ.api.trax.constant.EventType;
import ca.bc.gov.educ.api.trax.model.dto.AuthorityContact;
import ca.bc.gov.educ.api.trax.model.dto.DistrictContact;
import ca.bc.gov.educ.api.trax.model.dto.GradStatusEventPayloadDTO;
import ca.bc.gov.educ.api.trax.model.dto.SchoolContact;
import ca.bc.gov.educ.api.trax.model.dto.institute.School;
import ca.bc.gov.educ.api.trax.model.entity.EventEntity;
import ca.bc.gov.educ.api.trax.repository.EventRepository;
import ca.bc.gov.educ.api.trax.service.EventService;
import ca.bc.gov.educ.api.trax.util.JsonUtil;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jboss.threads.EnhancedQueueExecutor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import static ca.bc.gov.educ.api.trax.constant.EventType.*;


/**
 * This class is responsible to handle different choreographed events related student by calling different services.
 */

@Component
@Slf4j
public class ChoreographEventHandler {
  private final Executor eventExecutor;
  private final Map<String, EventService> eventServiceMap;

  private final EventRepository eventRepository;

  public ChoreographEventHandler(final List<EventService> eventServices, final EventRepository eventRepository) {
    this.eventServiceMap = new HashMap<>();
    this.eventRepository = eventRepository;
    this.eventExecutor = new EnhancedQueueExecutor.Builder()
            .setThreadFactory(new ThreadFactoryBuilder().setNameFormat("event-executor-%d").build())
            .setCorePoolSize(10).setMaximumPoolSize(20).setKeepAliveTime(Duration.ofSeconds(60)).build();
    eventServices.forEach(eventService -> this.eventServiceMap.put(eventService.getEventType(), eventService));
  }

  public void handleEvent(@NonNull final EventEntity eventEntity) {
    //only one thread will process all the request. since RDB won't handle concurrent requests.
    this.eventExecutor.execute(() -> {
      try {
        switch (EventType.valueOf(eventEntity.getEventType())) {
          case GRAD_STUDENT_GRADUATED -> {
            log.debug("Processing GRAD_STUDENT_GRADUATED eventEntity record :: {} ", eventEntity);
            val studentGraduated = JsonUtil.getJsonObjectFromString(GradStatusEventPayloadDTO.class, eventEntity.getEventPayload());
            this.eventServiceMap.get(GRAD_STUDENT_GRADUATED.toString()).processEvent(studentGraduated, eventEntity);
          }
          case GRAD_STUDENT_UPDATED -> {
            log.debug("Processing GRAD_STUDENT_UPDATED eventEntity record :: {} ", eventEntity);
            val studentUpdated = JsonUtil.getJsonObjectFromString(GradStatusEventPayloadDTO.class, eventEntity.getEventPayload());
            this.eventServiceMap.get(GRAD_STUDENT_UPDATED.toString()).processEvent(studentUpdated, eventEntity);
          }
          case GRAD_STUDENT_UNDO_COMPLETION -> {
            log.debug("Processing GRAD_STUDENT_UNDO_COMPLETION eventEntity record :: {} ", eventEntity);
            val studentUndoCompletion = JsonUtil.getJsonObjectFromString(GradStatusEventPayloadDTO.class, eventEntity.getEventPayload());
            this.eventServiceMap.get(GRAD_STUDENT_UNDO_COMPLETION.toString()).processEvent(studentUndoCompletion, eventEntity);
          }
          case CREATE_SCHOOL_CONTACT -> {
            log.debug("Processing {} eventEntity record :: {} ", eventEntity.getEventType(), eventEntity);
            val schoolContactCreated = JsonUtil.getJsonObjectFromString(SchoolContact.class, eventEntity.getEventPayload());
            this.eventServiceMap.get(CREATE_SCHOOL_CONTACT.toString()).processEvent(schoolContactCreated, eventEntity);
          }
          case UPDATE_SCHOOL_CONTACT -> {
            log.debug("Processing {} eventEntity record :: {} ", eventEntity.getEventType(), eventEntity);
            val schoolContactUpdated = JsonUtil.getJsonObjectFromString(SchoolContact.class, eventEntity.getEventPayload());
            this.eventServiceMap.get(UPDATE_SCHOOL_CONTACT.toString()).processEvent(schoolContactUpdated, eventEntity);
          }
          case DELETE_SCHOOL_CONTACT -> {
            log.debug("Processing {} eventEntity record :: {} ", eventEntity.getEventType(), eventEntity);
            val schoolContactDeleted = JsonUtil.getJsonObjectFromString(SchoolContact.class, eventEntity.getEventPayload());
            this.eventServiceMap.get(DELETE_SCHOOL_CONTACT.toString()).processEvent(schoolContactDeleted, eventEntity);
          }
          case CREATE_AUTHORITY_CONTACT -> {
            log.debug("Processing {} eventEntity record :: {} ", eventEntity.getEventType(), eventEntity);
            val authorityContactCreated = JsonUtil.getJsonObjectFromString(AuthorityContact.class, eventEntity.getEventPayload());
            this.eventServiceMap.get(CREATE_AUTHORITY_CONTACT.toString()).processEvent(authorityContactCreated, eventEntity);
          }
          case UPDATE_AUTHORITY_CONTACT -> {
            log.debug("Processing {} eventEntity record :: {} ", eventEntity.getEventType(), eventEntity);
            val authorityContactUpdated = JsonUtil.getJsonObjectFromString(AuthorityContact.class, eventEntity.getEventPayload());
            this.eventServiceMap.get(UPDATE_AUTHORITY_CONTACT.toString()).processEvent(authorityContactUpdated, eventEntity);
          }
          case DELETE_AUTHORITY_CONTACT -> {
            log.debug("Processing {} eventEntity record :: {} ", eventEntity.getEventType(), eventEntity);
            val authorityContactDeleted = JsonUtil.getJsonObjectFromString(AuthorityContact.class, eventEntity.getEventPayload());
            this.eventServiceMap.get(DELETE_AUTHORITY_CONTACT.toString()).processEvent(authorityContactDeleted, eventEntity);
          }
          case CREATE_DISTRICT_CONTACT -> {
            log.debug("Processing {} eventEntity record :: {} ", eventEntity.getEventType(), eventEntity);
            val districtContactCreated = JsonUtil.getJsonObjectFromString(DistrictContact.class, eventEntity.getEventPayload());
            this.eventServiceMap.get(CREATE_DISTRICT_CONTACT.toString()).processEvent(districtContactCreated, eventEntity);
          }
          case UPDATE_DISTRICT_CONTACT -> {
            log.debug("Processing {} eventEntity record :: {} ", eventEntity.getEventType(), eventEntity);
            val districtContactUpdated = JsonUtil.getJsonObjectFromString(DistrictContact.class, eventEntity.getEventPayload());
            this.eventServiceMap.get(UPDATE_DISTRICT_CONTACT.toString()).processEvent(districtContactUpdated, eventEntity);
          }
          case DELETE_DISTRICT_CONTACT -> {
            log.debug("Processing {} eventEntity record :: {} ", eventEntity.getEventType(), eventEntity);
            val districtContactDeleted = JsonUtil.getJsonObjectFromString(DistrictContact.class, eventEntity.getEventPayload());
            this.eventServiceMap.get(DELETE_DISTRICT_CONTACT.toString()).processEvent(districtContactDeleted, eventEntity);
          }
          case UPDATE_SCHOOL -> {
            val schoolUpdated = JsonUtil.getJsonObjectFromString(School.class, eventEntity.getEventPayload());
            this.eventServiceMap.get(UPDATE_SCHOOL.toString()).processEvent(schoolUpdated, eventEntity);
          }
          case CREATE_SCHOOL -> {
            // TODO
          }
          case UPDATE_DISTRICT -> {
            // TODO
          }
          case MOVE_SCHOOL -> {
            // TODO
          }
          default -> {
            log.warn("Silently ignoring eventEntity: {}", eventEntity);
            this.eventRepository.findByEventId(eventEntity.getEventId()).ifPresent(existingEvent -> {
            existingEvent.setEventStatus(EventStatus.PROCESSED.toString());
            existingEvent.setUpdateDate(LocalDateTime.now());
            this.eventRepository.save(existingEvent);
            });
            break;
          }
        }
      } catch (final Exception exception) {
        log.error("Exception while processing eventEntity :: {}", eventEntity, exception);
      }
    });


  }
}
