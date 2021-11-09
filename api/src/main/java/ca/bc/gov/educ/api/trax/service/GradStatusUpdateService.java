package ca.bc.gov.educ.api.trax.service;

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
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import java.time.LocalDateTime;

import static ca.bc.gov.educ.api.trax.constant.EventStatus.PROCESSED;
import static ca.bc.gov.educ.api.trax.constant.EventType.UPDATE_GRAD_STATUS;

@Service
@Slf4j
public class GradStatusUpdateService extends BaseService {
    private final EntityManagerFactory emf;
    private final TraxStudentRepository traxStudentRepository;
    private final EventRepository eventRepository;
    private final EducGradTraxApiConstants constants;

    @Autowired
    public GradStatusUpdateService( EntityManagerFactory emf,
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
        GraduationStatus gradStatusUpdate = (GraduationStatus) request;

        var existingStudent = traxStudentRepository.findById(gradStatusUpdate.getPen());

        final EntityTransaction tx = em.getTransaction();
        try {
            if (existingStudent.isPresent()
                && constants.isTraxUpdateEnabled()) {
                log.info("==========> Start - Trax Incremental Update: pen# [{}]", gradStatusUpdate.getPen());
                TraxStudentEntity traxStudentEntity = existingStudent.get();
                // Needs to update required fields from GraduationStatus to TraxStudentEntity
                populateTraxStudent(traxStudentEntity, gradStatusUpdate);
                // below timeout is in milli seconds, so it is 10 seconds.
                tx.begin();
                em.createNativeQuery(this.buildUpdate(traxStudentEntity.getGradReqtYear(),
                        traxStudentEntity.getGradDate(), traxStudentEntity.getMincode(), traxStudentEntity.getMincodeGrad(),
                        traxStudentEntity.getStudGrade(), traxStudentEntity.getStudStatus(), traxStudentEntity.getArchiveFlag(),
                        traxStudentEntity.getHonourFlag(), traxStudentEntity.getXcriptActvDate(),
                        traxStudentEntity.getStudNo())).setHint("javax.persistence.query.timeout", 10000).executeUpdate();
                tx.commit();
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

    private String buildUpdate(final String gradReqtYear, final Long gradDate, final String mincode, final String mincodeGrad,
                               final String studGrade, final String studStatus, final String archiveFlag, final String honourFlag,
                               final Long activeDate, final String studNo) {
        String update = "UPDATE STUDENT_MASTER SET "
                + "GRAD_REQT_YEAR=" + "'" + gradReqtYear + "'" + ","
                + "GRAD_DATE=" + gradDate + ","
                + "MINCODE=" + "'" + (mincode == null ? "" :mincode) + "'" + ","
                + "MINCODE_GRAD=" + "'" + (mincodeGrad == null ? "" :mincodeGrad) + "'" + ","
                + "STUD_GRADE=" + "'" + ReplicationUtils.getBlankWhenNull(studGrade) + "'" + ","
                + "STUD_STATUS=" + "'" + (studStatus == null ? "" : studStatus) + "'" + ","
                + "ARCHIVE_FLAG=" + "'" + (archiveFlag == null ? "" : archiveFlag) + "'" + ","
                + "HONOUR_FLAG=" + "'" + (honourFlag == null ? "" : honourFlag) + "'" + ","
                + "XCRIPT_ACTV_DATE=" + activeDate
                + " WHERE STUD_NO=" + "'" + StringUtils.rightPad(studNo, 10) + "'"; // a space is appended CAREFUL not to remove.
        log.debug("Update Student_Master: " + update);
        return update;
    }

    @Override
    public String getEventType() {
        return UPDATE_GRAD_STATUS.toString();
    }
}