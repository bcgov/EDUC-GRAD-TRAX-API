package ca.bc.gov.educ.api.trax.util;

import org.apache.commons.lang3.StringUtils;

public class ReplicationUtils {

    public static String getBlankWhenNull(final String s){
        if(StringUtils.isNotEmpty(s)){
            return s;
        }
        // Return a blank
        return " ";
    }

    public static String getEmptyWhenNull(final String s){
        if(StringUtils.isNotEmpty(s)){
            return s;
        }
        // Return an empty value
        return "";
    }

    public static Long getZeroWhenNull(final Long s){
        if(s != null){
            return s;
        }
        // Return zero
        return 0L;
    }
}
