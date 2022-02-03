package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.constant.EventOutcome;
import ca.bc.gov.educ.api.trax.constant.EventType;
import ca.bc.gov.educ.api.trax.messaging.jetstream.Publisher;
import ca.bc.gov.educ.api.trax.model.dto.TraxUpdateInGrad;
import ca.bc.gov.educ.api.trax.model.entity.TraxUpdatedPubEvent;
import ca.bc.gov.educ.api.trax.model.entity.TraxUpdateInGradEntity;
import ca.bc.gov.educ.api.trax.model.transformer.TraxUpdateInGradTransformer;
import ca.bc.gov.educ.api.trax.repository.TraxUpdatedPubEventRepository;
import ca.bc.gov.educ.api.trax.repository.TraxUpdateInGradRepository;
import ca.bc.gov.educ.api.trax.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import net.javacrumbs.shedlock.core.LockAssert;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

import static ca.bc.gov.educ.api.trax.constant.EventStatus.DB_COMMITTED;
import static ca.bc.gov.educ.api.trax.util.EducGradTraxApiConstants.DEFAULT_CREATED_BY;
import static ca.bc.gov.educ.api.trax.util.EducGradTraxApiConstants.DEFAULT_UPDATED_BY;

@Service
public class TraxUpdateService {

    private static Logger logger = LoggerFactory.getLogger(TraxUpdateService.class);

    @Autowired
    private TraxUpdateInGradTransformer traxUpdateInGradTransformer;

    @Autowired
    TraxUpdateInGradRepository traxUpdateInGradRepository;

    @Autowired
    TraxUpdatedPubEventRepository traxUpdatedPubEventRepository;

    @Autowired
    Publisher publisher;

    @Transactional(readOnly = true)
    public List<TraxUpdateInGradEntity> getOutstandingList() {
        return traxUpdateInGradRepository.findOutstandingUpdates(new Date(System.currentTimeMillis()));
    }

    @Transactional(readOnly = true)
    public List<TraxUpdateInGradEntity> getInProgressByPen(String pen) {
        return traxUpdateInGradRepository.findByStatusAndPen("IN_PROGRESS", pen);
    }

    @Scheduled(cron = "${cron.scheduled.process.jobs.stan.run}") // every 5 minute
    @SchedulerLock(name = "PROCESS_TRAX_UPDATE_IN_GRAD_RECORDS", lockAtLeastFor = "${cron.scheduled.process.events.stan.lockAtLeastFor}", lockAtMostFor = "${cron.scheduled.process.events.stan.lockAtMostFor}")
    @Transactional(propagation = Propagation.REQUIRED)
    public void scheduledRunForTraxUpdates() {
        LockAssert.assertLocked();
        try {
            List<TraxUpdateInGradEntity> list = getOutstandingList();
            process(list);
        } catch (Exception ex) {
            logger.error("Unknown exception : {}", ex.getMessage());
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void process(List<TraxUpdateInGradEntity> traxStudents) {
        traxStudents.forEach(entity -> {
            publishTraxUpdatedEvent(entity);

            // update status to PUBLISHED
            entity.setStatus("PUBLISHED");
            traxUpdateInGradRepository.save(entity);
        });
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void publishTraxUpdatedEvent(TraxUpdateInGradEntity traxStudentEntity) {
        // Save TraxUpdatedPubEvent
        TraxUpdatedPubEvent traxUpdatedPubEvent = null;
        try {
            traxUpdatedPubEvent = persistTraxUpdatedEvent(traxStudentEntity);
        } catch (JsonProcessingException ex) {
            logger.error("JSON Processing exception : {}", ex.getMessage());
        }

        // publish NATS message
        publishToJetStream(traxUpdatedPubEvent);
    }

    private TraxUpdatedPubEvent persistTraxUpdatedEvent(TraxUpdateInGradEntity traxStudentEntity) throws JsonProcessingException {
        TraxUpdateInGrad traxStudent = traxUpdateInGradTransformer.transformToDTO(traxStudentEntity);
        String jsonString = JsonUtil.getJsonStringFromObject(traxStudent);
        final TraxUpdatedPubEvent traxUpdatedPubEvent = TraxUpdatedPubEvent.builder()
                .eventType(EventType.UPDATE_TRAX_STUDENT_MASTER.toString())
                .eventId(UUID.randomUUID())
                .eventOutcome(EventOutcome.TRAX_STUDENT_MASTER_UPDATED.toString())
                .activityCode(traxStudent.getUpdateType())
                .eventPayload(jsonString)
                .eventStatus(DB_COMMITTED.toString())
                .createUser(DEFAULT_CREATED_BY)
                .updateUser(DEFAULT_UPDATED_BY)
                .createDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();
        return traxUpdatedPubEventRepository.save(traxUpdatedPubEvent);
    }

    private void publishToJetStream(final TraxUpdatedPubEvent traxUpdatedPubEvent) {
        if (traxUpdatedPubEvent != null) {
            publisher.dispatchChoreographyEvent(traxUpdatedPubEvent);
        }
    }
}
