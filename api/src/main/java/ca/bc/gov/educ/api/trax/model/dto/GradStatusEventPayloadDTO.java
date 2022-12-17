package ca.bc.gov.educ.api.trax.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GradStatusEventPayloadDTO {
  private String pen;
  private String program;
  private String programCompletionDate;
  private String schoolOfRecord;
  private String schoolAtGrad;
  private String studentGrade;
  private String studentStatus;
  private String honoursStanding;

  private String updateUser;
}
