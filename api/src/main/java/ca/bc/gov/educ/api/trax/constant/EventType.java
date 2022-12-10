package ca.bc.gov.educ.api.trax.constant;

/**
 * The enum Event type.
 */
public enum EventType {
  /* ===========================================================
    Incremental updates from Grad to Trax
   =============================================================*/
  GRAD_STUDENT_GRADUATED,
  GRAD_STUDENT_UNDO_COMPLETION,
  GRAD_STUDENT_UPDATED,
  STUDENT_CERTIFICATE_DISTRIBUTED,
  /* ===========================================================
    Incremental updates from Trax to Grad
   =============================================================*/
  /**
   * Update trax student master event type
   */
  TRAX_STUDENT_UPDATED

}
