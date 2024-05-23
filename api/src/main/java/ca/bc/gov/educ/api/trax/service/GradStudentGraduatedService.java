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

import static ca.bc.gov.educ.api.trax.constant.EventType.GRAD_STUDENT_GRADUATED;

@Service
@Slf4j
public class GradStudentGraduatedService<T> extends EventCommonService<T> {
    private final EntityManagerFactory emf;

    @Autowired
    public GradStudentGraduatedService(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public void specialHandlingOnUpdateFieldsMap(Map<String, Pair<FieldType, Object>> updateFieldsMap, TraxStudentEntity traxStudentEntity, GradStatusEventPayloadDTO gradStatusUpdate) {
        // skip SCCP -------------------------------------------------------------------------
        if (StringUtils.equalsIgnoreCase(gradStatusUpdate.getProgram(), "SCCP")) {
            updateFieldsMap.clear();
            log.debug("  Skip the graduation update against SCCP student in TRAX");
            return;
        }

        // ignore fields for un-graduated student
        if (StringUtils.isBlank(gradStatusUpdate.getProgramCompletionDate())) {
            updateFieldsMap.remove(FIELD_GRAD_REQT_YEAR_AT_GRAD);
            updateFieldsMap.remove(FIELD_STUD_GRADE_AT_GRAD);
            updateFieldsMap.remove(FIELD_MINCODE_GRAD);
            // ignore fields for SCCP
            updateFieldsMap.remove(FIELD_SLP_DATE);
            updateFieldsMap.remove(FIELD_SCC_DATE);
        }
    }

    @Override
    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    @Override
    public String getEventType() {
        return GRAD_STUDENT_GRADUATED.toString();
    }
}