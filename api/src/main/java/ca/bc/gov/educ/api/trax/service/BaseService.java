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

    protected void populateTraxStudent(TraxStudentEntity traxStudentEntity, GraduationStatus gradStatus) {
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
        traxStudentEntity.setMincodeGrad(gradStatus.getSchoolAtGrad());
        traxStudentEntity.setStudGrade(gradStatus.getStudentGrade());
        traxStudentEntity.setHonourFlag(gradStatus.getHonoursStanding());
        String currentDateStr = EducGradTraxApiUtils.formatDate(new Date(), EducGradTraxApiConstants.TRAX_DATE_FORMAT);
        if (NumberUtils.isDigits(currentDateStr)) {
            traxStudentEntity.setXcriptActvDate(Long.valueOf(currentDateStr));
        }
        populateTraxStudentStatus(traxStudentEntity, gradStatus.getStudentStatus());
    }

    private void populateTraxStudentStatus(TraxStudentEntity traxStudentEntity, String gradStudentStatus) {
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
    }
}
