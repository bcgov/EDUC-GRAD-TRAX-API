package ca.bc.gov.educ.api.trax.constant;

public interface PermissionsContants {
	String _PREFIX = "#oauth2.hasAnyScope('";
	String _SUFFIX = "')";

	String READ_GRAD_PROGRAM = _PREFIX + "READ_GRAD_PROGRAM_CODE_DATA" + _SUFFIX;
	
}