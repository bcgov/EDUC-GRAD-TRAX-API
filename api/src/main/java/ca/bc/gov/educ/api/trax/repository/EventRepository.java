package ca.bc.gov.educ.api.trax.repository;

import ca.bc.gov.educ.api.trax.model.entity.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    @Transactional
    @Modifying
    @Query("delete from EventEntity where createDate <= :createDate")
    void deleteByCreateDateBefore(LocalDateTime createDate);

}
