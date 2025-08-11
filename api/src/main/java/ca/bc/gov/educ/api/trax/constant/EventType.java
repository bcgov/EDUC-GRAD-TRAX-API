package ca.bc.gov.educ.api.trax.constant;

/**
 * The enum EventEntity type.
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
  XPROGRAM,
  ASSESSMENT,
  COURSE,
  FI10ADD,

  /**
   * INSTITUTE API EVENT TYPES
   */
  UPDATE_SCHOOL,

  CREATE_SCHOOL,

  UPDATE_DISTRICT,

  CREATE_DISTRICT,

  UPDATE_AUTHORITY,

  CREATE_AUTHORITY,

  GET_AUTHORITY,

  GET_PAGINATED_SCHOOLS,

  GET_PAGINATED_AUTHORITIES,

  MOVE_SCHOOL,

  CREATE_SCHOOL_CONTACT,

  UPDATE_SCHOOL_CONTACT,

  DELETE_SCHOOL_CONTACT,

  CREATE_DISTRICT_CONTACT,

  UPDATE_DISTRICT_CONTACT,

  DELETE_DISTRICT_CONTACT,

  CREATE_AUTHORITY_CONTACT,

  UPDATE_AUTHORITY_CONTACT,

  DELETE_AUTHORITY_CONTACT,

  UPDATE_GRAD_SCHOOL,

  ASSESSMENT_STUDENT_UPDATE,

  CREATE_GRAD_SCHOOL
}
