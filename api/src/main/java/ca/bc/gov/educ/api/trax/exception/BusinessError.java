package ca.bc.gov.educ.api.trax.exception;

import lombok.Getter;

public enum BusinessError {
  EVENT_ALREADY_PERSISTED("EventEntity with event id :: $? , is already persisted in DB, a duplicate message from Jet Stream.");

  @Getter
  private final String code;

  BusinessError(final String code) {
    this.code = code;

  }
}
