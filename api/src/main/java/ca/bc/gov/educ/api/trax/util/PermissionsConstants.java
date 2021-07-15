package ca.bc.gov.educ.api.trax.util;

public interface PermissionsConstants {
	String _PREFIX = "#oauth2.hasAnyScope('";
	String _SUFFIX = "')";

	String READ_GRAD_COUNTRY = _PREFIX + "READ_GRAD_COUNTRY_CODE_DATA" + _SUFFIX;
	String READ_GRAD_PROVINCE = _PREFIX + "READ_GRAD_PROVINCE_CODE_DATA" + _SUFFIX;
	String READ_SCHOOL_DATA = _PREFIX + "READ_GRAD_SCHOOL_DATA" + _SUFFIX;
	String READ_PSI_INFO = _PREFIX + "READ_GRAD_PSI_DATA" + _SUFFIX;
}