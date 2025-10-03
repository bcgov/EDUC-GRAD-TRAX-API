package ca.bc.gov.educ.api.trax.repository;

import ca.bc.gov.educ.api.trax.model.entity.TraxUpdatedPubEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
  List<TraxUpdatedPubEvent> findByEventStatusOrderByCreateDate(String eventStatus);

  @Query(value = "SELECT s FROM TRAX_UPDATED_PUB_EVENT s WHERE s.EVENT_STATUS in :cleanupStatus AND ROWNUM < :batchSize ORDER BY CREATE_DATE", nativeQuery = true)
  List<TraxUpdatedPubEvent> fetchByEventStatus(List<String> cleanupStatus, int batchSize);

  @Query(value = "SELECT EVENT_ID FROM TRAX_UPDATED_PUB_EVENT s WHERE s.EVENT_STATUS in :cleanupStatus AND ROWNUM < :batchSize", nativeQuery = true)
  List<byte[]> findByStatusIn(List<String> cleanupStatus, int batchSize);
  
  @Transactional
  @Modifying
  @Query("delete from TraxUpdatedPubEvent where createDate <= :createDate")
  void deleteByCreateDateBefore(LocalDateTime createDate);

}
