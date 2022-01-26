package ca.bc.gov.educ.api.trax.util;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Getter
@Setter
public class EducGradTraxApiConstants {

    /**
     * The constant GRAD-TRAX-API.
     */
    public static final String API_NAME = "GRAD-TRAX-API";
    public static final String STREAM_NAME="GRAD_STATUS_EVENTS";
    public static final String CORRELATION_ID = "correlationID";

    //API end-point Mapping constants
    public static final String API_ROOT_MAPPING = "";
    public static final String API_VERSION = "v1";

    // API Root Mapping
    public static final String GRAD_TRAX_API_ROOT_MAPPING = "/api/" + API_VERSION;

    // Controller Mappings
    public static final String GRAD_TRAX_CODE_URL_MAPPING = GRAD_TRAX_API_ROOT_MAPPING + "/code";
    public static final String GRAD_SCHOOL_URL_MAPPING = GRAD_TRAX_API_ROOT_MAPPING + "/school";
    public static final String GRAD_DISTRICT_URL_MAPPING = GRAD_TRAX_API_ROOT_MAPPING + "/district";
    public static final String GRAD_PSI_URL_MAPPING = GRAD_TRAX_API_ROOT_MAPPING + "/psi";


    // Service Methods Mappings
    public static final String GET_ALL_COUNTRY_MAPPING = "/country";
    public static final String GET_ALL_COUNTRY_BY_CODE_MAPPING = "/country/{countryCode}";
    
    public static final String GET_ALL_PROVINCE_MAPPING = "/province";
    public static final String GET_ALL_PROVINCE_BY_CODE_MAPPING = "/province/{provinceCode}";

    public static final String GET_SCHOOL_BY_CODE_MAPPING = "/{minCode}";
    public static final String GET_SCHOOL_SEARCH_MAPPING = "/search";

    public static final String GET_PSI_BY_CODE_MAPPING = "/{psiCode}";
    public static final String GET_PSI_SEARCH_MAPPING="/search";

    public static final String GET_DISTRICT_BY_DISTNO_MAPPING = "/{distCode}";
    
    
    //Default Attribute value constants
    public static final String DEFAULT_CREATED_BY = "API_GRAD_TRAX";
    public static final Date DEFAULT_CREATED_TIMESTAMP = new Date();
    public static final String DEFAULT_UPDATED_BY = "API_GRAD_TRAX";
    public static final Date DEFAULT_UPDATED_TIMESTAMP = new Date();

    //Default Date format constants
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    
    public static final String TRAX_DATE_FORMAT = "yyyyMMdd";

    //NAT
    /**
     * The Server.
     */
    @Value("${nats.url}")
    private String natsUrl;
    /**
     * The Max reconnect.
     */
    @Value("${nats.maxReconnect}")
    private int maxReconnect;
    /**
     * The Connection name.
     */
    @Value("${nats.connectionName}")
    private String connectionName;

    // Incremental Trax Update
    @Value("${trax.update.enabled}")
    private boolean traxUpdateEnabled;
}
