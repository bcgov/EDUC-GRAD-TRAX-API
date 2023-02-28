package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.constant.FieldType;
import ca.bc.gov.educ.api.trax.model.dto.GradStatusEventPayloadDTO;
import ca.bc.gov.educ.api.trax.model.entity.Event;
import ca.bc.gov.educ.api.trax.model.entity.TraxStudentEntity;
import ca.bc.gov.educ.api.trax.repository.EventRepository;
import ca.bc.gov.educ.api.trax.repository.TraxStudentRepository;
import ca.bc.gov.educ.api.trax.util.EducGradTraxApiConstants;
import ca.bc.gov.educ.api.trax.util.EducGradTraxApiUtils;
import ca.bc.gov.educ.api.trax.util.ReplicationUtils;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.time.LocalDateTime;
import java.util.*;

import static ca.bc.gov.educ.api.trax.constant.EventStatus.PROCESSED;

public abstract class EventCommonService implements EventService {

    private static Logger logger = LoggerFactory.getLogger(EventCommonService.class);

    public static final String FIELD_GRAD_REQT_YEAR = "GRAD_REQT_YEAR";
    public static final String FIELD_GRAD_REQT_YEAR_AT_GRAD = "GRAD_REQT_YEAR_AT_GRAD";
    public static final String FIELD_GRAD_DATE = "GRAD_DATE";
    public static final String FIELD_SLP_DATE = "SLP_DATE";
    public static final String FIELD_MINCODE = "MINCODE";
    public static final String FIELD_MINCODE_GRAD = "MINCODE_GRAD";
    public static final String FIELD_STUD_GRADE = "STUD_GRADE";
    public static final String FIELD_STUD_GRADE_AT_GRAD = "STUD_GRADE_AT_GRAD";
    public static final String FIELD_STUD_STATUS = "STUD_STATUS";
    public static final String FIELD_ARCHIVE_FLAG = "ARCHIVE_FLAG";
    public static final String FIELD_DOGWOOD_FLAG = "DOGWOOD_FLAG";
    public static final String FIELD_ENGLISH_CERT = "ENGLISH_CERT";
    public static final String FIELD_FRENCH_CERT = "FRENCH_CERT";
    public static final String FIELD_HONOUR_FLAG = "HONOUR_FLAG";
    public static final String FIELD_XCRIPT_ACTV_DATE = "XCRIPT_ACTV_DATE";

    @Autowired
    private TraxStudentRepository traxStudentRepository;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private EducGradTraxApiConstants constants;

