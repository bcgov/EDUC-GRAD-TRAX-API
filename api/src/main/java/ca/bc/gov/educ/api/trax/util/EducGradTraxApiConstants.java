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

    public static final String INSTITUTE_STREAM_NAME="INSTITUTE_EVENTS";
    public static final String CORRELATION_ID = "correlationID";

    //API end-point Mapping constants
    public static final String API_VERSION_V1 = "/api/v1";
    public static final String API_VERSION_V2 = "/api/v2";

    // API Root Mapping
    public static final String GRAD_TRAX_API_ROOT_MAPPING_V1 = API_VERSION_V1+"/trax";
    public static final String GRAD_TRAX_API_ROOT_MAPPING_V2 = API_VERSION_V2+"/trax";

    // Controller Mappings
    public static final String GET_COMMON_SCHOOLS = "/common";
    public static final String GRAD_TRAX_CODE_URL_MAPPING_V1 =  GRAD_TRAX_API_ROOT_MAPPING_V1 + "/code";
    public static final String GRAD_TRAX_CODE_URL_MAPPING_V2 =  GRAD_TRAX_API_ROOT_MAPPING_V2 + "/code";
    public static final String GRAD_SCHOOL_URL_MAPPING_V1 = GRAD_TRAX_API_ROOT_MAPPING_V1 + "/school";
    public static final String GRAD_SCHOOL_URL_MAPPING_V2 = GRAD_TRAX_API_ROOT_MAPPING_V2 + "/school";
    public static final String GRAD_SCHOOL_DETAIL_URL_MAPPING_V2 = GRAD_TRAX_API_ROOT_MAPPING_V2 + "/school-detail";
    public static final String GRAD_DISTRICT_URL_MAPPING_V1 = GRAD_TRAX_API_ROOT_MAPPING_V1 + "/district";
    public static final String GRAD_DISTRICT_URL_MAPPING_V2 = GRAD_TRAX_API_ROOT_MAPPING_V2 + "/district";
    public static final String GRAD_PSI_URL_MAPPING_V1 = GRAD_TRAX_API_ROOT_MAPPING_V1 + "/psi";
    public static final String GRAD_PSI_URL_MAPPING_V2 = GRAD_TRAX_API_ROOT_MAPPING_V2 + "/psi";
    public static final String GRAD_TRAX_COMMON_URL_MAPPING_V1 = GRAD_TRAX_API_ROOT_MAPPING_V1 + GET_COMMON_SCHOOLS;
    public static final String GRAD_TRAX_COMMON_URL_MAPPING_V2 = GRAD_TRAX_API_ROOT_MAPPING_V2 + GET_COMMON_SCHOOLS;
    public static final String GRAD_TSW_URL_MAPPING_V1 = GRAD_TRAX_API_ROOT_MAPPING_V1 + "/tsw";
    public static final String GRAD_TSW_URL_MAPPING_V2 = GRAD_TRAX_API_ROOT_MAPPING_V2 + "/tsw";
    public static final String GRAD_EDW_URL_MAPPING_V1 = GRAD_TRAX_API_ROOT_MAPPING_V1 + "/edw";
    public static final String GRAD_EDW_URL_MAPPING_V2 = GRAD_TRAX_API_ROOT_MAPPING_V2 + "/edw";


    // Service Methods Mappings
    public static final String GET_ALL_COUNTRY_MAPPING = "/country";
    public static final String GET_ALL_COUNTRY_BY_CODE_MAPPING = "/country/{countryCode}";

    public static final String GET_ALL_PROVINCE_MAPPING = "/province";
    public static final String GET_ALL_PROVINCE_BY_CODE_MAPPING = "/province/{provinceCode}";

    public static final String CHECK_SCHOOL_BY_CODE_MAPPING = "/check/{minCode}";
    public static final String GET_SCHOOL_BY_CODE_MAPPING = "/{minCode}";

    public static final String GET_COMMON_SCHOOL_BY_CODE_MAPPING = GET_COMMON_SCHOOLS + GET_SCHOOL_BY_CODE_MAPPING;
    public static final String GET_SCHOOL_SEARCH_MAPPING = "/search";

    public static final String GET_PSI_BY_CODE_MAPPING = "/{psiCode}";
    public static final String GET_PSI_SEARCH_MAPPING="/search";

    public static final String GET_STUDENT_PSI_BY_CODE_MAPPING = "/student";

    public static final String GET_DISTRICT_BY_DISTNO_MAPPING = "/{distNo}";
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
    public static final String DELETE_TRAX_STUDENT_NO_MAPPING = "/trax-student-no/{pen}";

    // Event urls
    public static final String EVENT_HISTORY_MAPPING_V1 = GRAD_TRAX_API_ROOT_MAPPING_V1 + "/event/history";

    //Default Attribute value constants
    public static final String DEFAULT_CREATED_BY = "API_GRAD_TRAX";
    protected static final Date DEFAULT_CREATED_TIMESTAMP = new Date();
    public static final String DEFAULT_UPDATED_BY = "API_GRAD_TRAX";
    protected static final Date DEFAULT_UPDATED_TIMESTAMP = new Date();

    //Default Date format constants
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

    public static final String TRAX_DATE_FORMAT = "yyyyMMdd";
    public static final String TRAX_TSW_DATE_FORMAT = "yyyyMM";
    protected static final Date ADULT_18_RULE_VALID_DATE = EducGradTraxApiUtils.parseDate("2012-07-01");

    @Value("${authorization.user}")
    private String userName;

    @Value("${authorization.password}")
    private String password;

    @Value("${endpoint.keycloak.getToken}")
    private String tokenUrl;

    @Value("${authorization.institute-api.client-id}")
    private String instituteClientId;

    @Value("${authorization.institute-api.client-secret}")
    private String instituteClientSecret;

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

    @Value("${redis.url}")
    private String redisUrl;

    @Value("${redis.port}")
    private String redisPort;

    @Value("${redis.user}")
    private String redisUser;

    @Value("${redis.secret}")
    private String redisSecret;

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

    @Value("${endpoint.institute-api.get-all-schools.url}")
    private String allSchoolsFromInstituteApiUrl;

    @Value("${endpoint.institute-api.get-school-details-by-id.url}")
    private String schoolDetailsByIdFromInstituteApiUrl;

    @Value("${endpoint.institute-api.get-all-districts.url}")
    private String allDistrictsFromInstituteApiUrl;
    @Value("${endpoint.institute-api.get-district.url}")
    private String getDistrictFromInstituteApiUrl;

    @Value("${endpoint.institute-api.get-all-school-category-codes.url}")
    private String allSchoolCategoryCodesFromInstituteApiUrl;

    @Value("${endpoint.institute-api.get-all-school-funding-group-codes.url}")
    private String allSchoolFundingGroupCodesFromInstituteApiUrl;

    @Value("${endpoint.student-admin.school-details.url}")
    private String studentAdminSchoolDetailsUrl;

    @Value("${endpoint.student-admin.district-details.url}")
    private String studentAdminDistrictDetailsUrl;

    @Value("${endpoint.student-admin.authority-details.url}")
    private String studentAdminAuthorityDetailsUrl;

    @Value("${endpoint.institute-api.get-schools-paginated.url}")
    private String schoolsPaginated;

    @Value("${endpoint.institute-api.get-districts-paginated.url}")
    private String districtsPaginated;

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

    @Value("${props.school-cache-expiry-in-mins}")
    private long schoolCacheExpiryInMins;

}
