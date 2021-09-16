package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.model.dto.GraduationStatus;
import ca.bc.gov.educ.api.trax.model.entity.Event;
import ca.bc.gov.educ.api.trax.model.entity.TraxStudentEntity;
import ca.bc.gov.educ.api.trax.repository.EventRepository;
import ca.bc.gov.educ.api.trax.repository.TraxStudentRepository;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
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
            // Needs to update required fields from GraduationStatus to TraxStudentEntity
            if (StringUtils.isNotBlank(gradStatusUpdate.getProgram())) {
                String year = convertProgramToYear(gradStatusUpdate.getProgram());
                if (year != null) {
                    traxStudentEntity.setGradReqtYear(year);
                }
            }
            if (StringUtils.isNotBlank(gradStatusUpdate.getProgramCompletionDate())) {
                String gradDateStr = gradStatusUpdate.getProgramCompletionDate().replace("/", "");
                if (NumberUtils.isDigits(gradDateStr)) {
                    traxStudentEntity.setGradDate(Long.valueOf(gradDateStr));
                }
            } else {
                traxStudentEntity.setGradDate(0L);
            }
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

    private String convertProgramToYear(String program) {
        String ret = null;
        if (StringUtils.startsWith(program, "2018")) {
            ret = "2018";
        } else if (StringUtils.startsWith(program, "2004")) {
            ret = "2004";
        } else if (StringUtils.startsWith(program, "1996")) {
            ret = "1996";
        } else if (StringUtils.startsWith(program, "1986")) {
            ret = "1986";
        } else if (StringUtils.startsWith(program, "1950")
            || StringUtils.startsWith(program, "NOPROG")) {
            ret = "1950";
        } else if (StringUtils.startsWith(program, "SCCP")) {
            ret = "SCCP";
        }
        return ret;
    }
}