package ca.bc.gov.educ.api.trax.constant;

/**
 * The enum EventEntity outcome.
 */
public enum EventOutcome {
  /**
   * Student updated event outcome.
   */
  GRAD_STATUS_UPDATED,
  /**
   * Student updated event outcome.
   */
  TRAX_STUDENT_MASTER_UPDATED,

  /**
   * Institute API even outcomes
   */
  SCHOOL_UPDATED,

  SCHOOL_CREATED,

  DISTRICT_UPDATED,

  DISTRICT_CREATED,

  AUTHORITY_UPDATED,

  AUTHORITY_CREATED,

  AUTHORITY_FOUND,

  AUTHORITY_NOT_FOUND,

  SCHOOL_NOT_FOUND,

  SCHOOL_MOVED,

  SCHOOL_CONTACT_CREATED,

  SCHOOL_CONTACT_UPDATED,

  SCHOOL_CONTACT_DELETED,

  DISTRICT_CONTACT_CREATED,

  DISTRICT_CONTACT_UPDATED,

  DISTRICT_CONTACT_DELETED,

  AUTHORITY_CONTACT_CREATED,

  AUTHORITY_CONTACT_UPDATED,

  AUTHORITY_CONTACT_DELETED,

  GRAD_SCHOOL_UPDATED,

  ASSESSMENT_STUDENT_UPDATED,

  SCHOOL_OF_RECORD_UPDATED,
  
  GRAD_STUDENT_CITIZENSHIP_UPDATED,
  
  GRAD_STUDENT_ADOPTED,
  
  STUDENT_COURSES_UPDATED;

  public static boolean isValid(String value) {
    if (value == null) {
      return false;
    }
    try {
      EventOutcome.valueOf(value);
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }
}
