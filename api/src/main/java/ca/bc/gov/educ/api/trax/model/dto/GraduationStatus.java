package ca.bc.gov.educ.api.trax.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
@Component
public class GraduationStatus extends BaseModel{

    private String studentGradData;
    private String pen;
    private String program;
    private String programName;
    private String programCompletionDate;
    private String gpa;
    private String honoursStanding;
    private String recalculateGradStatus;   
    private String schoolOfRecord;
    private String schoolName;
    private String studentGrade;	
    private String studentStatus;
    private String studentStatusName;
    private UUID studentID;
    private String schoolAtGrad;
    private String schoolAtGradName;
    private String recalculateProjectedGrad;
    private Long batchId;
    private String consumerEducationRequirementMet;
    private String studentProjectedGradData;
}
