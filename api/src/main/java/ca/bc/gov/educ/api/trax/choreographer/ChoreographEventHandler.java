package ca.bc.gov.educ.api.trax.choreographer;

import ca.bc.gov.educ.api.trax.model.dto.GradStatusEventPayloadDTO;
import ca.bc.gov.educ.api.trax.model.dto.GraduationStatus;
import ca.bc.gov.educ.api.trax.model.entity.Event;
import ca.bc.gov.educ.api.trax.service.EventService;
import ca.bc.gov.educ.api.trax.util.JsonUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.jboss.threads.EnhancedQueueExecutor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

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
  private final ObjectMapper mapper = new ObjectMapper();
  private final Executor singleTaskExecutor = new EnhancedQueueExecutor.Builder()
      .setThreadFactory(new ThreadFactoryBuilder().setNameFormat("task-executor-%d").build())
      .setCorePoolSize(1).setMaximumPoolSize(1).build();
  private final Map<String, EventService> eventServiceMap;

  public ChoreographEventHandler(final List<EventService> eventServices) {
    this.eventServiceMap = new HashMap<>();
    eventServices.forEach(eventService -> this.eventServiceMap.put(eventService.getEventType(), eventService));
  }

  public void handleEvent(@NonNull final Event event) {
    //only one thread will process all the request. since RDB won't handle concurrent requests.
    this.singleTaskExecutor.execute(() -> {
      try {
        switch (event.getEventType()) {
          case "GRAD_STUDENT_GRADUATED":
            log.debug("Processing GRAD_STUDENT_GRADUATED event record :: {} ", event);
            final GradStatusEventPayloadDTO eventPayload1 = JsonUtil.getJsonObjectFromString(GradStatusEventPayloadDTO.class, event.getEventPayload());
            this.eventServiceMap.get(GRAD_STUDENT_GRADUATED.toString()).processEvent(eventPayload1, event);
            break;
          case "GRAD_STUDENT_UPDATED":
            log.debug("Processing GRAD_STUDENT_UPDATED event record :: {} ", event);
            final GradStatusEventPayloadDTO eventPayload2 = JsonUtil.getJsonObjectFromString(GradStatusEventPayloadDTO.class, event.getEventPayload());
            this.eventServiceMap.get(GRAD_STUDENT_UPDATED.toString()).processEvent(eventPayload2, event);
            break;
          case "GRAD_STUDENT_UNDO_COMPLETION":
            log.debug("Processing GRAD_STUDENT_UNDO_COMPLETION event record :: {} ", event);
            final GradStatusEventPayloadDTO eventPayload3 = JsonUtil.getJsonObjectFromString(GradStatusEventPayloadDTO.class, event.getEventPayload());
            this.eventServiceMap.get(GRAD_STUDENT_UNDO_COMPLETION.toString()).processEvent(eventPayload3, event);
            break;
          case "STUDENT_CERTIFICATE_DISTRIBUTED":
            log.debug("Processing STUDENT_CERTIFICATE_DISTRIBUTED event record :: {} ", event);
            final GradStatusEventPayloadDTO eventPayload4 = JsonUtil.getJsonObjectFromString(GradStatusEventPayloadDTO.class, event.getEventPayload());
            this.eventServiceMap.get(STUDENT_CERTIFICATE_DISTRIBUTED.toString()).processEvent(eventPayload4, event);
            break;
          default:
            log.warn("Silently ignoring event: {}", event);
            break;
        }
      } catch (final Exception exception) {
        log.error("Exception while processing event :: {}", event, exception);
      }
    });


  }
}
