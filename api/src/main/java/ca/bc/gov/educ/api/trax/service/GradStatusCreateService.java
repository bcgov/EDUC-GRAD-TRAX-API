package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.model.dto.GraduationStatus;
import ca.bc.gov.educ.api.trax.model.entity.Event;
import ca.bc.gov.educ.api.trax.model.entity.TraxStudentEntity;
import ca.bc.gov.educ.api.trax.repository.EventRepository;
import ca.bc.gov.educ.api.trax.repository.TraxStudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static ca.bc.gov.educ.api.trax.constant.EventStatus.PROCESSED;
import static ca.bc.gov.educ.api.trax.model.dto.EventType.CREATE_GRAD_STATUS;

@Service
public class GradStatusCreateService implements EventService {
    private final TraxStudentRepository traxStudentRepository;
    private final EventRepository eventRepository;

    @Autowired
    public GradStatusCreateService(TraxStudentRepository traxStudentRepository, EventRepository eventRepository) {
        this.traxStudentRepository = traxStudentRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional
    @Override
    public <T extends Object> void processEvent(T request, Event event) {
        GraduationStatus gradStatusCreate = (GraduationStatus) request;

        var existingStudent = traxStudentRepository.findById(gradStatusCreate.getPen());
        if (existingStudent.isEmpty()) {
            TraxStudentEntity traxStudentEntity = new TraxStudentEntity();
            // Needs to transfer required fields from GraduationStatus to TraxStudentEntity
           // traxStudentRepository.save(traxStudentEntity);
        }

        var existingEvent = eventRepository.findByEventId(event.getEventId());
        existingEvent.ifPresent(eventRecord -> {
            eventRecord.setEventStatus(PROCESSED.toString());
            eventRecord.setUpdateDate(LocalDateTime.now());
            eventRepository.save(eventRecord);
        });
    }

    @Override
    public String getEventType() {
        return CREATE_GRAD_STATUS.toString();
    }
}
