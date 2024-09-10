package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.exception.ServiceException;
import ca.bc.gov.educ.api.trax.repository.EventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class EventHistoryService {

    private EventRepository eventRepository;

    @Autowired
    public EventHistoryService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public void purgeOldEventAndEventHistoryRecords(LocalDateTime sinceBefore) throws ServiceException {
        try {
            this.eventRepository.deleteByCreateDateLessThan(sinceBefore);
        } catch (Exception e) {
            throw new ServiceException(String.format("Exception encountered when attempting old Event History Purge: %s", e.getMessage()));
        }
    }


}
