package ca.bc.gov.educ.api.trax.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TraxGraduationUpdateDTO extends TraxStudentUpdateDTO {

    // GRAD_REQT_YEAR
    private String graduationRequirementYear;
    // STUD_GRAD
    private String studentGrade;
    // SchoolId
    private UUID schoolOfRecordId;
    // SLP_DATE
    private String slpDate;
    // STUD_CITIZ
    private String citizenship;
    // STUD_STATUS
    private String studentStatus;
    // ARCHIVE_FLAG
    private String archiveFlag;

}
