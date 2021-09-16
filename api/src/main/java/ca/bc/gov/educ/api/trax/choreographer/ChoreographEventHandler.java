package ca.bc.gov.educ.api.trax.choreographer;

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

import static ca.bc.gov.educ.api.trax.model.dto.EventType.*;


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
    //only one thread will process all the request. since RDB wont handle concurrent requests.
    this.singleTaskExecutor.execute(() -> {

      try {
        switch (event.getEventType()) {
          case "CREATE_GRAD_STATUS":
            final GraduationStatus gradStatusCreate = JsonUtil.getJsonObjectFromString(GraduationStatus.class, event.getEventPayload());
            this.eventServiceMap.get(CREATE_GRAD_STATUS.toString()).processEvent(gradStatusCreate, event);
            break;
          case "UPDATE_GRAD_STATUS":
            log.info("Processing UPDATE_STUDENT event record :: {} ", event);
            final GraduationStatus gradStatusUpdate = JsonUtil.getJsonObjectFromString(GraduationStatus.class, event.getEventPayload());
            this.eventServiceMap.get(UPDATE_GRAD_STATUS.toString()).processEvent(gradStatusUpdate, event);
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
