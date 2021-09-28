package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.model.dto.GraduationStatus;
import ca.bc.gov.educ.api.trax.model.entity.Event;
import ca.bc.gov.educ.api.trax.model.entity.TraxStudentEntity;
import ca.bc.gov.educ.api.trax.repository.EventRepository;
import ca.bc.gov.educ.api.trax.repository.TraxStudentRepository;
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

    @Autowired
    public GradStatusUpdateService( EntityManagerFactory emf, TraxStudentRepository traxStudentRepository, EventRepository eventRepository) {
        this.emf = emf;
        this.traxStudentRepository = traxStudentRepository;
        this.eventRepository = eventRepository;
    }

    @Override
    public <T extends Object> void processEvent(T request, Event event) {
        val em = this.emf.createEntityManager();
        GraduationStatus gradStatusUpdate = (GraduationStatus) request;

        var existingStudent = traxStudentRepository.findById(gradStatusUpdate.getPen());

        final EntityTransaction tx = em.getTransaction();
        try {
            if (existingStudent.isPresent()) {
                TraxStudentEntity traxStudentEntity = existingStudent.get();
                // Needs to update required fields from GraduationStatus to TraxStudentEntity
                populateTraxStudent(traxStudentEntity, gradStatusUpdate);
                // below timeout is in milli seconds, so it is 10 seconds.
                tx.begin();
                em.createNativeQuery(this.buildUpdate(traxStudentEntity.getGradReqtYear(),
                        traxStudentEntity.getGradDate(), traxStudentEntity.getMincode(),
                        traxStudentEntity.getStudGrade(), traxStudentEntity.getStudStatus(),
                        traxStudentEntity.getStudNo())).setHint("javax.persistence.query.timeout", 10000).executeUpdate();
                tx.commit();
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

    private void populateTraxStudent(TraxStudentEntity traxStudentEntity, GraduationStatus gradStatus) {
        // Needs to update required fields from GraduationStatus to TraxStudentEntity
        if (StringUtils.isNotBlank(gradStatus.getProgram())) {
            String year = convertProgramToYear(gradStatus.getProgram());
            if (year != null) {
                traxStudentEntity.setGradReqtYear(year);
            }
        }
        if (StringUtils.isNotBlank(gradStatus.getProgramCompletionDate())) {
            String gradDateStr = gradStatus.getProgramCompletionDate().replace("/", "");
            if (NumberUtils.isDigits(gradDateStr)) {
                traxStudentEntity.setGradDate(Long.valueOf(gradDateStr));
            }
        } else {
            traxStudentEntity.setGradDate(0L);
        }
        traxStudentEntity.setMincode(gradStatus.getSchoolOfRecord());
        traxStudentEntity.setStudGrade(gradStatus.getStudentGrade());
        traxStudentEntity.setStudStatus(gradStatus.getStudentStatus());
    }

    private String buildUpdate(final String gradReqtYear, final Long gradDate, final String mincode, final String studGrade, final String studStatus, final String studNo) {
        String update = "UPDATE STUDENT_MASTER SET "
                + "GRAD_REQT_YEAR=" + "'" + gradReqtYear + "'" + ","
                + "GRAD_DATE=" + gradDate + ","
                + "MINCODE=" + "'" + (mincode == null ? "" :mincode) + "'" + ","
                + "STUD_GRADE=" + "'" + ReplicationUtils.getBlankWhenNull(studGrade) + "'" + ","
                + "STUD_STATUS=" + "'" + (studStatus == null ? "" : studStatus) + "'"
                + " WHERE STUD_NO=" + "'" + StringUtils.rightPad(studNo, 10) + "'"; // a space is appended CAREFUL not to remove.
        log.debug("Update Student_Master: " + update);
        return update;
    }

    @Override
    public String getEventType() {
        return UPDATE_GRAD_STATUS.toString();
    }
}