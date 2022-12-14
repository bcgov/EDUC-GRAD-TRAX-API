package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.constant.FieldType;
import ca.bc.gov.educ.api.trax.model.dto.GradStatusEventPayloadDTO;
import ca.bc.gov.educ.api.trax.model.entity.Event;
import ca.bc.gov.educ.api.trax.model.entity.TraxStudentEntity;
import ca.bc.gov.educ.api.trax.repository.EventRepository;
import ca.bc.gov.educ.api.trax.repository.TraxStudentRepository;
import ca.bc.gov.educ.api.trax.util.EducGradTraxApiConstants;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static ca.bc.gov.educ.api.trax.constant.EventStatus.PROCESSED;
import static ca.bc.gov.educ.api.trax.constant.EventType.GRAD_STUDENT_UNDO_COMPLETION;

@Service
@Slf4j
public class GradStudentUndoCompletionService extends BaseService {
    private final EntityManagerFactory emf;
    private final TraxStudentRepository traxStudentRepository;
    private final EventRepository eventRepository;
    private final EducGradTraxApiConstants constants;

    @Autowired
    public GradStudentUndoCompletionService(EntityManagerFactory emf,
                                            TraxStudentRepository traxStudentRepository,
                                            EventRepository eventRepository,
                                            EducGradTraxApiConstants constants) {
        this.emf = emf;
        this.traxStudentRepository = traxStudentRepository;
        this.eventRepository = eventRepository;
        this.constants = constants;
    }

    @Override
    public <T extends Object> void processEvent(T request, Event event) {
        GradStatusEventPayloadDTO gradStatusUpdate = (GradStatusEventPayloadDTO) request;

        val em = this.emf.createEntityManager();
        var existingStudent = traxStudentRepository.findById(gradStatusUpdate.getPen());
        final EntityTransaction tx = em.getTransaction();
        try {
            process(existingStudent, gradStatusUpdate, em, tx, constants.isTraxUpdateEnabled());

            var existingEvent = eventRepository.findByEventId(event.getEventId());
            existingEvent.ifPresent(eventRecord -> {
                eventRecord.setEventStatus(PROCESSED.toString());
                eventRecord.setUpdateDate(LocalDateTime.now());
                eventRepository.saveAndFlush(eventRecord);
            });
        } catch (Exception e) {
            log.error("Error occurred saving entity " + e.getMessage());
            tx.rollback();
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    @Override
    public Map<String, Pair<FieldType, Object>> initializeUpdateFieldsMap() {
        Map<String, Pair<FieldType, Object>> updateFieldsMap = new HashMap<>();
        // grad_reqt_year_at_grad(= blank)
        updateFieldsMap.put(FIELD_GRAD_REQT_YEAR_AT_GRAD, null);
        // grad_date(= zero), slp_date(= zero for SCCP)
        updateFieldsMap.put(FIELD_GRAD_DATE, null);
        updateFieldsMap.put(FIELD_SLP_DATE, null);
        // mincode_grad(= blank)
        updateFieldsMap.put(FIELD_MINCODE_GRAD, null);
        // stud_grade_at_grad(= blank)
        updateFieldsMap.put(FIELD_STUD_GRADE_AT_GRAD, null);
        // honour_flag(= blank)
        updateFieldsMap.put(FIELD_HONOUR_FLAG, null);

        return updateFieldsMap;
    }

    @Override
    public void specialHandlingOnUpdateFieldsMap(Map<String, Pair<FieldType, Object>> updateFieldsMap, TraxStudentEntity traxStudentEntity, GradStatusEventPayloadDTO gradStatusUpdate) {
        boolean isSCCP = StringUtils.equalsIgnoreCase(gradStatusUpdate.getProgram(), "SCCP");
        // For SCCP -------------------------------------------------------------------------
        if (!isSCCP && updateFieldsMap.containsKey(FIELD_SLP_DATE)) {
            updateFieldsMap.remove(FIELD_SLP_DATE);
        }

        // When a student is un-graduated ---------------------------------------------------
        // grad_reqt_year_at_grad(= blank)
        gradStatusUpdate.setProgram(null);
        updateFieldsMap.put(FIELD_GRAD_REQT_YEAR_AT_GRAD, Pair.of(FieldType.TRAX_STRING, " "));

        if (isSCCP) {
            // slp_date(= zero for SCCP)
            gradStatusUpdate.setProgramCompletionDate(null);
            updateFieldsMap.put(FIELD_SLP_DATE, Pair.of(FieldType.TRAX_DATE, Long.valueOf("0")));
        }

        // mincode_grad(= blank)
        gradStatusUpdate.setSchoolAtGrad(null);
        updateFieldsMap.put(FIELD_MINCODE_GRAD, Pair.of(FieldType.TRAX_STRING, " "));

        // stud_grade_at_grad(= blank)
        gradStatusUpdate.setStudentGrade(null);
        updateFieldsMap.put(FIELD_STUD_GRADE_AT_GRAD, Pair.of(FieldType.TRAX_STRING, " "));

        // honour_flag(= blank)
        gradStatusUpdate.setHonoursStanding(null);
        updateFieldsMap.put(FIELD_HONOUR_FLAG, Pair.of(FieldType.TRAX_STRING, " "));
    }

    @Override
    public String getEventType() {
        return GRAD_STUDENT_UNDO_COMPLETION.toString();
    }
}