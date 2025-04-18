package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.constant.EventOutcome;
import ca.bc.gov.educ.api.trax.constant.EventType;
import ca.bc.gov.educ.api.trax.messaging.NatsConnection;
import ca.bc.gov.educ.api.trax.messaging.jetstream.Publisher;
import ca.bc.gov.educ.api.trax.messaging.jetstream.Subscriber;
import ca.bc.gov.educ.api.trax.model.dto.*;
import ca.bc.gov.educ.api.trax.model.entity.TraxUpdateInGradEntity;
import ca.bc.gov.educ.api.trax.model.entity.TraxUpdatedPubEvent;
import ca.bc.gov.educ.api.trax.model.transformer.TraxUpdateInGradTransformer;
import ca.bc.gov.educ.api.trax.repository.TraxUpdateInGradRepository;
import ca.bc.gov.educ.api.trax.repository.TraxUpdatedPubEventRepository;
import ca.bc.gov.educ.api.trax.service.institute.CommonService;
import ca.bc.gov.educ.api.trax.util.JsonUtil;
import ca.bc.gov.educ.api.trax.util.RestUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import net.javacrumbs.shedlock.core.LockAssert;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import redis.clients.jedis.JedisCluster;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static ca.bc.gov.educ.api.trax.constant.EventStatus.DB_COMMITTED;
import static ca.bc.gov.educ.api.trax.util.EducGradTraxApiConstants.DEFAULT_CREATED_BY;
import static ca.bc.gov.educ.api.trax.util.EducGradTraxApiConstants.DEFAULT_UPDATED_BY;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class TraxUpdateServiceTest {

    @Autowired
    TraxUpdateService traxUpdateService;

    @MockBean
    TraxCommonService traxCommonService;

    @MockBean
    CommonService commonService;

    @Autowired
    TraxUpdateInGradTransformer traxUpdateInGradTransformer;

    @MockBean
    TraxUpdateInGradRepository traxUpdateInGradRepository;

    @MockBean
    TraxUpdatedPubEventRepository traxUpdatedPubEventRepository;

    @MockBean
    RestUtils restUtils;

    // NATS
    @MockBean
    private NatsConnection natsConnection;

    @MockBean
    private Publisher publisher;

    @MockBean
    private Subscriber subscriber;
    @MockBean
    private JedisConnectionFactory jedisConnectionFactoryMock;
    @MockBean
    private JedisCluster jedisClusterMock;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public ClientRegistrationRepository clientRegistrationRepository() {
            return new ClientRegistrationRepository() {
                @Override
                public ClientRegistration findByRegistrationId(String registrationId) {
                    return null;
                }
            };
        }
    }

    @Before
    public void setUp() {
        openMocks(this);
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testGetOutstandingList() {
        TraxUpdateInGradEntity traxUpdateInGradEntity = new TraxUpdateInGradEntity();
        traxUpdateInGradEntity.setId(BigDecimal.valueOf(1000));
        traxUpdateInGradEntity.setPen("123456789");
        traxUpdateInGradEntity.setUpdateType("UPD_GRAD");
        traxUpdateInGradEntity.setStatus("OUTSTANDING");
        traxUpdateInGradEntity.setUpdateDate(DateUtils.addDays(new Date(), -1));

        when(traxUpdateInGradRepository.findOutstandingUpdates(any())).thenReturn(Arrays.asList(traxUpdateInGradEntity));

        List<TraxUpdateInGradEntity> results = traxUpdateService.getOutstandingList();

        assertThat(results).isNotNull();
        assertThat(results).hasSize(1);
        TraxUpdateInGradEntity responseEntity = results.get(0);
        assertThat(traxUpdateInGradEntity.getPen()).isEqualTo(responseEntity.getPen());
        assertThat(traxUpdateInGradEntity.getStatus()).isEqualTo(responseEntity.getStatus());
    }

    @Test
    public void testScheduledRunForTraxUpdates() throws JsonProcessingException {
        String pen = "123456789";
        String mincode = "12345678";
        UUID schoolId = UUID.randomUUID();
        LockAssert.TestHelper.makeAllAssertsPass(true);

        TraxUpdateInGradEntity traxUpdateInGradEntity = new TraxUpdateInGradEntity();
        traxUpdateInGradEntity.setPen(pen);
        traxUpdateInGradEntity.setUpdateType("UPD_GRAD");
        traxUpdateInGradEntity.setStatus("OUTSTANDING");
        traxUpdateInGradEntity.setUpdateDate(DateUtils.addDays(new Date(), -1));

        TraxGraduationUpdateDTO payload = new TraxGraduationUpdateDTO();
        payload.setPen(pen);
        payload.setStudentGrade("12");
        payload.setSchoolOfRecordId(schoolId);
        payload.setCitizenship("C");
        payload.setGraduationRequirementYear("2018");
        String jsonString = JsonUtil.getJsonStringFromObject(payload);

        ConvGradStudent traxStudent = new ConvGradStudent();
        traxStudent.setPen(pen);
        traxStudent.setStudentGrade("12");
        traxStudent.setGraduationRequirementYear("2018");
        traxStudent.setStudentStatus("A");
        traxStudent.setArchiveFlag("A");
        traxStudent.setStudentCitizenship("C");
        traxStudent.setSchoolOfRecordId(schoolId);

        TraxUpdatedPubEvent traxUpdatedPubEvent = TraxUpdatedPubEvent.builder()
                .eventType(EventType.UPD_GRAD.toString())
                .eventId(UUID.randomUUID())
                .eventOutcome(EventOutcome.TRAX_STUDENT_MASTER_UPDATED.toString())
                .activityCode(traxUpdateInGradEntity.getUpdateType())
                .eventPayload(jsonString)
                .eventStatus(DB_COMMITTED.toString())
                .createUser(DEFAULT_CREATED_BY)
                .updateUser(DEFAULT_UPDATED_BY)
                .createDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();

        ResponseObj tokenObj = new ResponseObj();
        tokenObj.setAccess_token("123");
        when(restUtils.getTokenResponseObject()).thenReturn(tokenObj);
        when(traxUpdateInGradRepository.findOutstandingUpdates(any())).thenReturn(Arrays.asList(traxUpdateInGradEntity));
        when(traxUpdatedPubEventRepository.save(traxUpdatedPubEvent)).thenReturn(traxUpdatedPubEvent);
        when(traxCommonService.getStudentMasterDataFromTrax(pen)).thenReturn(Arrays.asList(traxStudent));
        when(commonService.getSchoolIdFromRedisCache(mincode)).thenReturn(schoolId );

        traxUpdateService.publishTraxUpdatedEvent(traxUpdateInGradEntity);
        traxUpdateService.updateStatus(traxUpdateInGradEntity);

        assertThatNoException();
    }

    @Test
    public void testProcess_whenNewStudent_isAdded() throws JsonProcessingException {
        String pen = "123456789";

        TraxUpdateInGradEntity traxUpdateInGradEntity = new TraxUpdateInGradEntity();
        traxUpdateInGradEntity.setPen(pen);
        traxUpdateInGradEntity.setUpdateType("NEWSTUDENT");
        traxUpdateInGradEntity.setStatus("OUTSTANDING");
        traxUpdateInGradEntity.setUpdateDate(DateUtils.addDays(new Date(), -1));

        TraxDemographicsUpdateDTO payload = new TraxDemographicsUpdateDTO();
        payload.setPen(pen);
        payload.setLastName("Test");
        payload.setFirstName("QA");
        String jsonString = JsonUtil.getJsonStringFromObject(payload);

        ConvGradStudent traxStudent = new ConvGradStudent();
        traxStudent.setPen(pen);
        traxStudent.setStudentGrade("12");
        traxStudent.setSchoolOfRecordId(UUID.randomUUID());
        traxStudent.setGraduationRequirementYear("2018");
        traxStudent.setStudentStatus("A");
        traxStudent.setArchiveFlag("A");
        traxStudent.setStudentCitizenship("C");

        TraxUpdatedPubEvent traxUpdatedPubEvent = TraxUpdatedPubEvent.builder()
                .eventType(EventType.NEWSTUDENT.toString())
                .eventId(UUID.randomUUID())
                .eventOutcome(EventOutcome.TRAX_STUDENT_MASTER_UPDATED.toString())
                .activityCode(traxUpdateInGradEntity.getUpdateType())
                .eventPayload(jsonString)
                .eventStatus(DB_COMMITTED.toString())
                .createUser(DEFAULT_CREATED_BY)
                .updateUser(DEFAULT_UPDATED_BY)
                .createDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();

        ResponseObj tokenObj = new ResponseObj();
        tokenObj.setAccess_token("123");
        when(restUtils.getTokenResponseObject()).thenReturn(tokenObj);
        when(traxUpdatedPubEventRepository.save(traxUpdatedPubEvent)).thenReturn(traxUpdatedPubEvent);
        when(traxCommonService.getStudentMasterDataFromTrax(pen)).thenReturn(Arrays.asList(traxStudent));

        traxUpdateService.publishTraxUpdatedEvent(traxUpdateInGradEntity);
        traxUpdateService.updateStatus(traxUpdateInGradEntity);

        assertThatNoException();
    }

    @Test
    public void testProcess_whenStudentDemographicInfo_isUpdated() throws JsonProcessingException {
        String pen = "123456789";

        TraxUpdateInGradEntity traxUpdateInGradEntity = new TraxUpdateInGradEntity();
        traxUpdateInGradEntity.setPen(pen);
        traxUpdateInGradEntity.setUpdateType("UPD_DEMOG");
        traxUpdateInGradEntity.setStatus("OUTSTANDING");
        traxUpdateInGradEntity.setUpdateDate(DateUtils.addDays(new Date(), -1));

        TraxDemographicsUpdateDTO payload = new TraxDemographicsUpdateDTO();
        payload.setPen(pen);
        payload.setLastName("Test");
        payload.setFirstName("QA");
        String jsonString = JsonUtil.getJsonStringFromObject(payload);

        ConvGradStudent traxStudent = new ConvGradStudent();
        traxStudent.setPen(pen);
        traxStudent.setStudentGrade("12");
        traxStudent.setSchoolOfRecordId(UUID.randomUUID());
        traxStudent.setGraduationRequirementYear("2018");
        traxStudent.setStudentStatus("A");
        traxStudent.setArchiveFlag("A");
        traxStudent.setStudentCitizenship("C");

        Student penStudent = new Student();
        penStudent.setPen(pen);
        penStudent.setStudentID(UUID.randomUUID().toString());
        penStudent.setLegalLastName("Test");
        penStudent.setLegalFirstName("QA");

        TraxUpdatedPubEvent traxUpdatedPubEvent = TraxUpdatedPubEvent.builder()
                .eventType(EventType.UPD_DEMOG.toString())
                .eventId(UUID.randomUUID())
                .eventOutcome(EventOutcome.TRAX_STUDENT_MASTER_UPDATED.toString())
                .activityCode(traxUpdateInGradEntity.getUpdateType())
                .eventPayload(jsonString)
                .eventStatus(DB_COMMITTED.toString())
                .createUser(DEFAULT_CREATED_BY)
                .updateUser(DEFAULT_UPDATED_BY)
                .createDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();

        ResponseObj tokenObj = new ResponseObj();
        tokenObj.setAccess_token("123");
        when(restUtils.getTokenResponseObject()).thenReturn(tokenObj);
        when(traxUpdatedPubEventRepository.save(traxUpdatedPubEvent)).thenReturn(traxUpdatedPubEvent);
        when(traxCommonService.getStudentMasterDataFromTrax(pen)).thenReturn(Arrays.asList(traxStudent));
        when(traxCommonService.getStudentDemographicsDataFromTrax(pen)).thenReturn(Arrays.asList(penStudent));

        traxUpdateService.publishTraxUpdatedEvent(traxUpdateInGradEntity);
        traxUpdateService.updateStatus(traxUpdateInGradEntity);

        assertThatNoException();
    }

    @Test
    public void testProcess_whenGraduation_isUpdated() throws JsonProcessingException {
        String pen = "123456789";
        String mincode = "12345678";
        UUID schoolId = UUID.randomUUID();

        TraxUpdateInGradEntity traxUpdateInGradEntity = new TraxUpdateInGradEntity();
        traxUpdateInGradEntity.setPen(pen);
        traxUpdateInGradEntity.setUpdateType("UPD_GRAD");
        traxUpdateInGradEntity.setStatus("OUTSTANDING");
        traxUpdateInGradEntity.setUpdateDate(DateUtils.addDays(new Date(), -1));

        TraxGraduationUpdateDTO payload = new TraxGraduationUpdateDTO();
        payload.setPen(pen);
        payload.setStudentGrade("12");
        payload.setSchoolOfRecordId(schoolId);
        payload.setCitizenship("C");
        payload.setGraduationRequirementYear("2018");
        String jsonString = JsonUtil.getJsonStringFromObject(payload);

        ConvGradStudent traxStudent = new ConvGradStudent();
        traxStudent.setPen(pen);
        traxStudent.setStudentGrade("12");
        traxStudent.setGraduationRequirementYear("2018");
        traxStudent.setStudentStatus("A");
        traxStudent.setArchiveFlag("A");
        traxStudent.setStudentCitizenship("C");
        traxStudent.setSchoolOfRecordId(schoolId);

        TraxUpdatedPubEvent traxUpdatedPubEvent = TraxUpdatedPubEvent.builder()
                .eventType(EventType.UPD_GRAD.toString())
                .eventId(UUID.randomUUID())
                .eventOutcome(EventOutcome.TRAX_STUDENT_MASTER_UPDATED.toString())
                .activityCode(traxUpdateInGradEntity.getUpdateType())
                .eventPayload(jsonString)
                .eventStatus(DB_COMMITTED.toString())
                .createUser(DEFAULT_CREATED_BY)
                .updateUser(DEFAULT_UPDATED_BY)
                .createDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();

        ResponseObj tokenObj = new ResponseObj();
        tokenObj.setAccess_token("123");
        when(restUtils.getTokenResponseObject()).thenReturn(tokenObj);
        when(traxUpdatedPubEventRepository.save(traxUpdatedPubEvent)).thenReturn(traxUpdatedPubEvent);
        when(traxCommonService.getStudentMasterDataFromTrax(pen)).thenReturn(Arrays.asList(traxStudent));
        when(commonService.getSchoolIdFromRedisCache(mincode)).thenReturn(schoolId );

        traxUpdateService.publishTraxUpdatedEvent(traxUpdateInGradEntity);
        traxUpdateService.updateStatus(traxUpdateInGradEntity);

        assertThatNoException();
    }

    @Test
    public void testProcess_whenStudentStatus_isUpdated() throws JsonProcessingException {
        String pen = "123456789";
        String mincode = "12345678";
        UUID schoolId = UUID.randomUUID();

        TraxUpdateInGradEntity traxUpdateInGradEntity = new TraxUpdateInGradEntity();
        traxUpdateInGradEntity.setPen(pen);
        traxUpdateInGradEntity.setUpdateType("UPD_GRAD");
        traxUpdateInGradEntity.setStatus("OUTSTANDING");
        traxUpdateInGradEntity.setUpdateDate(DateUtils.addDays(new Date(), -1));

        TraxGraduationUpdateDTO payload = new TraxGraduationUpdateDTO();
        payload.setPen(pen);
        payload.setStudentStatus("A");
        payload.setStudentStatus("A");
        payload.setSchoolOfRecordId(schoolId);
        String jsonString = JsonUtil.getJsonStringFromObject(payload);

        ConvGradStudent traxStudent = new ConvGradStudent();
        traxStudent.setPen(pen);
        traxStudent.setStudentGrade("12");
        traxStudent.setGraduationRequirementYear("2018");
        traxStudent.setStudentStatus("A");
        traxStudent.setArchiveFlag("A");
        traxStudent.setStudentCitizenship("C");
        traxStudent.setSchoolOfRecordId(schoolId);

        TraxUpdatedPubEvent traxUpdatedPubEvent = TraxUpdatedPubEvent.builder()
                .eventType(EventType.UPD_GRAD.toString())
                .eventId(UUID.randomUUID())
                .eventOutcome(EventOutcome.TRAX_STUDENT_MASTER_UPDATED.toString())
                .activityCode(traxUpdateInGradEntity.getUpdateType())
                .eventPayload(jsonString)
                .eventStatus(DB_COMMITTED.toString())
                .createUser(DEFAULT_CREATED_BY)
                .updateUser(DEFAULT_UPDATED_BY)
                .createDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();

        ResponseObj tokenObj = new ResponseObj();
        tokenObj.setAccess_token("123");
        when(restUtils.getTokenResponseObject()).thenReturn(tokenObj);
        when(traxUpdatedPubEventRepository.save(traxUpdatedPubEvent)).thenReturn(traxUpdatedPubEvent);
        when(traxCommonService.getStudentMasterDataFromTrax(pen)).thenReturn(Arrays.asList(traxStudent));
        when(commonService.getSchoolIdFromRedisCache(mincode)).thenReturn(schoolId );

        traxUpdateService.publishTraxUpdatedEvent(traxUpdateInGradEntity);
        traxUpdateService.updateStatus(traxUpdateInGradEntity);

        assertThatNoException();
    }

    @Test
    public void testProcess_whenXProgram_isUpdated() throws JsonProcessingException {
        String pen = "123456789";

        TraxUpdateInGradEntity traxUpdateInGradEntity = new TraxUpdateInGradEntity();
        traxUpdateInGradEntity.setPen(pen);
        traxUpdateInGradEntity.setUpdateType("XPROGRAM");
        traxUpdateInGradEntity.setStatus("OUTSTANDING");
        traxUpdateInGradEntity.setUpdateDate(DateUtils.addDays(new Date(), -1));

        TraxXProgramDTO payload = new TraxXProgramDTO();
        payload.setPen(pen);
        payload.setProgramList(Arrays.asList("XC"));
        String jsonString = JsonUtil.getJsonStringFromObject(payload);

        ConvGradStudent traxStudent = new ConvGradStudent();
        traxStudent.setPen(pen);
        traxStudent.setStudentGrade("12");
        traxStudent.setSchoolOfRecordId(UUID.randomUUID());
        traxStudent.setGraduationRequirementYear("2018");
        traxStudent.setStudentStatus("A");
        traxStudent.setArchiveFlag("A");
        traxStudent.setStudentCitizenship("C");
        traxStudent.setProgramCodes(Arrays.asList("XC"));

        TraxUpdatedPubEvent traxUpdatedPubEvent = TraxUpdatedPubEvent.builder()
                .eventType(EventType.XPROGRAM.toString())
                .eventId(UUID.randomUUID())
                .eventOutcome(EventOutcome.TRAX_STUDENT_MASTER_UPDATED.toString())
                .activityCode(traxUpdateInGradEntity.getUpdateType())
                .eventPayload(jsonString)
                .eventStatus(DB_COMMITTED.toString())
                .createUser(DEFAULT_CREATED_BY)
                .updateUser(DEFAULT_UPDATED_BY)
                .createDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();

        ResponseObj tokenObj = new ResponseObj();
        tokenObj.setAccess_token("123");
        when(restUtils.getTokenResponseObject()).thenReturn(tokenObj);
        when(traxUpdatedPubEventRepository.save(traxUpdatedPubEvent)).thenReturn(traxUpdatedPubEvent);
        when(traxCommonService.getStudentMasterDataFromTrax(pen)).thenReturn(Arrays.asList(traxStudent));

        traxUpdateService.publishTraxUpdatedEvent(traxUpdateInGradEntity);
        traxUpdateService.updateStatus(traxUpdateInGradEntity);

        assertThatNoException();
    }

    @Test
    public void testProcess_whenAssessment_isUpdated() throws JsonProcessingException {
        String pen = "123456789";

        TraxUpdateInGradEntity traxUpdateInGradEntity = new TraxUpdateInGradEntity();
        traxUpdateInGradEntity.setPen(pen);
        traxUpdateInGradEntity.setUpdateType("ASSESSMENT");
        traxUpdateInGradEntity.setStatus("OUTSTANDING");
        traxUpdateInGradEntity.setUpdateDate(DateUtils.addDays(new Date(), -1));

        TraxStudentUpdateDTO payload = new TraxStudentUpdateDTO();
        payload.setPen(pen);
        String jsonString = JsonUtil.getJsonStringFromObject(payload);

        ConvGradStudent traxStudent = new ConvGradStudent();
        traxStudent.setPen(pen);
        traxStudent.setStudentGrade("12");
        traxStudent.setSchoolOfRecordId(UUID.randomUUID());
        traxStudent.setGraduationRequirementYear("2018");
        traxStudent.setStudentStatus("A");
        traxStudent.setArchiveFlag("A");
        traxStudent.setStudentCitizenship("C");
        traxStudent.setProgramCodes(Arrays.asList("XC"));

        TraxUpdatedPubEvent traxUpdatedPubEvent = TraxUpdatedPubEvent.builder()
                .eventType(EventType.ASSESSMENT.toString())
                .eventId(UUID.randomUUID())
                .eventOutcome(EventOutcome.TRAX_STUDENT_MASTER_UPDATED.toString())
                .activityCode(traxUpdateInGradEntity.getUpdateType())
                .eventPayload(jsonString)
                .eventStatus(DB_COMMITTED.toString())
                .createUser(DEFAULT_CREATED_BY)
                .updateUser(DEFAULT_UPDATED_BY)
                .createDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();

        ResponseObj tokenObj = new ResponseObj();
        tokenObj.setAccess_token("123");
        when(restUtils.getTokenResponseObject()).thenReturn(tokenObj);
        when(traxUpdatedPubEventRepository.save(traxUpdatedPubEvent)).thenReturn(traxUpdatedPubEvent);
        when(traxCommonService.getStudentMasterDataFromTrax(pen)).thenReturn(Arrays.asList(traxStudent));

        traxUpdateService.publishTraxUpdatedEvent(traxUpdateInGradEntity);
        traxUpdateService.updateStatus(traxUpdateInGradEntity);

        assertThatNoException();
    }

    @Test
    public void testProcess_whenCourse_isUpdated() throws JsonProcessingException {
        String pen = "123456789";

        TraxUpdateInGradEntity traxUpdateInGradEntity = new TraxUpdateInGradEntity();
        traxUpdateInGradEntity.setPen(pen);
        traxUpdateInGradEntity.setUpdateType("COURSE");
        traxUpdateInGradEntity.setStatus("OUTSTANDING");
        traxUpdateInGradEntity.setUpdateDate(DateUtils.addDays(new Date(), -1));

        TraxStudentUpdateDTO payload = new TraxStudentUpdateDTO();
        payload.setPen(pen);
        String jsonString = JsonUtil.getJsonStringFromObject(payload);

        ConvGradStudent traxStudent = new ConvGradStudent();
        traxStudent.setPen(pen);
        traxStudent.setStudentGrade("12");
        traxStudent.setSchoolOfRecordId(UUID.randomUUID());
        traxStudent.setGraduationRequirementYear("2018");
        traxStudent.setStudentStatus("A");
        traxStudent.setArchiveFlag("A");
        traxStudent.setStudentCitizenship("C");
        traxStudent.setProgramCodes(Arrays.asList("XC"));

        TraxUpdatedPubEvent traxUpdatedPubEvent = TraxUpdatedPubEvent.builder()
                .eventType(EventType.COURSE.toString())
                .eventId(UUID.randomUUID())
                .eventOutcome(EventOutcome.TRAX_STUDENT_MASTER_UPDATED.toString())
                .activityCode(traxUpdateInGradEntity.getUpdateType())
                .eventPayload(jsonString)
                .eventStatus(DB_COMMITTED.toString())
                .createUser(DEFAULT_CREATED_BY)
                .updateUser(DEFAULT_UPDATED_BY)
                .createDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();

        ResponseObj tokenObj = new ResponseObj();
        tokenObj.setAccess_token("123");
        when(restUtils.getTokenResponseObject()).thenReturn(tokenObj);
        when(traxUpdatedPubEventRepository.save(traxUpdatedPubEvent)).thenReturn(traxUpdatedPubEvent);
        when(traxCommonService.getStudentMasterDataFromTrax(pen)).thenReturn(Arrays.asList(traxStudent));

        traxUpdateService.publishTraxUpdatedEvent(traxUpdateInGradEntity);
        traxUpdateService.updateStatus(traxUpdateInGradEntity);

        assertThatNoException();
    }

    @Test
    public void testProcess_whenFrenchImmersion_forGrade10_isAdded() throws JsonProcessingException {
        String pen = "123456789";

        TraxUpdateInGradEntity traxUpdateInGradEntity = new TraxUpdateInGradEntity();
        traxUpdateInGradEntity.setPen(pen);
        traxUpdateInGradEntity.setUpdateType("FI10ADD");
        traxUpdateInGradEntity.setStatus("OUTSTANDING");
        traxUpdateInGradEntity.setUpdateDate(DateUtils.addDays(new Date(), -1));

        TraxFrenchImmersionUpdateDTO payload = new TraxFrenchImmersionUpdateDTO();
        payload.setPen(pen);
        payload.setCourseCode("FRAL");
        payload.setCourseLevel("10");
        payload.setGraduationRequirementYear("2018");
        String jsonString = JsonUtil.getJsonStringFromObject(payload);

        ConvGradStudent traxStudent = new ConvGradStudent();
        traxStudent.setPen(pen);
        traxStudent.setStudentGrade("12");
        traxStudent.setSchoolOfRecordId(UUID.randomUUID());
        traxStudent.setGraduationRequirementYear("2018");
        traxStudent.setStudentStatus("A");
        traxStudent.setArchiveFlag("A");
        traxStudent.setStudentCitizenship("C");
        traxStudent.setProgramCodes(Arrays.asList("XC"));

        TraxUpdatedPubEvent traxUpdatedPubEvent = TraxUpdatedPubEvent.builder()
                .eventType(EventType.FI10ADD.toString())
                .eventId(UUID.randomUUID())
                .eventOutcome(EventOutcome.TRAX_STUDENT_MASTER_UPDATED.toString())
                .activityCode(traxUpdateInGradEntity.getUpdateType())
                .eventPayload(jsonString)
                .eventStatus(DB_COMMITTED.toString())
                .createUser(DEFAULT_CREATED_BY)
                .updateUser(DEFAULT_UPDATED_BY)
                .createDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();

        ResponseObj tokenObj = new ResponseObj();
        tokenObj.setAccess_token("123");
        when(restUtils.getTokenResponseObject()).thenReturn(tokenObj);
        when(traxUpdatedPubEventRepository.save(traxUpdatedPubEvent)).thenReturn(traxUpdatedPubEvent);
        when(traxCommonService.getStudentMasterDataFromTrax(pen)).thenReturn(Arrays.asList(traxStudent));

        traxUpdateService.publishTraxUpdatedEvent(traxUpdateInGradEntity);
        traxUpdateService.updateStatus(traxUpdateInGradEntity);

        assertThatNoException();
    }
}
