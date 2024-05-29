package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.constant.FieldType;
import ca.bc.gov.educ.api.trax.model.dto.GradStatusEventPayloadDTO;
import ca.bc.gov.educ.api.trax.model.entity.TraxStudentEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.util.Map;

import static ca.bc.gov.educ.api.trax.constant.EventType.GRAD_STUDENT_UNDO_COMPLETION;

@Service
@Slf4j
public class GradStudentUndoCompletionService<T> extends EventCommonService<T> {
    private final EntityManagerFactory emf;

    @Autowired
    public GradStudentUndoCompletionService(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public void specialHandlingOnUpdateFieldsMap(Map<String, Pair<FieldType, Object>> updateFieldsMap, TraxStudentEntity traxStudentEntity, GradStatusEventPayloadDTO gradStatusUpdate) {
        boolean isSCCP = StringUtils.equalsIgnoreCase(gradStatusUpdate.getProgram(), "SCCP");

        // When a student is un-graduated ---------------------------------------------------
        if (isSCCP) {
            gradStatusUpdate.setProgramCompletionDate(null);
            // slp_date(= zero)
            updateFieldsMap.put(FIELD_SLP_DATE, Pair.of(FieldType.TRAX_DATE, Long.valueOf("0")));
            // scc_date(= zero)
            updateFieldsMap.put(FIELD_SCC_DATE, Pair.of(FieldType.TRAX_DATE, Long.valueOf("0")));

            // ignored fields for programs other than SCCP
            updateFieldsMap.remove(FIELD_GRAD_REQT_YEAR_AT_GRAD);
            updateFieldsMap.remove(FIELD_GRAD_DATE);
            updateFieldsMap.remove(FIELD_MINCODE_GRAD);
            updateFieldsMap.remove(FIELD_STUD_GRADE_AT_GRAD);
            updateFieldsMap.remove(FIELD_HONOUR_FLAG);
            updateFieldsMap.remove(FIELD_DOGWOOD_FLAG);
            updateFieldsMap.remove(FIELD_ENGLISH_CERT);
            updateFieldsMap.remove(FIELD_FRENCH_CERT);
        } else {
            // grad_reqt_year_at_grad(= blank)
            gradStatusUpdate.setProgram(null);
            updateFieldsMap.put(FIELD_GRAD_REQT_YEAR_AT_GRAD, Pair.of(FieldType.TRAX_STRING, " "));

            // grad_date(= zero)
            gradStatusUpdate.setProgramCompletionDate(null);
            updateFieldsMap.put(FIELD_GRAD_DATE, Pair.of(FieldType.TRAX_DATE, Long.valueOf("0")));

            // ignored fields for SCCP
            updateFieldsMap.remove(FIELD_SLP_DATE);
            updateFieldsMap.remove(FIELD_SCC_DATE);

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