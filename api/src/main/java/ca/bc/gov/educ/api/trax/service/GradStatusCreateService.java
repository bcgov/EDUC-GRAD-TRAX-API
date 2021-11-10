package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.model.dto.GradSearchStudent;
import ca.bc.gov.educ.api.trax.model.dto.GraduationStatus;
import ca.bc.gov.educ.api.trax.model.entity.Event;
import ca.bc.gov.educ.api.trax.model.entity.TraxStudentEntity;
import ca.bc.gov.educ.api.trax.repository.EventRepository;
import ca.bc.gov.educ.api.trax.repository.TraxStudentRepository;
import ca.bc.gov.educ.api.trax.util.EducGradTraxApiConstants;
import ca.bc.gov.educ.api.trax.util.ReplicationUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import java.time.LocalDateTime;

import static ca.bc.gov.educ.api.trax.constant.EventStatus.PROCESSED;
import static ca.bc.gov.educ.api.trax.constant.EventType.CREATE_GRAD_STATUS;

@Service
@Slf4j
public class GradStatusCreateService extends BaseService {
    private final EntityManagerFactory emf;
    private final TraxStudentRepository traxStudentRepository;
    private final EventRepository eventRepository;
    private final EducGradTraxApiConstants constants;

    @Autowired
    public GradStatusCreateService(EntityManagerFactory emf,
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
        GraduationStatus gradStatusCreate = (GraduationStatus) request;

        var existingStudent = traxStudentRepository.findById(gradStatusCreate.getPen());
        final EntityTransaction tx = em.getTransaction();
        try {
            if (existingStudent.isEmpty()
                && constants.isTraxUpdateEnabled()) {
                log.info("==========> Start - Trax New Create: pen# [{}]", gradStatusCreate.getPen());
                TraxStudentEntity traxStudentEntity = new TraxStudentEntity();
                // TODO (jsung)
                // 1. Calls PEN Student API to get pen demographic data to populate TraxStudentEntity
                GradSearchStudent pendemog = new GradSearchStudent();
                populateTraxStudent(traxStudentEntity, pendemog);
                // 2. Needs to transfer required fields from GraduationStatus to TraxStudentEntity
                populateTraxStudent(traxStudentEntity, gradStatusCreate);
                // below timeout is in milli seconds, so it is 10 seconds.
                tx.begin();
                em.createNativeQuery(this.buildInsert(traxStudentEntity)).setHint("javax.persistence.query.timeout", 10000).executeUpdate();
                tx.commit();
                log.info("==========> End - Trax New Create: pen# [{}]", gradStatusCreate.getPen());
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

    private void populateTraxStudent(TraxStudentEntity traxStudentEntity, GradSearchStudent demogStudent) {
        traxStudentEntity.setStudBirth(demogStudent.getDob());
        traxStudentEntity.setStudSex(demogStudent.getSexCode());

        traxStudentEntity.setStudSurname(demogStudent.getLegalLastName());
        traxStudentEntity.setStudGiven(demogStudent.getLegalFirstName());
        traxStudentEntity.setStudMiddle(demogStudent.getLegalMiddleNames());

        traxStudentEntity.setAddress1("");
        traxStudentEntity.setAddress2("");
        traxStudentEntity.setCity("");
        traxStudentEntity.setProvCode("");
        traxStudentEntity.setCntryCode("");
        traxStudentEntity.setPostal("");

    }

    private String buildInsert(TraxStudentEntity traxStudentEntity) {
        String insert = "insert into student_master (archive_flag, stud_no, stud_surname, stud_given, stud_middle, address1, address2, city, prov_code, cntry_code, postal, stud_birth, stud_sex, stud_citiz, stud_grade, mincode," +
                "stud_status, grad_date, dogwood_flag, honour_flag, mincode_grad, french_dogwood, grad_reqt_year, slp_date, grad_reqt_year_at_grad, stud_grade_at_grad, xcript_actv_date) values (" +
                "'" + traxStudentEntity.getArchiveFlag() + "'," +
                "'" + traxStudentEntity.getStudNo() + "'," +
                "'" + ReplicationUtils.getEmptyWhenNull(traxStudentEntity.getStudSurname()) + "'," +
                "'" + ReplicationUtils.getEmptyWhenNull(traxStudentEntity.getStudGiven()) + "'," +
                "'" + ReplicationUtils.getEmptyWhenNull(traxStudentEntity.getStudMiddle()) + "'," +
                "'" + ReplicationUtils.getEmptyWhenNull(traxStudentEntity.getAddress1()) + "'," +
                "'" + ReplicationUtils.getEmptyWhenNull(traxStudentEntity.getAddress2()) + "'," +
                "'" + ReplicationUtils.getEmptyWhenNull(traxStudentEntity.getCity()) + "'," +
                "'" + ReplicationUtils.getEmptyWhenNull(traxStudentEntity.getProvCode()) + "'," +
                "'" + ReplicationUtils.getEmptyWhenNull(traxStudentEntity.getCntryCode()) + "'," +
                "'" + ReplicationUtils.getEmptyWhenNull(traxStudentEntity.getPostal()) + "'," +
                "'" + ReplicationUtils.getEmptyWhenNull(traxStudentEntity.getStudBirth()) + "'," +
                "'" + ReplicationUtils.getEmptyWhenNull(traxStudentEntity.getStudSex()) + "'," +
                "'" + ReplicationUtils.getEmptyWhenNull(traxStudentEntity.getStudCitiz()) + "'," +
                "'" + ReplicationUtils.getBlankWhenNull(traxStudentEntity.getStudGrade()) + "'," +
                "'" + ReplicationUtils.getEmptyWhenNull(traxStudentEntity.getMincode()) + "'," +
                "'" + ReplicationUtils.getEmptyWhenNull(traxStudentEntity.getStudStatus()) + "'," +
                " " + ReplicationUtils.getZeroWhenNull(traxStudentEntity.getGradDate()) + "," +
                "'" + ReplicationUtils.getEmptyWhenNull(traxStudentEntity.getDogwoodFlag()) + "'," +
                "'" + ReplicationUtils.getEmptyWhenNull(traxStudentEntity.getHonourFlag()) + "'," +
                "'" + ReplicationUtils.getBlankWhenNull(traxStudentEntity.getMincodeGrad()) + "'," +
                "'" + ReplicationUtils.getEmptyWhenNull(traxStudentEntity.getFrenchDogwood()) + "'," +
                "'" + ReplicationUtils.getEmptyWhenNull(traxStudentEntity.getGradReqtYear()) + "'," +
                " " + ReplicationUtils.getZeroWhenNull(traxStudentEntity.getSlpDate()) + "," +
                "'" + ReplicationUtils.getEmptyWhenNull(traxStudentEntity.getGradReqtYearAtGrad()) + "'," +
                "'" + ReplicationUtils.getBlankWhenNull(traxStudentEntity.getStudGradeAtGrad()) + "'," +
                traxStudentEntity.getXcriptActvDate() +
              ")";
        log.debug("Create Student_Master: " + insert);
        return insert;
    }

    @Override
    public String getEventType() {
        return CREATE_GRAD_STATUS.toString();
    }
}
