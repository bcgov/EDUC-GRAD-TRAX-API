package ca.bc.gov.educ.api.trax.messaging.jetstream;

import ca.bc.gov.educ.api.trax.constant.EventOutcome;
import ca.bc.gov.educ.api.trax.constant.EventType;
import ca.bc.gov.educ.api.trax.model.dto.ChoreographedEvent;
import ca.bc.gov.educ.api.trax.model.entity.TraxUpdatedPubEvent;
import ca.bc.gov.educ.api.trax.util.EducGradTraxApiConstants;
import ca.bc.gov.educ.api.trax.util.JsonUtil;
import io.nats.client.Connection;
import io.nats.client.JetStream;
import io.nats.client.JetStreamApiException;
import io.nats.client.api.StreamConfiguration;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static ca.bc.gov.educ.api.trax.constant.Topics.TRAX_UPDATE_EVENT_TOPIC;

/**
 * The type Publisher.
 */
@Component("publisher")
@Slf4j
public class Publisher {
  private final JetStream jetStream;

  /**
   * Instantiates a new Publisher.
   *
   * @param natsConnection the nats connection
   * @throws IOException           the io exception
   * @throws JetStreamApiException the jet stream api exception
   */
  @Autowired
  public Publisher(final Connection natsConnection) throws IOException, JetStreamApiException {
    this.jetStream = natsConnection.jetStream();
    this.createUpdateTraxStudentMasterEventStream(natsConnection);
  }

  /**
   * here only name and replicas and max messages are set, rest all are library default.
   *
   * @param natsConnection the nats connection
   * @throws IOException           the io exception
   * @throws JetStreamApiException the jet stream api exception
   */
  private void createUpdateTraxStudentMasterEventStream(final Connection natsConnection) throws IOException, JetStreamApiException {
    val streamConfiguration = StreamConfiguration.builder().name(EducGradTraxApiConstants.TRAX_STREAM_NAME).replicas(1).maxMessages(10000).addSubjects(TRAX_UPDATE_EVENT_TOPIC.name()).build();
    try {
      natsConnection.jetStreamManagement().updateStream(streamConfiguration);
    } catch (final JetStreamApiException exception) {
      if (exception.getErrorCode() == 404) { // the stream does not exist , lets create it.
        natsConnection.jetStreamManagement().addStream(streamConfiguration);
      } else {
        log.error("exception", exception);
      }
    }

  }


  /**
   * Dispatch choreography replicationEvent.
   *
   * @param traxUpdatedPubEvent the traxUpdatedPubEvent
   */
  public void dispatchChoreographyEvent(final TraxUpdatedPubEvent traxUpdatedPubEvent) {
    if (traxUpdatedPubEvent != null && traxUpdatedPubEvent.getEventId() != null) {
      ChoreographedEvent choreographedEvent = new ChoreographedEvent();
      choreographedEvent.setEventType(EventType.valueOf(traxUpdatedPubEvent.getEventType()));
      choreographedEvent.setEventOutcome(EventOutcome.valueOf(traxUpdatedPubEvent.getEventOutcome()));
      choreographedEvent.setActivityCode(traxUpdatedPubEvent.getActivityCode());
      choreographedEvent.setEventPayload(traxUpdatedPubEvent.getEventPayload());
      choreographedEvent.setEventID(traxUpdatedPubEvent.getEventId());
      choreographedEvent.setCreateUser(traxUpdatedPubEvent.getCreateUser());
      choreographedEvent.setUpdateUser(traxUpdatedPubEvent.getUpdateUser());
      try {
        log.debug("Broadcasting replicationEvent :: {}", choreographedEvent);
        val pub = this.jetStream.publishAsync(TRAX_UPDATE_EVENT_TOPIC.name(), JsonUtil.getJsonBytesFromObject(choreographedEvent));
        pub.thenAcceptAsync(result -> log.debug("EventEntity ID :: {} Published to JetStream :: {}", traxUpdatedPubEvent.getEventId(), result.getSeqno()));
      } catch (IOException e) {
        log.error("exception while broadcasting message to JetStream", e);
      }
    }
  }
}
