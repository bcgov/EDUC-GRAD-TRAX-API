package ca.bc.gov.educ.api.trax.repository;

import ca.bc.gov.educ.api.trax.model.entity.EventEntity;
import ca.bc.gov.educ.api.trax.model.entity.TraxUpdatedPubEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<EventEntity, UUID> {

    Optional<EventEntity> findByEventId(UUID eventId);

    List<EventEntity> findAllByEventStatusOrderByCreateDate(String eventStatus);


    @Query(value = "SELECT s FROM REPLICATION_EVENT s WHERE s.EVENT_STATUS in :cleanupStatus AND ROWNUM < :batchSize ORDER BY CREATE_DATE", nativeQuery = true)
    List<EventEntity> fetchByEventStatus(List<String> cleanupStatus, int batchSize);

    @Transactional
    void deleteByCreateDateLessThan(LocalDateTime createDate);

}
