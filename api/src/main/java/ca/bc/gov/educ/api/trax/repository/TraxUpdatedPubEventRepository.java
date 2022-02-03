package ca.bc.gov.educ.api.trax.repository;

import ca.bc.gov.educ.api.trax.model.entity.Event;
import ca.bc.gov.educ.api.trax.model.entity.TraxUpdatedPubEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * The interface TraxUpdatedPub event repository.
 */
public interface TraxUpdatedPubEventRepository extends JpaRepository<TraxUpdatedPubEvent, UUID> {
  /**
   * Find by event id optional.
   *
   * @param eventId the event id
   * @return the optional
   */
  Optional<TraxUpdatedPubEvent> findByEventId(UUID eventId);

  /**
   * Find by saga id and event type optional.
   *
   * @param sagaId    the saga i
   * @param eventType the event type
   * @return the optional
   */
  Optional<TraxUpdatedPubEvent> findBySagaIdAndEventType(UUID sagaId, String eventType);

  /**
   * Find by event status list.
   *
   * @param eventStatus the event status
   * @return the list
   */
  List<TraxUpdatedPubEvent> findByEventStatus(String eventStatus);
}
