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

import static ca.bc.gov.educ.api.trax.constant.EventType.GRAD_STUDENT_UPDATED;

@Service
@Slf4j
public class GradStudentUpdatedService<T> extends EventCommonService<T> {
    private final EntityManagerFactory emf;

    @Autowired
    public GradStudentUpdatedService(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public void specialHandlingOnUpdateFieldsMap(Map<String, Pair<FieldType, Object>> updateFieldsMap, TraxStudentEntity traxStudentEntity, GradStatusEventPayloadDTO gradStatusUpdate) {
        boolean isSCCP = StringUtils.equalsIgnoreCase(gradStatusUpdate.getProgram(), "SCCP");
        // SCCP
        if (isSCCP && !updateFieldsMap.containsKey(FIELD_SLP_DATE)) {
            updateFieldsMap.put(FIELD_SLP_DATE, null);
        } else if (!isSCCP && updateFieldsMap.containsKey(FIELD_SLP_DATE)) {
            updateFieldsMap.remove(FIELD_SLP_DATE);
        }

        // when grad program is updated from SCCP to something else
        String curProgram = traxStudentEntity.getGradReqtYear();
        if (StringUtils.equalsIgnoreCase(curProgram, "SCCP") && !isSCCP) {
            updateFieldsMap.put(FIELD_SLP_DATE, null);
        }
    }

    @Override
    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    @Override
    public String getEventType() {
        return GRAD_STUDENT_UPDATED.toString();
    }
}