package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.model.dto.GradStatusEventPayloadDTO;
import ca.bc.gov.educ.api.trax.model.dto.GraduationStatus;
import ca.bc.gov.educ.api.trax.model.entity.Event;
import ca.bc.gov.educ.api.trax.model.entity.TraxStudentEntity;
import ca.bc.gov.educ.api.trax.repository.EventRepository;
import ca.bc.gov.educ.api.trax.repository.TraxStudentRepository;
import ca.bc.gov.educ.api.trax.util.EducGradTraxApiConstants;
import ca.bc.gov.educ.api.trax.util.ReplicationUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import java.time.LocalDateTime;
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
        val em = this.emf.createEntityManager();
        GradStatusEventPayloadDTO gradStatusUpdate = (GradStatusEventPayloadDTO) request;

        var existingStudent = traxStudentRepository.findById(gradStatusUpdate.getPen());

        final EntityTransaction tx = em.getTransaction();
        try {
            if (existingStudent.isPresent()
                && constants.isTraxUpdateEnabled()) {
                log.info("==========> Start - Trax Incremental Update: pen# [{}]", gradStatusUpdate.getPen());
                // Needs to update required fields from GraduationStatus to TraxStudentEntity
                Map<String, Object> updateFieldsMap = buildUpdateFieldsMap(existingStudent.get(), gradStatusUpdate);
                if (!updateFieldsMap.isEmpty()) {
                    // below timeout is in milli seconds, so it is 10 seconds.
                    tx.begin();
                    em.createNativeQuery(this.buildUpdateQuery(gradStatusUpdate.getPen(), updateFieldsMap))
                            .setHint("javax.persistence.query.timeout", 10000).executeUpdate();
                    tx.commit();
                    log.info("  === Update Transaction is committed! ===");
                } else {
                    log.info("  === Skip Transaction as no changes are detected!!! ===");
                }
                log.info("==========> End - Trax Incremental Update: pen# [{}]", gradStatusUpdate.getPen());
            }

            var existingEvent = eventRepository.findByEventId(event.getEventId());
            existingEvent.ifPresent(eventRecord -> {
                eventRecord.setEventStatus(PROCESSED.toString());
                eventRecord.setUpdateDate(LocalDateTime.now());
                eventRepository.save(eventRecord);
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
    public String getEventType() {
        return GRAD_STUDENT_UNDO_COMPLETION.toString();
    }
}