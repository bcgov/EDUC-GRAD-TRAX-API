package ca.bc.gov.educ.api.trax.constant;

/**
 * The enum Event type.
 */
public enum EventType {
  /* ===========================================================
    Incremental updates from Grad to Trax
   =============================================================*/
  /**
   * Create Grad Status event type
   */
  CREATE_GRAD_STATUS,
  /**
   * Update Grad Status event type
   */
  UPDATE_GRAD_STATUS,
  /* ===========================================================
    Incremental updates from Trax to Grad
   =============================================================*/
  /**
   * Update trax student master event type
   */
  UPDATE_TRAX_STUDENT_MASTER,
}