    @Override
    public <T extends Object> void processEvent(T request, Event event) {
        GradStatusEventPayloadDTO gradStatusUpdate = (GradStatusEventPayloadDTO) request;

        val em = this.getEntityManager();
        var existingStudent = traxStudentRepository.findById(gradStatusUpdate.getPen());
        final EntityTransaction tx = em.getTransaction();
        try {
            process(existingStudent, gradStatusUpdate, em, tx, constants.isTraxUpdateEnabled());

            var existingEvent = eventRepository.findByEventId(event.getEventId());
            existingEvent.ifPresent(eventRecord -> {
                eventRecord.setEventStatus(PROCESSED.toString());
                eventRecord.setUpdateDate(LocalDateTime.now());
                eventRepository.saveAndFlush(eventRecord);
            });
        } catch (Exception e) {
            logger.error("Error occurred saving entity {}", e.getMessage());
            tx.rollback();
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    private void process(Optional<TraxStudentEntity> existingStudent, GradStatusEventPayloadDTO gradStatusUpdate, EntityManager em, EntityTransaction tx, boolean updateTrax) {
        if (updateTrax && existingStudent.isPresent()) {
            logger.debug("==========> Start - Trax Incremental Update: pen# [{}]", gradStatusUpdate.getPen());
            Map<String, Pair<FieldType, Object>> updateFieldsMap = setupUpdateFieldsMap();
            specialHandlingOnUpdateFieldsMap(updateFieldsMap, existingStudent.get(), gradStatusUpdate);
            // Needs to update required fields from GraduationStatus to TraxStudentEntity
            validateAndPopulateUpdateFieldsMap(updateFieldsMap, existingStudent.get(), gradStatusUpdate);
            if (!updateFieldsMap.isEmpty()) {
                // below timeout is in milli seconds, so it is 10 seconds.
                tx.begin();
                em.createNativeQuery(buildUpdateQuery(gradStatusUpdate.getPen(), updateFieldsMap))
                        .setHint("javax.persistence.query.timeout", 10000).executeUpdate();
                tx.commit();
                logger.debug("  === Update Transaction is committed! ===");
            } else {
                logger.debug("  === Skip Transaction as no changes are detected!!! ===");
            }
            logger.debug("==========> End - Trax Incremental Update: pen# [{}]", gradStatusUpdate.getPen());
        }
    }

    // UpdateFieldsMap to keep which TRAX fields need to be updated by Event Type
    private Map<String, Pair<FieldType, Object>> setupUpdateFieldsMap() {
        Map<String, Pair<FieldType, Object>> updateFieldsMap = new HashMap<>();
        if (StringUtils.equalsIgnoreCase(getEventType(), "GRAD_STUDENT_GRADUATED")) {
            // grad_reqt_year_at_grad
            updateFieldsMap.put(FIELD_GRAD_REQT_YEAR_AT_GRAD, null);
            // grad_date
            updateFieldsMap.put(FIELD_GRAD_DATE, null);
            // mincode_grad
            updateFieldsMap.put(FIELD_MINCODE_GRAD, null);
            // stud_grade_at_grad
            updateFieldsMap.put(FIELD_STUD_GRADE_AT_GRAD, null);
            // honour_flag
            updateFieldsMap.put(FIELD_HONOUR_FLAG, null);
        } else if (StringUtils.equalsIgnoreCase(getEventType(), "GRAD_STUDENT_UNDO_COMPLETION")) {
            // grad_reqt_year_at_grad
            updateFieldsMap.put(FIELD_GRAD_REQT_YEAR_AT_GRAD, null);
            // grad_date
            updateFieldsMap.put(FIELD_GRAD_DATE, null);
            // mincode_grad
            updateFieldsMap.put(FIELD_MINCODE_GRAD, null);
            // stud_grade_at_grad
            updateFieldsMap.put(FIELD_STUD_GRADE_AT_GRAD, null);
            // honour_flag
            updateFieldsMap.put(FIELD_HONOUR_FLAG, null);
            // dogwood_flag
            updateFieldsMap.put(FIELD_DOGWOOD_FLAG, null);
            // english_cert
            updateFieldsMap.put(FIELD_ENGLISH_CERT, null);
            // french_cert
            updateFieldsMap.put(FIELD_FRENCH_CERT, null);
        } else if (StringUtils.equalsIgnoreCase(getEventType(), "GRAD_STUDENT_UPDATED")) {
            // grad_reqt_year
            updateFieldsMap.put(FIELD_GRAD_REQT_YEAR, null);
            // slp_date (SCCP)
            updateFieldsMap.put(FIELD_SLP_DATE, null);
            // mincode
            updateFieldsMap.put(FIELD_MINCODE, null);
            // mincode_grad
            updateFieldsMap.put(FIELD_MINCODE_GRAD, null);
            // stud_grade
            updateFieldsMap.put(FIELD_STUD_GRADE, null);
            // stud_status, archive_flag
            updateFieldsMap.put(FIELD_STUD_STATUS, null);
            updateFieldsMap.put(FIELD_ARCHIVE_FLAG, null);
        }
        return updateFieldsMap;
    }

    // initialization or removal
    public abstract void specialHandlingOnUpdateFieldsMap(Map<String, Pair<FieldType, Object>> updateFieldsMap, TraxStudentEntity traxStudentEntity, GradStatusEventPayloadDTO gradStatusUpdate);

    public abstract EntityManager getEntityManager();

    // validate the updated fields by comparing the values between GRAD and TRAX
    protected void validateAndPopulateUpdateFieldsMap(Map<String, Pair<FieldType, Object>> updateFieldsMap, TraxStudentEntity traxStudentEntity, GradStatusEventPayloadDTO gradStatus) {
        if (updateFieldsMap.isEmpty()) {
            return;
        }
        Set<String> fields = updateFieldsMap.keySet();
        // GRAD Requested Year
        if (fields.contains(FIELD_GRAD_REQT_YEAR)) {
            handleStringField(updateFieldsMap, FIELD_GRAD_REQT_YEAR, traxStudentEntity.getGradReqtYear(), convertProgramToYear(gradStatus.getProgram()));
        }
        // GRAD Requested Year At Grad
        if (fields.contains(FIELD_GRAD_REQT_YEAR_AT_GRAD)) {
            handleStringField(updateFieldsMap, FIELD_GRAD_REQT_YEAR_AT_GRAD, traxStudentEntity.getGradReqtYearAtGrad(), convertProgramToYear(gradStatus.getProgram()));
        }
        // GRAD Date
        if (fields.contains(FIELD_GRAD_DATE)) {
            handleDateField(updateFieldsMap, FIELD_GRAD_DATE, traxStudentEntity.getGradDate(), gradStatus.getProgramCompletionDate());
        }
        // SLP Date
        if (fields.contains(FIELD_SLP_DATE)) {
            handleDateField(updateFieldsMap, FIELD_SLP_DATE, traxStudentEntity.getSlpDate(), gradStatus.getProgramCompletionDate());
        }
        // Mincode
        if (fields.contains(FIELD_MINCODE)) {
            handleStringField(updateFieldsMap, FIELD_MINCODE, traxStudentEntity.getMincode(), gradStatus.getSchoolOfRecord());
        }
        // Mincode_Grad
        if (fields.contains(FIELD_MINCODE_GRAD)) {
            handleStringField(updateFieldsMap, FIELD_MINCODE_GRAD, traxStudentEntity.getMincodeGrad(), gradStatus.getSchoolAtGrad());
        }
        // Student Grade
        if (fields.contains(FIELD_STUD_GRADE)) {
            handleStringField(updateFieldsMap, FIELD_STUD_GRADE, traxStudentEntity.getStudGrade(), gradStatus.getStudentGrade());
        }
        // Student Grade At Grad
        if (fields.contains(FIELD_STUD_GRADE_AT_GRAD)) {
            handleStringField(updateFieldsMap, FIELD_STUD_GRADE_AT_GRAD, traxStudentEntity.getStudGradeAtGrad(), gradStatus.getStudentGrade());
        }
        // Student Status
        if (fields.contains(FIELD_STUD_STATUS)) {
            checkAndPopulateTraxStudentStatus(traxStudentEntity, gradStatus.getStudentStatus(), updateFieldsMap);
        }
        // Honour Flag
        if (fields.contains(FIELD_HONOUR_FLAG)) {
            handleStringField(updateFieldsMap, FIELD_HONOUR_FLAG, traxStudentEntity.getHonourFlag(), gradStatus.getHonoursStanding());
        }

        if (!updateFieldsMap.isEmpty()) {
            // Current Date
            String currentDateStr = EducGradTraxApiUtils.formatDate(new Date(), EducGradTraxApiConstants.TRAX_DATE_FORMAT);
            if (NumberUtils.isDigits(currentDateStr)) {
                updateFieldsMap.put(FIELD_XCRIPT_ACTV_DATE, Pair.of(FieldType.TRAX_DATE, Long.valueOf(currentDateStr)));
            }
            // Update User
        }
    }

    protected String convertProgramToYear(String program) {
        String ret = " ";
        if (StringUtils.startsWith(program, "2018")) {
            ret = "2018";
        } else if (StringUtils.startsWith(program, "2004")) {
            ret = "2004";
        } else if (StringUtils.startsWith(program, "1996")) {
            ret = "1996";
        } else if (StringUtils.startsWith(program, "1986")) {
            ret = "1986";
        } else if (StringUtils.startsWith(program, "1950")
                || StringUtils.startsWith(program, "NOPROG")) {
            ret = "1950";
        } else if (StringUtils.startsWith(program, "SCCP")) {
            ret = "SCCP";
        }
        return ret;
    }

    private String buildUpdateQuery(String pen, Map<String, Pair<FieldType, Object>> updateFieldsMap) {
        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE STUDENT_MASTER SET ");

        Iterator<String> ir = updateFieldsMap.keySet().iterator();
        while(ir.hasNext()) {
            String key = ir.next();
            Pair<FieldType, Object> pair = updateFieldsMap.get(key);

            if (pair.getLeft() == FieldType.TRAX_DATE) {
                Long value = (Long) pair.getRight();
                sb.append(getUpdateFieldForLong(key, value, !ir.hasNext()));
            } else {
                String value = (String) pair.getRight();
                sb.append(getUpdateFieldForString(key, value, !ir.hasNext()));
            }
        }

        sb.append(" WHERE STUD_NO=" + "'" + StringUtils.rightPad(pen, 10) + "'"); // a space is appended CAREFUL not to remove.

        logger.debug("Update Query: {}",  sb);
        return sb.toString();

    }

    private void checkAndPopulateTraxStudentStatus(TraxStudentEntity traxStudentEntity, String gradStudentStatus, Map<String, Pair<FieldType, Object>> updateFieldsMap) {
        String studStatus = traxStudentEntity.getStudStatus();
        String archivedFlag = traxStudentEntity.getArchiveFlag();

        String newStudStatus = null;
        String newArchiveFlag = null;

        if (StringUtils.equals(gradStudentStatus, "CUR")) {
            newStudStatus = "A";
            newArchiveFlag = "A";
        } else if (StringUtils.equals(gradStudentStatus, "ARC")) {
            newStudStatus = "A";
            newArchiveFlag = "I";
        } else if (StringUtils.equals(gradStudentStatus, "DEC")) {
            newStudStatus = "D";
            newArchiveFlag = "I";
        } else if (StringUtils.equals(gradStudentStatus, "MER")) {
            newStudStatus = "M";
            newArchiveFlag = "I";
        } else if (StringUtils.equals(gradStudentStatus, "TER")) {
            newStudStatus = "T";
            newArchiveFlag = "I";
        }

        if (!StringUtils.equalsIgnoreCase(studStatus, newStudStatus)) {
            updateFieldsMap.put(FIELD_STUD_STATUS, Pair.of(FieldType.TRAX_STRING, newStudStatus));
        } else {
            updateFieldsMap.remove(FIELD_STUD_STATUS);
        }

        if (!StringUtils.equalsIgnoreCase(archivedFlag, newArchiveFlag)) {
            updateFieldsMap.put(FIELD_ARCHIVE_FLAG, Pair.of(FieldType.TRAX_STRING, newArchiveFlag));
        } else {
            updateFieldsMap.remove(FIELD_ARCHIVE_FLAG);
        }
    }

    private void handleStringField(Map<String, Pair<FieldType, Object>> updateFieldsMap, final String fieldName, final String currentValue, final String newValue) {
        String gradValue = ReplicationUtils.getBlankWhenNull(newValue);
        String traxValue  = ReplicationUtils.getBlankWhenNull(currentValue);
        if (!StringUtils.equalsIgnoreCase(gradValue, traxValue)) {
            updateFieldsMap.put(fieldName, Pair.of(FieldType.TRAX_STRING, gradValue));
        } else {
            updateFieldsMap.remove(fieldName);
        }
    }

    private void handleDateField(Map<String, Pair<FieldType, Object>> updateFieldsMap, final String fieldName, final Long currentDate, final String newDateStr) {
        if (StringUtils.isNotBlank(newDateStr)) {
            compareAndSetDate(updateFieldsMap, fieldName, currentDate, newDateStr);
        } else if (currentDate != null && currentDate != 0L) {
            updateFieldsMap.put(fieldName, Pair.of(FieldType.TRAX_DATE, Long.valueOf("0")));
        } else {
            updateFieldsMap.remove(fieldName);
        }
    }

    /**
     *
     * @param updateFieldsMap
     * @param fieldName
     * @param currentDate       trax date as long value
     * @param newDateStr        date string as format of "yyyy-MM-DD"
     */
    private void compareAndSetDate(Map<String, Pair<FieldType, Object>> updateFieldsMap, final String fieldName, final Long currentDate, final String newDateStr) {
        String gradDateStr = newDateStr.replace("-", "");
        if (updateFieldsMap.keySet().contains(FIELD_GRAD_DATE)) { // grad_date is yyyyMM, otherwise yyyyMMDD
            gradDateStr = gradDateStr.substring(0,6);
        }
        if (NumberUtils.isDigits(gradDateStr) && NumberUtils.compare(Long.valueOf(gradDateStr), currentDate) != 0) {
            updateFieldsMap.put(fieldName, Pair.of(FieldType.TRAX_DATE, Long.valueOf(gradDateStr)));
        } else {
            updateFieldsMap.remove(fieldName);
        }
    }

    private String getUpdateFieldForString(String key, String value, boolean isLastField) {
        StringBuilder sb = new StringBuilder();
        sb.append(key);
        sb.append("='");
        sb.append(value);
        sb.append("'");
        if (!isLastField) {
            sb.append(",");
        }
        return sb.toString();
    }

    private String getUpdateFieldForLong(String key, Long value, boolean isLastField) {
        StringBuilder sb = new StringBuilder();
        sb.append(key);
        sb.append("=");
        sb.append(value);
        if (!isLastField) {
            sb.append(",");
        }
        return sb.toString();
    }
}
