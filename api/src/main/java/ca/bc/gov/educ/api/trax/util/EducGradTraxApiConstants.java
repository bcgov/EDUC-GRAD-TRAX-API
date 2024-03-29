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
    public static final String GRAD_STREAM_NAME ="GRAD_STATUS_EVENT_STREAM";
    public static final String TRAX_STREAM_NAME="TRAX_STATUS_EVENT_STREAM";
    public static final String CORRELATION_ID = "correlationID";

    //API end-point Mapping constants
    public static final String API_ROOT_MAPPING = "";
    public static final String API_VERSION = "v1";

    // API Root Mapping
    public static final String GRAD_TRAX_API_ROOT_MAPPING = "/api/" + API_VERSION+"/trax";

    // Controller Mappings
    public static final String GRAD_TRAX_CODE_URL_MAPPING = GRAD_TRAX_API_ROOT_MAPPING + "/code";
    public static final String GRAD_SCHOOL_URL_MAPPING = GRAD_TRAX_API_ROOT_MAPPING + "/school";
    public static final String GRAD_DISTRICT_URL_MAPPING = GRAD_TRAX_API_ROOT_MAPPING + "/district";
    public static final String GRAD_PSI_URL_MAPPING = GRAD_TRAX_API_ROOT_MAPPING + "/psi";
    public static final String GRAD_TRAX_COMMON_URL_MAPPING = GRAD_TRAX_API_ROOT_MAPPING + "/common";
    public static final String GRAD_TSW_URL_MAPPING = GRAD_TRAX_API_ROOT_MAPPING + "/tsw";
    public static final String GRAD_EDW_URL_MAPPING = GRAD_TRAX_API_ROOT_MAPPING + "/edw";


    // Service Methods Mappings
    public static final String GET_ALL_COUNTRY_MAPPING = "/country";
    public static final String GET_ALL_COUNTRY_BY_CODE_MAPPING = "/country/{countryCode}";

    public static final String GET_ALL_PROVINCE_MAPPING = "/province";
    public static final String GET_ALL_PROVINCE_BY_CODE_MAPPING = "/province/{provinceCode}";

    public static final String CHECK_SCHOOL_BY_CODE_MAPPING = "/check/{minCode}";
    public static final String GET_SCHOOL_BY_CODE_MAPPING = "/{minCode}";
    public static final String GET_SCHOOL_SEARCH_MAPPING = "/search";

    public static final String GET_PSI_BY_CODE_MAPPING = "/{psiCode}";
    public static final String GET_PSI_SEARCH_MAPPING="/search";

    public static final String GET_STUDENT_PSI_BY_CODE_MAPPING = "/student";

    public static final String GET_DISTRICT_BY_DISTNO_MAPPING = "/{distCode}";
    public static final String GET_DISTRICTS_BY_SCHOOL_CATEGORY_MAPPING = "/schoolCategories";
    public static final String GET_SCHOOLS_BY_SCHOOL_CATEGORY_MAPPING = "/schoolCategories";

    public static final String GET_TRANSCRIPT_DEMOG_BY_PEN_MAPPING = "/tran-demog/{pen}";
    public static final String GET_TRANSCRIPT_COURSE_BY_PEN_MAPPING = "/tran-courses/{pen}";

    public static final String GET_SCHOOLS_BY_GRAD_YEAR_MAPPING = "/get-schools/snapshot/{gradYear}";
    public static final String GET_STUDENTS_BY_GRAD_YEAR_MAPPING = "/get-students/snapshot/{gradYear}";
    public static final String GET_STUDENTS_BY_GRAD_YEAR_AND_SCHOOL_MAPPING = "/get-students/snapshot/{gradYear}/{minCode}";

    public static final String GET_TRAX_STUDENT_MASTER_MAPPING = "/student-master/{pen}";
    public static final String GET_TRAX_STUDENT_DEMOG_MAPPING = "/student-demog/{pen}";
    public static final String GET_TRAX_STUDENT_NO_LIST_BY_PAGING_MAPPING = "/student-no-list/paginated";
    public static final String GET_TOTAL_NUMBER_OF_TRAX_STUDENT_NO_LIST_MAPPING = "/student-no-list/total-count";
    public static final String GET_COURSE_RESTRICTION_LIST_MAPPING = "/course-restrictions";
    public static final String GET_COURSE_REQUIREMENT_LIST_MAPPING = "/course-requirements";
    public static final String POST_SAVE_TRAX_STUDENT_NO_MAPPING = "/trax-student-no";

    //Default Attribute value constants
    public static final String DEFAULT_CREATED_BY = "API_GRAD_TRAX";
    protected static final Date DEFAULT_CREATED_TIMESTAMP = new Date();
    public static final String DEFAULT_UPDATED_BY = "API_GRAD_TRAX";
    protected static final Date DEFAULT_UPDATED_TIMESTAMP = new Date();

    //Default Date format constants
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

    public static final String TRAX_DATE_FORMAT = "yyyyMMdd";
    public static final String TRAX_TSW_DATE_FORMAT = "yyyyMM";
    public static final Date ADULT_18_RULE_VALID_DATE = EducGradTraxApiUtils.parseDate("2012-07-01");

    @Value("${authorization.user}")
    private String userName;

    @Value("${authorization.password}")
    private String password;

    @Value("${endpoint.keycloak.getToken}")
    private String tokenUrl;

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

    // Splunk LogHelper Enabled
    @Value("${splunk.log-helper.enabled}")
    private boolean splunkLogHelperEnabled;

    @Value("${endpoint.educ-school-api.get-school-by-mincode.url}")
    private String schoolByMincodeSchoolApiUrl;

    @Value("${endpoint.educ-school-api.get-all-schools.url}")
    private String allSchoolSchoolApiUrl;

    // Scheduler: ongoing updates from TRAX to GRAD
    @Value("${cron.scheduled.process.events.trax-to-grad.run}")
    private String traxToGradCronRun;

    @Value("${cron.scheduled.process.events.trax-to-grad.lockAtLeastFor}")
    private String traxToGradLockAtLeastFor;

    @Value("${cron.scheduled.process.events.trax-to-grad.lockAtMostFor}")
    private String traxToGradLockAtMostFor;

    @Value("${cron.scheduled.process.events.trax-to-grad.threshold}")
    private int traxToGradProcessingThreshold;

    // Scheduler: ongoing updates from GRAD to TRAX
    @Value("${cron.scheduled.process.events.grad-to-trax.run}")
    private String gradToTraxCronRun;

    @Value("${cron.scheduled.process.events.grad-to-trax.lockAtLeastFor}")
    private String gradToTraxLockAtLeastFor;

    @Value("${cron.scheduled.process.events.grad-to-trax.lockAtMostFor}")
    private String gradToTraxLockAtMostFor;

    @Value("${cron.scheduled.process.events.grad-to-trax.threshold}")
    private int gradToTraxProcessingThreshold;

    // Scheduler: TRAX triggers
    @Value("${cron.scheduled.process.trigger-jobs.read-trax-update.run}")
    private String traxTriggersCronRun;

    @Value("${cron.scheduled.process.trigger-jobs.read-trax-update.lockAtLeastFor}")
    private String traxTriggersLockAtLeastFor;

    @Value("${cron.scheduled.process.trigger-jobs.read-trax-update.lockAtMostFor}")
    private String traxTriggersLockAtMostFor;

    @Value("${cron.scheduled.process.trigger-jobs.read-trax-update.threshold}")
    private int traxTriggersProcessingThreshold;

    @Value("${cron.scheduled.process.purge-old-records.staleInDays}")
    private int recordsStaleInDays;

}
