package ca.bc.gov.educ.api.trax.choreographer;

import ca.bc.gov.educ.api.trax.constant.EventType;
import ca.bc.gov.educ.api.trax.model.dto.AuthorityContact;
import ca.bc.gov.educ.api.trax.model.dto.DistrictContact;
import ca.bc.gov.educ.api.trax.model.dto.GradStatusEventPayloadDTO;
import ca.bc.gov.educ.api.trax.model.dto.SchoolContact;
import ca.bc.gov.educ.api.trax.model.entity.Event;
import ca.bc.gov.educ.api.trax.service.EventService;
import ca.bc.gov.educ.api.trax.util.JsonUtil;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jboss.threads.EnhancedQueueExecutor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.time.Duration;
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

  public ChoreographEventHandler(final List<EventService> eventServices) {
    this.eventServiceMap = new HashMap<>();
    this.eventExecutor = new EnhancedQueueExecutor.Builder()
            .setThreadFactory(new ThreadFactoryBuilder().setNameFormat("event-executor-%d").build())
            .setCorePoolSize(10).setMaximumPoolSize(20).setKeepAliveTime(Duration.ofSeconds(60)).build();
    eventServices.forEach(eventService -> this.eventServiceMap.put(eventService.getEventType(), eventService));
  }

  public void handleEvent(@NonNull final Event event) {
    //only one thread will process all the request. since RDB won't handle concurrent requests.
    this.eventExecutor.execute(() -> {
      try {
        switch (EventType.valueOf(event.getEventType())) {
          case GRAD_STUDENT_GRADUATED -> {
            log.debug("Processing GRAD_STUDENT_GRADUATED event record :: {} ", event);
            val studentGraduated = JsonUtil.getJsonObjectFromString(GradStatusEventPayloadDTO.class, event.getEventPayload());
            this.eventServiceMap.get(GRAD_STUDENT_GRADUATED.toString()).processEvent(studentGraduated, event);
          }
          case GRAD_STUDENT_UPDATED -> {
            log.debug("Processing GRAD_STUDENT_UPDATED event record :: {} ", event);
            val studentUpdated = JsonUtil.getJsonObjectFromString(GradStatusEventPayloadDTO.class, event.getEventPayload());
            this.eventServiceMap.get(GRAD_STUDENT_UPDATED.toString()).processEvent(studentUpdated, event);
          }
          case GRAD_STUDENT_UNDO_COMPLETION -> {
            log.debug("Processing GRAD_STUDENT_UNDO_COMPLETION event record :: {} ", event);
            val studentUndoCompletion = JsonUtil.getJsonObjectFromString(GradStatusEventPayloadDTO.class, event.getEventPayload());
            this.eventServiceMap.get(GRAD_STUDENT_UNDO_COMPLETION.toString()).processEvent(studentUndoCompletion, event);
          }
          case CREATE_SCHOOL_CONTACT -> {
            log.debug("Processing {} event record :: {} ", event.getEventType(), event);
            val schoolContactCreated = JsonUtil.getJsonObjectFromString(SchoolContact.class, event.getEventPayload());
            this.eventServiceMap.get(CREATE_SCHOOL_CONTACT.toString()).processEvent(schoolContactCreated, event);
          }
          case UPDATE_SCHOOL_CONTACT -> {
            log.debug("Processing {} event record :: {} ", event.getEventType(), event);
            val schoolContactUpdated = JsonUtil.getJsonObjectFromString(SchoolContact.class, event.getEventPayload());
            this.eventServiceMap.get(UPDATE_SCHOOL_CONTACT.toString()).processEvent(schoolContactUpdated, event);
          }
          case DELETE_SCHOOL_CONTACT -> {
            log.debug("Processing {} event record :: {} ", event.getEventType(), event);
            val schoolContactDeleted = JsonUtil.getJsonObjectFromString(SchoolContact.class, event.getEventPayload());
            this.eventServiceMap.get(DELETE_SCHOOL_CONTACT.toString()).processEvent(schoolContactDeleted, event);
          }
          case CREATE_AUTHORITY_CONTACT -> {
            log.debug("Processing {} event record :: {} ", event.getEventType(), event);
            val authorityContactCreated = JsonUtil.getJsonObjectFromString(AuthorityContact.class, event.getEventPayload());
            this.eventServiceMap.get(CREATE_AUTHORITY_CONTACT.toString()).processEvent(authorityContactCreated, event);
          }
          case UPDATE_AUTHORITY_CONTACT -> {
            log.debug("Processing {} event record :: {} ", event.getEventType(), event);
            val authorityContactUpdated = JsonUtil.getJsonObjectFromString(AuthorityContact.class, event.getEventPayload());
            this.eventServiceMap.get(UPDATE_AUTHORITY_CONTACT.toString()).processEvent(authorityContactUpdated, event);
          }
          case DELETE_AUTHORITY_CONTACT -> {
            log.debug("Processing {} event record :: {} ", event.getEventType(), event);
            val authorityContactDeleted = JsonUtil.getJsonObjectFromString(AuthorityContact.class, event.getEventPayload());
            this.eventServiceMap.get(DELETE_AUTHORITY_CONTACT.toString()).processEvent(authorityContactDeleted, event);
          }
          case CREATE_DISTRICT_CONTACT -> {
            log.debug("Processing {} event record :: {} ", event.getEventType(), event);
            val districtContactCreated = JsonUtil.getJsonObjectFromString(DistrictContact.class, event.getEventPayload());
            this.eventServiceMap.get(CREATE_DISTRICT_CONTACT.toString()).processEvent(districtContactCreated, event);
          }
          case UPDATE_DISTRICT_CONTACT -> {
            log.debug("Processing {} event record :: {} ", event.getEventType(), event);
            val districtContactUpdated = JsonUtil.getJsonObjectFromString(DistrictContact.class, event.getEventPayload());
            this.eventServiceMap.get(UPDATE_DISTRICT_CONTACT.toString()).processEvent(districtContactUpdated, event);
          }
          case DELETE_DISTRICT_CONTACT -> {
            log.debug("Processing {} event record :: {} ", event.getEventType(), event);
            val districtContactDeleted = JsonUtil.getJsonObjectFromString(DistrictContact.class, event.getEventPayload());
            this.eventServiceMap.get(DELETE_DISTRICT_CONTACT.toString()).processEvent(districtContactDeleted, event);
          }
          default -> log.warn("Silently ignoring event: {}", event);
        }
      } catch (final Exception exception) {
        log.error("Exception while processing event :: {}", event, exception);
      }
    });


  }
}
