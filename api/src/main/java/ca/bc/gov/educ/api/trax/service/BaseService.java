package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.constant.FieldType;
import ca.bc.gov.educ.api.trax.model.dto.GradStatusEventPayloadDTO;
import ca.bc.gov.educ.api.trax.model.entity.TraxStudentEntity;
import ca.bc.gov.educ.api.trax.util.EducGradTraxApiConstants;
import ca.bc.gov.educ.api.trax.util.EducGradTraxApiUtils;
import ca.bc.gov.educ.api.trax.util.ReplicationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.*;

public abstract class BaseService implements EventService {

    private static Logger logger = LoggerFactory.getLogger(BaseService.class);

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
    public static final String FIELD_HONOUR_FLAG = "HONOUR_FLAG";
    public static final String FIELD_XCRIPT_ACTV_DATE = "XCRIPT_ACTV_DATE";

    protected void process(Optional<TraxStudentEntity> existingStudent, GradStatusEventPayloadDTO gradStatusUpdate, EntityManager em, EntityTransaction tx, boolean updateTrax) {
        if (updateTrax && existingStudent.isPresent()) {
            logger.info("==========> Start - Trax Incremental Update: pen# [{}]", gradStatusUpdate.getPen());
            Map<String, Pair<FieldType, Object>> updateFieldsMap = initializeUpdateFieldsMap();
            specialHandlingOnUpdateFieldsMap(updateFieldsMap, existingStudent.get(), gradStatusUpdate);
            // Needs to update required fields from GraduationStatus to TraxStudentEntity
            validateAndPopulateUpdateFieldsMap(updateFieldsMap, existingStudent.get(), gradStatusUpdate);
            if (!updateFieldsMap.isEmpty()) {
                // below timeout is in milli seconds, so it is 10 seconds.
                tx.begin();
                em.createNativeQuery(buildUpdateQuery(gradStatusUpdate.getPen(), updateFieldsMap))
                        .setHint("javax.persistence.query.timeout", 10000).executeUpdate();
                tx.commit();
                logger.info("  === Update Transaction is committed! ===");
            } else {
                logger.info("  === Skip Transaction as no changes are detected!!! ===");
            }
            logger.info("==========> End - Trax Incremental Update: pen# [{}]", gradStatusUpdate.getPen());
        }
    }

    public abstract Map<String, Pair<FieldType, Object>> initializeUpdateFieldsMap();

    // remove
    public abstract void specialHandlingOnUpdateFieldsMap(Map<String, Pair<FieldType, Object>> updateFieldsMap, TraxStudentEntity traxStudentEntity, GradStatusEventPayloadDTO gradStatusUpdate);

