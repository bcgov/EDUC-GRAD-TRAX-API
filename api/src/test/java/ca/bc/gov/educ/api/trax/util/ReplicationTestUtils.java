package ca.bc.gov.educ.api.trax.util;

import ca.bc.gov.educ.api.trax.repository.EventHistoryRepository;
import ca.bc.gov.educ.api.trax.repository.EventRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("test")
@Getter
public class ReplicationTestUtils {

    private final EventRepository eventRepository;
    private final EventHistoryRepository eventHistoryRepository;

    @Autowired
    public ReplicationTestUtils(EventRepository eventRepository, EventHistoryRepository eventHistoryRepository) {
        this.eventRepository = eventRepository;
        this.eventHistoryRepository = eventHistoryRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void cleanDB() {
        this.eventHistoryRepository.deleteAll();
        this.eventRepository.deleteAll();
    }
}
