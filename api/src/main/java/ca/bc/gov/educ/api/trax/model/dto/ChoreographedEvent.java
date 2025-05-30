package ca.bc.gov.educ.api.trax.model.dto;

import ca.bc.gov.educ.api.trax.constant.EventOutcome;
import ca.bc.gov.educ.api.trax.constant.EventType;
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
public class ChoreographedEvent {
  UUID eventID;
  /**
   * The EventEntity type.
   */
  EventType eventType;
  /**
   * The EventEntity outcome.
   */
  EventOutcome eventOutcome;
  /**
   * The Activity code.
   */
  String activityCode;
  /**
   * The EventEntity payload.
   */
  String eventPayload; // json string
  /**
   * The Create user.
   */
  String createUser;
  /**
   * The Update user.
   */
  String updateUser;
}