    protected void validateAndPopulateUpdateFieldsMap(Map<String, Pair<FieldType, Object>> updateFieldsMap, TraxStudentEntity traxStudentEntity, GradStatusEventPayloadDTO gradStatus) {
        Set<String> fields = updateFieldsMap.keySet();
        // GRAD Requested Year
        if (fields.contains(FIELD_GRAD_REQT_YEAR)) {
            String year = convertProgramToYear(gradStatus.getProgram());
            if (!StringUtils.equalsIgnoreCase(year, traxStudentEntity.getGradReqtYear())) {
                updateFieldsMap.put(FIELD_GRAD_REQT_YEAR, Pair.of(FieldType.TRAX_STRING, year));
            } else {
                updateFieldsMap.remove(FIELD_GRAD_REQT_YEAR);
            }
        }
        // GRAD Requested Year At Grad
        if (fields.contains(FIELD_GRAD_REQT_YEAR_AT_GRAD)) {
            String year = convertProgramToYear(gradStatus.getProgram());
            if (!StringUtils.equalsIgnoreCase(year, traxStudentEntity.getGradReqtYearAtGrad())) {
                updateFieldsMap.put(FIELD_GRAD_REQT_YEAR_AT_GRAD, Pair.of(FieldType.TRAX_STRING, year));
            } else {
                updateFieldsMap.remove(FIELD_GRAD_REQT_YEAR_AT_GRAD);
            }
        }
        // GRAD Date
        if (fields.contains(FIELD_GRAD_DATE)) {
            if (StringUtils.isNotBlank(gradStatus.getProgramCompletionDate())) {
                String gradDateStr = gradStatus.getProgramCompletionDate().replace("/", "");
                if (NumberUtils.isDigits(gradDateStr) && NumberUtils.compare(Long.valueOf(gradDateStr), traxStudentEntity.getGradDate()) != 0) {
                    updateFieldsMap.put(FIELD_GRAD_DATE, Pair.of(FieldType.TRAX_DATE, Long.valueOf(gradDateStr)));
                } else {
                    updateFieldsMap.remove(FIELD_GRAD_DATE);
                }
            } else if (traxStudentEntity.getGradDate() != null && traxStudentEntity.getGradDate() != 0L) {
                updateFieldsMap.put(FIELD_GRAD_DATE, Pair.of(FieldType.TRAX_DATE, Long.valueOf("0")));
            } else {
                updateFieldsMap.remove(FIELD_GRAD_DATE);
            }
        }
        // SLP Date
        if (fields.contains(FIELD_SLP_DATE)) {
            if (StringUtils.isNotBlank(gradStatus.getProgramCompletionDate())) {
                String slpDateStr = gradStatus.getProgramCompletionDate().replace("/", "");
                if (NumberUtils.isDigits(slpDateStr) && NumberUtils.compare(Long.valueOf(slpDateStr), traxStudentEntity.getSlpDate()) != 0) {
                    updateFieldsMap.put(FIELD_SLP_DATE, Pair.of(FieldType.TRAX_DATE, Long.valueOf(slpDateStr)));
                } else {
                    updateFieldsMap.remove(FIELD_SLP_DATE);
                }
            } else if (traxStudentEntity.getSlpDate() != null && traxStudentEntity.getSlpDate() != 0L) {
                updateFieldsMap.put(FIELD_SLP_DATE,Pair.of(FieldType.TRAX_DATE, Long.valueOf("0")));
            } else {
                updateFieldsMap.remove(FIELD_SLP_DATE);
            }
        }
        // Mincode
        if (fields.contains(FIELD_MINCODE)) {
            String newMincode = ReplicationUtils.getBlankWhenNull(gradStatus.getSchoolOfRecord());
            String curMincode = ReplicationUtils.getBlankWhenNull(traxStudentEntity.getMincode());
            if (!StringUtils.equalsIgnoreCase(newMincode, curMincode)) {
                updateFieldsMap.put(FIELD_MINCODE, Pair.of(FieldType.TRAX_STRING, newMincode));
            } else {
                updateFieldsMap.remove(FIELD_MINCODE);
            }
        }
        // Mincode_Grad
        if (fields.contains(FIELD_MINCODE_GRAD)) {
            String newMincodeGrad = ReplicationUtils.getBlankWhenNull(gradStatus.getSchoolAtGrad());
            String curMincodeGrad = ReplicationUtils.getBlankWhenNull(traxStudentEntity.getMincodeGrad());
            if (!StringUtils.equalsIgnoreCase(newMincodeGrad, curMincodeGrad)) {
                updateFieldsMap.put(FIELD_MINCODE_GRAD, Pair.of(FieldType.TRAX_STRING, newMincodeGrad));
            } else {
                updateFieldsMap.remove(FIELD_MINCODE_GRAD);
            }
        }
        // Student Grade
        if (fields.contains(FIELD_STUD_GRADE)) {
            String newGrade = ReplicationUtils.getBlankWhenNull(gradStatus.getStudentGrade());
            String curGrade = ReplicationUtils.getBlankWhenNull(traxStudentEntity.getStudGrade());
            if (!StringUtils.equalsIgnoreCase(newGrade, curGrade)) {
                updateFieldsMap.put(FIELD_STUD_GRADE, Pair.of(FieldType.TRAX_STRING, newGrade));
            } else {
                updateFieldsMap.remove(FIELD_STUD_GRADE);
            }
        }
        // Student Grade At Grad
        if (fields.contains(FIELD_STUD_GRADE_AT_GRAD)) {
            String newGradeAtGrad = ReplicationUtils.getBlankWhenNull(gradStatus.getStudentGrade());
            String curGradeAtGrad = ReplicationUtils.getBlankWhenNull(traxStudentEntity.getStudGradeAtGrad());
            if (!StringUtils.equalsIgnoreCase(newGradeAtGrad, curGradeAtGrad)) {
                updateFieldsMap.put(FIELD_STUD_GRADE_AT_GRAD, Pair.of(FieldType.TRAX_STRING, newGradeAtGrad));
            } else {
                updateFieldsMap.remove(FIELD_STUD_GRADE_AT_GRAD);
            }
        }
        // Student Status
        if (fields.contains(FIELD_STUD_STATUS)) {
            checkAndPopulateTraxStudentStatus(traxStudentEntity, gradStatus.getStudentStatus(), updateFieldsMap);
        }
        // Honour Flag
        if (fields.contains(FIELD_HONOUR_FLAG)) {
            String newHonourFlag = ReplicationUtils.getBlankWhenNull(gradStatus.getHonoursStanding());
            String curHonourFlag = ReplicationUtils.getBlankWhenNull(traxStudentEntity.getHonourFlag());
            if (!StringUtils.equalsIgnoreCase(newHonourFlag, curHonourFlag)) {
                updateFieldsMap.put(FIELD_HONOUR_FLAG, Pair.of(FieldType.TRAX_STRING, newHonourFlag));
            } else {
                updateFieldsMap.remove(FIELD_HONOUR_FLAG);
            }
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

        logger.debug("Update Student_Master: " + sb.toString());
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
