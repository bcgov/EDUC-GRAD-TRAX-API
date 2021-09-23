package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.model.dto.GradSearchStudent;
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
import static ca.bc.gov.educ.api.trax.model.dto.EventType.CREATE_GRAD_STATUS;

@Service
@Slf4j
public class GradStatusCreateService extends BaseService {
    private final EntityManagerFactory emf;
    private final TraxStudentRepository traxStudentRepository;
    private final EventRepository eventRepository;

    @Autowired
    public GradStatusCreateService(EntityManagerFactory emf, TraxStudentRepository traxStudentRepository, EventRepository eventRepository) {
        this.emf = emf;
        this.traxStudentRepository = traxStudentRepository;
        this.eventRepository = eventRepository;
    }

    @Override
    public <T extends Object> void processEvent(T request, Event event) {
        val em = this.emf.createEntityManager();
        GraduationStatus gradStatusCreate = (GraduationStatus) request;

        var existingStudent = traxStudentRepository.findById(gradStatusCreate.getPen());
        final EntityTransaction tx = em.getTransaction();
        try {
            if (existingStudent.isEmpty()) {
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

        traxStudentEntity.setStudNo(StringUtils.rightPad(gradStatus.getPen(), 10));
        traxStudentEntity.setArchiveFlag("A");
        traxStudentEntity.setHonourFlag(gradStatus.getHonoursStanding());
    }

    private String buildInsert(TraxStudentEntity traxStudentEntity) {
        String insert = "insert into student_master (archive_flag, stud_no, stud_surname, stud_given, stud_middle, address1, address2, city, prov_code, cntry_code, postal, stud_birth, stud_sex, stud_citiz, stud_grade, mincode," +
                "stud_status, grad_date, dogwood_flag, honour_flag, mincode_grad, french_dogwood, grad_reqt_year, slp_date, stud_reqt_year_at_grad, stud_grade_at_grad) values (" +
                "'" + traxStudentEntity.getArchiveFlag() + "'," +
                "'" + traxStudentEntity.getStudNo() + "'," +
                "'" + traxStudentEntity.getStudSurname() + "'," +
                "'" + (traxStudentEntity.getStudGiven() == null? "" : traxStudentEntity.getStudGiven()) + "'," +
                "'" + (traxStudentEntity.getStudMiddle() == null? "" : traxStudentEntity.getStudMiddle()) + "'," +
                "'" + (traxStudentEntity.getAddress1() == null? "" : traxStudentEntity.getAddress1()) + "'," +
                "'" + (traxStudentEntity.getAddress2() == null? "" : traxStudentEntity.getAddress2()) + "'," +
                "'" + (traxStudentEntity.getCity() == null? "" : traxStudentEntity.getCity()) + "'," +
                "'" + (traxStudentEntity.getProvCode() == null? "" : traxStudentEntity.getProvCode()) + "'," +
                "'" + (traxStudentEntity.getCntryCode() == null? "" : traxStudentEntity.getCntryCode()) + "'," +
                "'" + (traxStudentEntity.getPostal() == null? "" : traxStudentEntity.getPostal()) + "'," +
                "'" + traxStudentEntity.getStudBirth() + "'," +
                "'" + traxStudentEntity.getStudSex() + "'," +
                "'" + traxStudentEntity.getStudCitiz() + "'," +
                "'" + ReplicationUtils.getBlankWhenNull(traxStudentEntity.getStudGrade()) + "'," +
                "'" + (traxStudentEntity.getMincode() == null? "" : traxStudentEntity.getMincode()) + "'," +
                "'" + (traxStudentEntity.getStudStatus() == null ? "" : traxStudentEntity.getStudStatus()) + "'," +
                " " + traxStudentEntity.getGradDate() + "," +
                "'" + traxStudentEntity.getDogwoodFlag() + "'," +
                "'" + traxStudentEntity.getHonourFlag() + "'," +
                "'" + ReplicationUtils.getBlankWhenNull(traxStudentEntity.getMincodeGrad()) + "'," +
                "'" + traxStudentEntity.getFrenchDogwood() + "'," +
                "'" + traxStudentEntity.getGradReqtYear() + "'," +
                " " + traxStudentEntity.getSlpDate() + "," +
                "'" + traxStudentEntity.getGradReqtYearAtGrad() + "'," +
                "'" + ReplicationUtils.getBlankWhenNull(traxStudentEntity.getStudGradeAtGrad()) +
              ")";
        log.debug("Create Student_Master: " + insert);
        return insert;
    }

    @Override
    public String getEventType() {
        return CREATE_GRAD_STATUS.toString();
    }
}
