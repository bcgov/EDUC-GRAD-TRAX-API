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
  /* ===========================================================
    Incremental updates from Trax to Grad
   =============================================================*/
  /**
   * Trax update type
   */
  NEWSTUDENT,
  UPD_DEMOG,
  UPD_GRAD,
  UPD_STD_STATUS,
  XPROGRAM,
  ASSESSMENT,
  COURSE,
  FI10ADD
}
