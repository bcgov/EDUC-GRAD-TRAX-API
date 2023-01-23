package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.constant.FieldType;
import ca.bc.gov.educ.api.trax.model.dto.GradStatusEventPayloadDTO;
import ca.bc.gov.educ.api.trax.model.entity.TraxStudentEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.Map;

import static ca.bc.gov.educ.api.trax.constant.EventType.GRAD_STUDENT_UNDO_COMPLETION;

@Service
@Slf4j
public class GradStudentUndoCompletionService extends EventCommonService {
    private final EntityManagerFactory emf;

    @Autowired
    public GradStudentUndoCompletionService(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public void specialHandlingOnUpdateFieldsMap(Map<String, Pair<FieldType, Object>> updateFieldsMap, TraxStudentEntity traxStudentEntity, GradStatusEventPayloadDTO gradStatusUpdate) {
        // skip SCCP -------------------------------------------------------------------------
        if (StringUtils.equalsIgnoreCase(gradStatusUpdate.getProgram(), "SCCP")) {
            updateFieldsMap.clear();
            log.info("  Skip the undo completion update against SCCP student in TRAX.");
        }

        // When a student is un-graduated ---------------------------------------------------
        // grad_reqt_year_at_grad(= blank)
        gradStatusUpdate.setProgram(null);
        updateFieldsMap.put(FIELD_GRAD_REQT_YEAR_AT_GRAD, Pair.of(FieldType.TRAX_STRING, " "));

        // grad_date or slp_date(= zero)
        gradStatusUpdate.setProgramCompletionDate(null);
        updateFieldsMap.put(FIELD_GRAD_DATE, Pair.of(FieldType.TRAX_DATE, Long.valueOf("0")));

        // mincode_grad(= blank)
        gradStatusUpdate.setSchoolAtGrad(null);
        updateFieldsMap.put(FIELD_MINCODE_GRAD, Pair.of(FieldType.TRAX_STRING, " "));

        // stud_grade_at_grad(= blank)
        gradStatusUpdate.setStudentGrade(null);
        updateFieldsMap.put(FIELD_STUD_GRADE_AT_GRAD, Pair.of(FieldType.TRAX_STRING, " "));

        // honour_flag(= blank)
        gradStatusUpdate.setHonoursStanding(null);
        updateFieldsMap.put(FIELD_HONOUR_FLAG, Pair.of(FieldType.TRAX_STRING, " "));

        // extra
        updateFieldsMap.put(FIELD_DOGWOOD_FLAG, Pair.of(FieldType.TRAX_STRING, " "));
        updateFieldsMap.put(FIELD_ENGLISH_CERT, Pair.of(FieldType.TRAX_STRING, " "));
        updateFieldsMap.put(FIELD_FRENCH_CERT, Pair.of(FieldType.TRAX_STRING, " "));
    }

    @Override
    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    @Override
    public String getEventType() {
        return GRAD_STUDENT_UNDO_COMPLETION.toString();
    }
}