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

import static ca.bc.gov.educ.api.trax.constant.EventType.GRAD_STUDENT_GRADUATED;

@Service
@Slf4j
public class GradStudentGraduatedService extends EventCommonService {
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
            log.info("  Skip the graduation update against SCCP student in TRAX");
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