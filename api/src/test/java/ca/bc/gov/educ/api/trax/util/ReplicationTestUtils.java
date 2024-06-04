package ca.bc.gov.educ.api.trax.util;

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

    @Autowired
    public ReplicationTestUtils(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void cleanDB() {
        this.eventRepository.deleteAll();
    }
}
