package ca.bc.gov.educ.api.trax.repository;

import ca.bc.gov.educ.api.trax.model.entity.EventHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventHistoryRepository extends JpaRepository<EventHistoryEntity, UUID>, JpaSpecificationExecutor<EventHistoryEntity> {

    Optional<EventHistoryEntity> findByEvent_ReplicationEventId(UUID replicationEventId);

}
