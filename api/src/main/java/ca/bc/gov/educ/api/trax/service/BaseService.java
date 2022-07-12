package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.model.dto.GraduationStatus;
import ca.bc.gov.educ.api.trax.model.entity.TraxStudentEntity;
import ca.bc.gov.educ.api.trax.util.EducGradTraxApiConstants;
import ca.bc.gov.educ.api.trax.util.EducGradTraxApiUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Date;

public abstract class BaseService implements EventService {

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

    protected boolean validateAndSetTraxStudentUpdate(TraxStudentEntity traxStudentEntity, GraduationStatus gradStatus) {
        boolean isUpdated = false;
        // Needs to update required fields from GraduationStatus to TraxStudentEntity
        // GRAD Requested Year
        if (StringUtils.isNotBlank(gradStatus.getProgram())) {
            String year = convertProgramToYear(gradStatus.getProgram());
            if (!StringUtils.equalsIgnoreCase(year, traxStudentEntity.getGradReqtYear())) {
                isUpdated = true;
                traxStudentEntity.setGradReqtYear(year);
            }
        }
        // GRAD Date
        if (StringUtils.isNotBlank(gradStatus.getProgramCompletionDate())) {
            String gradDateStr = gradStatus.getProgramCompletionDate().replace("/", "");
            if (NumberUtils.isDigits(gradDateStr) && NumberUtils.compare(Long.valueOf(gradDateStr), traxStudentEntity.getGradDate()) != 0) {
                isUpdated = true;
                traxStudentEntity.setGradDate(Long.valueOf(gradDateStr));
            }
        } else if (traxStudentEntity.getGradDate() != null && traxStudentEntity.getGradDate() != 0L) {
            isUpdated = true;
            traxStudentEntity.setGradDate(0L);
        }

        // Mincode
        if (!StringUtils.equalsIgnoreCase(gradStatus.getSchoolOfRecord(), traxStudentEntity.getMincode())) {
            isUpdated = true;
            traxStudentEntity.setMincode(gradStatus.getSchoolOfRecord());
        }

        // Mincode_Grad
        if (!StringUtils.equalsIgnoreCase(gradStatus.getSchoolAtGrad(), traxStudentEntity.getMincodeGrad())) {
            isUpdated = true;
            traxStudentEntity.setMincodeGrad(gradStatus.getSchoolAtGrad());
        }

        // Student Grade
        if (!StringUtils.equalsIgnoreCase(gradStatus.getStudentGrade(), traxStudentEntity.getStudGrade())) {
            isUpdated = true;
            traxStudentEntity.setStudGrade(gradStatus.getStudentGrade());
        }

        // Student Status
        if (validateAndSetTraxStudentStatus(traxStudentEntity, gradStatus.getStudentStatus())) {
            isUpdated = true;
        }

        // Honour Flag
        if (!StringUtils.equalsIgnoreCase(gradStatus.getHonoursStanding(), traxStudentEntity.getHonourFlag())) {
            isUpdated = true;
            traxStudentEntity.setHonourFlag(gradStatus.getHonoursStanding());
        }

        // Current Date
        String currentDateStr = EducGradTraxApiUtils.formatDate(new Date(), EducGradTraxApiConstants.TRAX_DATE_FORMAT);
        if (NumberUtils.isDigits(currentDateStr)) {
            traxStudentEntity.setXcriptActvDate(Long.valueOf(currentDateStr));
        }

        return isUpdated;
    }

    private boolean validateAndSetTraxStudentStatus(TraxStudentEntity traxStudentEntity, String gradStudentStatus) {
        String studStatus = traxStudentEntity.getStudStatus();
        String archivedFlag = traxStudentEntity.getArchiveFlag();

        if (StringUtils.equals(gradStudentStatus, "CUR")) {
            traxStudentEntity.setStudStatus("A");
            traxStudentEntity.setArchiveFlag("A");
        } else if (StringUtils.equals(gradStudentStatus, "ARC")) {
            traxStudentEntity.setStudStatus("A");
            traxStudentEntity.setArchiveFlag("I");
        } else if (StringUtils.equals(gradStudentStatus, "DEC")) {
            traxStudentEntity.setStudStatus("D");
            traxStudentEntity.setArchiveFlag("I");
        } else if (StringUtils.equals(gradStudentStatus, "MER")) {
            traxStudentEntity.setStudStatus("M");
            traxStudentEntity.setArchiveFlag("I");
        } else if (StringUtils.equals(gradStudentStatus, "TER")) {
            traxStudentEntity.setStudStatus("T");
            traxStudentEntity.setArchiveFlag("I");
        }

        if (!StringUtils.equalsIgnoreCase(studStatus, traxStudentEntity.getStudStatus())) {
            return true;
        }

        if (!StringUtils.equalsIgnoreCase(archivedFlag, traxStudentEntity.getArchiveFlag())) {
            return true;
        }

        return false; // status is not updated at all
    }
}
