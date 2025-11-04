package ca.bc.gov.educ.api.trax.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@EqualsAndHashCode(callSuper = true)
@Data
public class IgnoreEventException extends Exception {

  private final String eventType;
  private final String eventOutcome;

  @Serial
  private static final long serialVersionUID = 5241655513745148898L;

  public IgnoreEventException(final String message, String eventType, String eventOutcome) {
    super(message);
    this.eventType = eventType;
    this.eventOutcome = eventOutcome;
  }
}
