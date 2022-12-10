package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.model.dto.GradStatusEventPayloadDTO;
import ca.bc.gov.educ.api.trax.model.entity.TraxStudentEntity;
import ca.bc.gov.educ.api.trax.util.EducGradTraxApiConstants;
import ca.bc.gov.educ.api.trax.util.EducGradTraxApiUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class BaseService implements EventService {

    public static final String FIELD_GRAD_REQT_YEAR = "GRAD_REQT_YEAR";
    public static final String FIELD_GRAD_DATE = "GRAD_DATE";
    public static final String FIELD_MINCODE = "MINCODE";
    public static final String FIELD_MINCODE_GRAD = "MINCODE_GRAD";
    public static final String FIELD_STUD_GRADE = "STUD_GRADE";
    public static final String FIELD_STUD_STATUS = "STUD_STATUS";
    public static final String FIELD_ARCHIVE_FLAG = "ARCHIVE_FLAG";
    public static final String FIELD_HONOUR_FLAG = "HONOUR_FLAG";
    public static final String FIELD_XCRIPT_ACTV_DATE = "XCRIPT_ACTV_DATE";

//    abstract Map<String, Object> validateUpdateFieldsMap(Map<String, Object> updateFieldsMap);
//
//    protected  void processTransaction() {
//
//    }

    protected String convertProgramToYear(String program) {
        String ret = null;
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

    protected String buildUpdateQuery(String pen, Map<String, Object> updateFieldsMap) {
        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE STUDENT_MASTER SET ");

        Set<String> fields = updateFieldsMap.keySet();
        // GRAD_REQT_YEAR
        if (fields.contains(FIELD_GRAD_REQT_YEAR)) {
            String value = (String) updateFieldsMap.get(FIELD_GRAD_REQT_YEAR);
            sb.append(getUpdateFieldForString(FIELD_GRAD_REQT_YEAR, value, false));
        }
        // GRAD_DATE
        if (fields.contains(FIELD_GRAD_DATE)) {
            Long value = (Long) updateFieldsMap.get(FIELD_GRAD_DATE);
            sb.append(getUpdateFieldForLong(FIELD_GRAD_DATE, value, false));
        }
        // MINCODE
        if (fields.contains(FIELD_MINCODE)) {
            String value = (String) updateFieldsMap.get(FIELD_MINCODE);
            sb.append(getUpdateFieldForString(FIELD_MINCODE, value, false));
        }
        // MINCODE_GRAD
        if (fields.contains(FIELD_MINCODE_GRAD)) {
            String value = (String) updateFieldsMap.get(FIELD_MINCODE_GRAD);
            sb.append(getUpdateFieldForString(FIELD_MINCODE_GRAD, value, false));
        }
        // STUD_GRADE
        if (fields.contains(FIELD_STUD_GRADE)) {
            String value = (String) updateFieldsMap.get(FIELD_STUD_GRADE);
            sb.append(getUpdateFieldForString(FIELD_STUD_GRADE, value, false));
        }
        // STUD_STATUS
        if (fields.contains(FIELD_STUD_STATUS)) {
            String value = (String) updateFieldsMap.get(FIELD_STUD_STATUS);
            sb.append(getUpdateFieldForString(FIELD_STUD_STATUS, value, false));
        }
        // ARCHIVE_FLAG
        if (fields.contains(FIELD_ARCHIVE_FLAG)) {
            String value = (String) updateFieldsMap.get(FIELD_ARCHIVE_FLAG);
            sb.append(getUpdateFieldForString(FIELD_ARCHIVE_FLAG, value, false));
        }
        // HONOUR_FLAG
        if (fields.contains(FIELD_HONOUR_FLAG)) {
            String value = (String) updateFieldsMap.get(FIELD_HONOUR_FLAG);
            sb.append(getUpdateFieldForString(FIELD_HONOUR_FLAG, value, false));
        }
        // XCRIPT_ACTV_DATE
        if (fields.contains(FIELD_XCRIPT_ACTV_DATE)) {
            Long value = (Long) updateFieldsMap.get(FIELD_XCRIPT_ACTV_DATE);
            sb.append(getUpdateFieldForLong(FIELD_XCRIPT_ACTV_DATE, value, true));
        }

        sb.append(" WHERE STUD_NO=" + "'" + StringUtils.rightPad(pen, 10) + "'"); // a space is appended CAREFUL not to remove.

//        log.debug("Update Student_Master: " + sb.toString());
        return sb.toString();

    }

    protected Map<String, Object> buildUpdateFieldsMap(TraxStudentEntity traxStudentEntity, GradStatusEventPayloadDTO gradStatus) {
        // Needs to update required fields from GraduationStatus to TraxStudentEntity
        Map<String, Object> updateFieldsMap = new HashMap<>();

        // GRAD Requested Year
        if (StringUtils.isNotBlank(gradStatus.getProgram())) {
            String year = convertProgramToYear(gradStatus.getProgram());
            if (!StringUtils.equalsIgnoreCase(year, traxStudentEntity.getGradReqtYear())) {
                updateFieldsMap.put(FIELD_GRAD_REQT_YEAR, year);
            }
        }
        // GRAD Date
        if (StringUtils.isNotBlank(gradStatus.getProgramCompletionDate())) {
            String gradDateStr = gradStatus.getProgramCompletionDate().replace("/", "");
            if (NumberUtils.isDigits(gradDateStr) && NumberUtils.compare(Long.valueOf(gradDateStr), traxStudentEntity.getGradDate()) != 0) {
                updateFieldsMap.put(FIELD_GRAD_DATE, Long.valueOf(gradDateStr));
            }
        } else if (traxStudentEntity.getGradDate() != null && traxStudentEntity.getGradDate() != 0L) {
            updateFieldsMap.put(FIELD_GRAD_DATE, Long.valueOf("0"));
        }

        // Mincode
        if (!StringUtils.equalsIgnoreCase(gradStatus.getSchoolOfRecord(), traxStudentEntity.getMincode())) {
            updateFieldsMap.put(FIELD_MINCODE, gradStatus.getSchoolOfRecord());
        }

        // Mincode_Grad
        if (!StringUtils.equalsIgnoreCase(gradStatus.getSchoolAtGrad(), traxStudentEntity.getMincodeGrad())) {
            updateFieldsMap.put(FIELD_MINCODE_GRAD, gradStatus.getSchoolAtGrad());
        }

        // Student Grade
        if (!StringUtils.equalsIgnoreCase(gradStatus.getStudentGrade(), traxStudentEntity.getStudGrade())) {
            updateFieldsMap.put(FIELD_STUD_GRADE, gradStatus.getStudentGrade());
        }

        // Student Status
        checkAndPopulateTraxStudentStatus(traxStudentEntity, gradStatus.getStudentStatus(), updateFieldsMap);

        // Honour Flag
        if (!StringUtils.equalsIgnoreCase(gradStatus.getHonoursStanding(), traxStudentEntity.getHonourFlag())) {
            updateFieldsMap.put(FIELD_HONOUR_FLAG, gradStatus.getHonoursStanding());
            traxStudentEntity.setHonourFlag(gradStatus.getHonoursStanding());
        }


        if (!updateFieldsMap.isEmpty()) {
            // Current Date
            String currentDateStr = EducGradTraxApiUtils.formatDate(new Date(), EducGradTraxApiConstants.TRAX_DATE_FORMAT);
            if (NumberUtils.isDigits(currentDateStr)) {
                updateFieldsMap.put(FIELD_XCRIPT_ACTV_DATE, Long.valueOf(currentDateStr));
            }
            // Update User
        }

        return updateFieldsMap;
    }

    private void checkAndPopulateTraxStudentStatus(TraxStudentEntity traxStudentEntity, String gradStudentStatus, Map<String, Object> updateFieldsMap) {
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
            updateFieldsMap.put(FIELD_STUD_STATUS, newStudStatus);
        }

        if (!StringUtils.equalsIgnoreCase(archivedFlag, newArchiveFlag)) {
            updateFieldsMap.put(FIELD_ARCHIVE_FLAG, newArchiveFlag);
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
