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
import static ca.bc.gov.educ.api.trax.model.dto.EventType.UPDATE_GRAD_STATUS;

@Service
public class GradStatusUpdateService implements EventService {
    private final TraxStudentRepository traxStudentRepository;
    private final EventRepository eventRepository;

    @Autowired
    public GradStatusUpdateService(TraxStudentRepository traxStudentRepository, EventRepository eventRepository) {
        this.traxStudentRepository = traxStudentRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional
    @Override
    public <T extends Object> void processEvent(T request, Event event) {
        GraduationStatus gradStatusUpdate = (GraduationStatus) request;

        var existingStudent = traxStudentRepository.findById(gradStatusUpdate.getPen());
        if (existingStudent.isPresent()) {
            TraxStudentEntity traxStudentEntity =existingStudent.get();
            // Needs to transfer required fields from GraduationStatus to TraxStudentEntity
//            traxStudentEntity.setGradReqtYear(gradStatusUpdate.getProgram());
//            traxStudentEntity.setGradDate(gradStatusUpdate.getProgramCompletionDate());
            traxStudentEntity.setMincode(gradStatusUpdate.getSchoolOfRecord());
            traxStudentEntity.setStudGrade(gradStatusUpdate.getStudentGrade());
            traxStudentEntity.setStudStatus(gradStatusUpdate.getStudentStatus());
            traxStudentRepository.save(traxStudentEntity);
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
        return UPDATE_GRAD_STATUS.toString();
    }
}