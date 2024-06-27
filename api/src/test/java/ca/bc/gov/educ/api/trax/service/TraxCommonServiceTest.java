package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.messaging.NatsConnection;
import ca.bc.gov.educ.api.trax.messaging.jetstream.Publisher;
import ca.bc.gov.educ.api.trax.messaging.jetstream.Subscriber;
import ca.bc.gov.educ.api.trax.model.dto.*;
import ca.bc.gov.educ.api.trax.model.entity.GradCourseEntity;
import ca.bc.gov.educ.api.trax.model.entity.GradCourseKey;
import ca.bc.gov.educ.api.trax.model.entity.TraxStudentNoEntity;
import ca.bc.gov.educ.api.trax.model.transformer.GradCourseTransformer;
import ca.bc.gov.educ.api.trax.model.transformer.TraxStudentNoTransformer;
import ca.bc.gov.educ.api.trax.repository.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.*;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class TraxCommonServiceTest {

    @Autowired
    TraxCommonService traxCommonService;

    @Autowired
    GradCourseTransformer gradCourseTransformer;

    @Autowired
    TraxStudentNoTransformer traxStudentNoTransformer;

    @MockBean
    GradCourseRepository gradCourseRepository;

    @MockBean
    TraxStudentRepository traxStudentRepository;

    @MockBean
    TraxStudentNoRepository traxStudentNoRepository;

    @MockBean
    SchoolService schoolService;

    // NATS
    @MockBean
    private NatsConnection natsConnection;

    @MockBean
    private Publisher publisher;

    @MockBean
    private Subscriber subscriber;
    @MockBean
    private JedisConnectionFactory jedisConnectionFactory;

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
    public void testGetStudentMasterDataFromTrax() {
        Object[] obj = new Object[] {
                "123456789", "12345678", "12345678", "AD", Character.valueOf('A'),Character.valueOf('A'), "1950", Integer.valueOf(0),
                Integer.valueOf(0), null, null, null, null, null, null, null, null, null, null, Character.valueOf('C'), Character.valueOf('N'), null
        };
        List<Object[]> results = new ArrayList<>();
        results.add(obj);

        String pen = (String) obj[0];
        Character status = (Character)obj[4];

        Object[] cols = new Object[] {
                "1950", Integer.valueOf(0), Integer.valueOf(0), null
        };
        List<Object[]> list = new ArrayList<>();
        list.add(cols);

        when(this.traxStudentRepository.loadTraxStudent(pen)).thenReturn(results);

        var result = traxCommonService.getStudentMasterDataFromTrax(pen);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        ConvGradStudent responseObject = result.get(0);
        assertThat(responseObject.getPen()).isEqualTo(pen);
        assertThat(responseObject.getStudentStatus()).isEqualTo(status.toString());
    }

//    @Test
//    public void testGetGraduatedStudentMasterDataFromTraxFor1950AdultProgram_whenAllowedAdult_isYes_thenReturns_adult19RuleAsFalse() {
//        Object[] obj = new Object[] {
//                "123456789", "1234567", "1234568", "AD", Character.valueOf('A'),Character.valueOf('A'), "1950", "202206",
//                Integer.valueOf(0), Integer.valueOf(0), null, null, null, null, null, "S", null, "E", null, Character.valueOf('C'), Character.valueOf('N'), Character.valueOf('Y')
//        };
//        List<Object[]> results = new ArrayList<>();
//        results.add(obj);
//
//        final String pen = (String) obj[0];
//        final String mincode = (String) obj[1];
//        final String mincodeAtGrad = (String) obj[2];
//        Character status = (Character)obj[4];
//
//        // TSW Demographics
//        TranscriptStudentDemog transcriptStudentDemog = new TranscriptStudentDemog();
//        transcriptStudentDemog.setStudNo(pen);
//        transcriptStudentDemog.setMincode(mincode);
//        transcriptStudentDemog.setGradDate(202201L);
//
//        // TSW Courses
//        TranscriptStudentCourse tswCourse1 = new TranscriptStudentCourse();
//        tswCourse1.setStudNo(pen);
//        tswCourse1.setReportType("1");
//        tswCourse1.setCourseCode("Generic");
//        tswCourse1.setCourseName("Generic Course Name");
//        tswCourse1.setCourseLevel("12");
//        tswCourse1.setFinalPercentage("91.00");
//        tswCourse1.setFinalLG("A");
//        tswCourse1.setCourseSession("202206");
//        tswCourse1.setNumberOfCredits("4");
//        tswCourse1.setUsedForGrad("4");
//        tswCourse1.setFoundationReq("10");
//        tswCourse1.setUpdateDate(20220601L);
//
//        TranscriptStudentCourse tswCourse2 = new TranscriptStudentCourse();
//        tswCourse2.setStudNo(pen);
//        tswCourse2.setReportType("2");
//        tswCourse2.setCourseCode("TestCourse");
//        tswCourse2.setCourseName("Test Course Name");
//        tswCourse2.setCourseLevel("12");
//        tswCourse2.setFinalPercentage("92.00");
//        tswCourse2.setFinalLG("A");
//        tswCourse2.setCourseSession("202206");
//        tswCourse2.setNumberOfCredits("4");
//        tswCourse2.setUsedForGrad("4");
//        tswCourse2.setFoundationReq("11");
//        tswCourse2.setSpecialCase("E");
//        tswCourse2.setUpdateDate(20220601L);
//
//        TranscriptStudentCourse tswCourse3 = new TranscriptStudentCourse();
//        tswCourse2.setStudNo(pen);
//        tswCourse2.setReportType("2");
//        tswCourse2.setCourseCode("TestCourse2");
//        tswCourse2.setCourseName("Test Course2 Name");
//        tswCourse2.setCourseLevel("12");
//        tswCourse2.setFinalPercentage("XMT");
//        tswCourse2.setFinalLG("A");
//        tswCourse2.setCourseSession("202206");
//        tswCourse2.setNumberOfCredits("4");
//        tswCourse2.setUsedForGrad("4");
//        tswCourse2.setFoundationReq("11");
//        tswCourse2.setUpdateDate(20220601L);
//
//        TranscriptStudentCourse tswAssessment = new TranscriptStudentCourse();
//        tswAssessment.setStudNo(pen);
//        tswAssessment.setReportType("3");
//        tswAssessment.setCourseCode("TestAssmt");
//        tswAssessment.setCourseName("Test Assessment Name");
//        tswAssessment.setCourseLevel("12");
//        tswAssessment.setFinalPercentage("XMT");
//        tswAssessment.setCourseSession("202206");
//        tswAssessment.setFoundationReq("15");
//        tswAssessment.setUpdateDate(new Date(System.currentTimeMillis() - 100000L).getTime());
//
//        School school = new School();
//        school.setMinCode(mincode);
//        school.setSchoolCategory("02");
//
//        CommonSchool commonSchool = new CommonSchool();
//        commonSchool.setSchlNo(mincode);
//        commonSchool.setSchoolName("Test School");
//        commonSchool.setSchoolCategoryCode("02");
//
//        School schoolAtGrad = new School();
//        schoolAtGrad.setMinCode(mincodeAtGrad);
//        schoolAtGrad.setSchoolCategory("02");
//
//        CommonSchool commonSchoolAtGrad = new CommonSchool();
//        commonSchoolAtGrad.setSchlNo(mincodeAtGrad);
//        commonSchoolAtGrad.setSchoolName("Test GRAD School");
//        commonSchoolAtGrad.setSchoolCategoryCode("02");
//
//        when(this.schoolService.getSchoolDetails(mincode, "123")).thenReturn(school);
//        when(this.schoolService.getCommonSchool("123", mincode)).thenReturn(commonSchool);
//
//        when(this.schoolService.getSchoolDetails(mincodeAtGrad, "123")).thenReturn(schoolAtGrad);
//        when(this.schoolService.getCommonSchool("123", mincode)).thenReturn(commonSchoolAtGrad);
//
//        Object[] cols = new Object[] {
//                "1950", Integer.valueOf(202206), Integer.valueOf(0), Integer.valueOf(0)
//        };
//        List<Object[]> list = new ArrayList<>();
//        list.add(cols);
//
//        when(this.traxStudentRepository.getGraduationData(pen)).thenReturn(list);
//        when(this.traxStudentRepository.loadTraxGraduatedStudent(pen)).thenReturn(results);
//        when(this.tswService.existsTranscriptStudentDemog(pen)).thenReturn(true);
//        when(this.tswService.getTranscriptStudentDemog(pen)).thenReturn(transcriptStudentDemog);
//        when(this.tswService.getTranscriptStudentCourses(pen)).thenReturn(Arrays.asList(tswCourse1, tswCourse2, tswCourse3, tswAssessment));
//
//        var result = traxCommonService.getStudentMasterDataFromTrax(pen, "123");
//
//        assertThat(result).hasSize(1);
//        ConvGradStudent responseObject = result.get(0);
//        assertThat(responseObject.getPen()).isEqualTo(pen);
//        assertThat(responseObject.getStudentStatus()).isEqualTo(status.toString());
//        assertThat(responseObject.getStudentLoadType()).isEqualTo(StudentLoadType.GRAD_ONE);
//        assertThat(responseObject.getTranscriptStudentDemog()).isNotNull();
//        assertThat(responseObject.getTranscriptStudentCourses()).hasSize(4);
//        assertThat(responseObject.isAdult19Rule()).isFalse();
//    }
//
//    @Test
//    public void testGetGraduatedStudentMasterDataFromTraxFor1950AdultProgram_whenAllowedAdult_isNull_andGradDate_isBefore201207_thenReturns_adult19RuleAsTrue() {
//        Object[] obj = new Object[] {
//                "123456789", "1234567", "1234568", "AD", Character.valueOf('A'),Character.valueOf('A'), "1950", "201206",
//                Integer.valueOf(0), Integer.valueOf(0), null, null, null, null, null, "S", null, "E", null, Character.valueOf('C'), Character.valueOf('N'), null
//        };
//        List<Object[]> results = new ArrayList<>();
//        results.add(obj);
//
//        final String pen = (String) obj[0];
//        final String mincode = (String) obj[1];
//        final String mincodeAtGrad = (String) obj[2];
//        Character status = (Character)obj[4];
//
//        // TSW Demographics
//        TranscriptStudentDemog transcriptStudentDemog = new TranscriptStudentDemog();
//        transcriptStudentDemog.setStudNo(pen);
//        transcriptStudentDemog.setMincode(mincode);
//        transcriptStudentDemog.setGradDate(202201L);
//
//        // TSW Courses
//        TranscriptStudentCourse tswCourse1 = new TranscriptStudentCourse();
//        tswCourse1.setStudNo(pen);
//        tswCourse1.setReportType("1");
//        tswCourse1.setCourseCode("Generic");
//        tswCourse1.setCourseName("Generic Course Name");
//        tswCourse1.setCourseLevel("12");
//        tswCourse1.setFinalPercentage("91.00");
//        tswCourse1.setFinalLG("A");
//        tswCourse1.setCourseSession("202206");
//        tswCourse1.setNumberOfCredits("4");
//        tswCourse1.setUsedForGrad("4");
//        tswCourse1.setFoundationReq("10");
//        tswCourse1.setUpdateDate(20220601L);
//
//        TranscriptStudentCourse tswCourse2 = new TranscriptStudentCourse();
//        tswCourse2.setStudNo(pen);
//        tswCourse2.setReportType("2");
//        tswCourse2.setCourseCode("TestCourse");
//        tswCourse2.setCourseName("Test Course Name");
//        tswCourse2.setCourseLevel("12");
//        tswCourse2.setFinalPercentage("92.00");
//        tswCourse2.setFinalLG("A");
//        tswCourse2.setCourseSession("202206");
//        tswCourse2.setNumberOfCredits("4");
//        tswCourse2.setUsedForGrad("4");
//        tswCourse2.setFoundationReq("11");
//        tswCourse2.setSpecialCase("E");
//        tswCourse2.setUpdateDate(20220601L);
//
//        TranscriptStudentCourse tswCourse3 = new TranscriptStudentCourse();
//        tswCourse2.setStudNo(pen);
//        tswCourse2.setReportType("2");
//        tswCourse2.setCourseCode("TestCourse2");
//        tswCourse2.setCourseName("Test Course2 Name");
//        tswCourse2.setCourseLevel("12");
//        tswCourse2.setFinalPercentage("XMT");
//        tswCourse2.setFinalLG("A");
//        tswCourse2.setCourseSession("202206");
//        tswCourse2.setNumberOfCredits("4");
//        tswCourse2.setUsedForGrad("4");
//        tswCourse2.setFoundationReq("11");
//        tswCourse2.setUpdateDate(20220601L);
//
//        TranscriptStudentCourse tswAssessment = new TranscriptStudentCourse();
//        tswAssessment.setStudNo(pen);
//        tswAssessment.setReportType("3");
//        tswAssessment.setCourseCode("TestAssmt");
//        tswAssessment.setCourseName("Test Assessment Name");
//        tswAssessment.setCourseLevel("12");
//        tswAssessment.setFinalPercentage("XMT");
//        tswAssessment.setCourseSession("202206");
//        tswAssessment.setFoundationReq("15");
//        tswAssessment.setUpdateDate(new Date(System.currentTimeMillis() - 100000L).getTime());
//
//        School school = new School();
//        school.setMinCode(mincode);
//        school.setSchoolCategory("02");
//
//        CommonSchool commonSchool = new CommonSchool();
//        commonSchool.setSchlNo(mincode);
//        commonSchool.setSchoolName("Test School");
//        commonSchool.setSchoolCategoryCode("02");
//
//        School schoolAtGrad = new School();
//        schoolAtGrad.setMinCode(mincodeAtGrad);
//        schoolAtGrad.setSchoolCategory("02");
//
//        CommonSchool commonSchoolAtGrad = new CommonSchool();
//        commonSchoolAtGrad.setSchlNo(mincodeAtGrad);
//        commonSchoolAtGrad.setSchoolName("Test GRAD School");
//        commonSchoolAtGrad.setSchoolCategoryCode("02");
//
//        when(this.schoolService.getSchoolDetails(mincode, "123")).thenReturn(school);
//        when(this.schoolService.getCommonSchool("123", mincode)).thenReturn(commonSchool);
//        when(this.schoolService.getSchoolDetails(mincodeAtGrad, "123")).thenReturn(schoolAtGrad);
//        when(this.schoolService.getCommonSchool("123", mincode)).thenReturn(commonSchoolAtGrad);
//
//        Object[] cols = new Object[] {
//                "1950", Integer.valueOf(201206), Integer.valueOf(0), Integer.valueOf(0)
//        };
//        List<Object[]> list = new ArrayList<>();
//        list.add(cols);
//
//        when(this.traxStudentRepository.getGraduationData(pen)).thenReturn(list);
//
//        when(this.traxStudentRepository.loadTraxGraduatedStudent(pen)).thenReturn(results);
//        when(this.tswService.existsTranscriptStudentDemog(pen)).thenReturn(true);
//        when(this.tswService.getTranscriptStudentDemog(pen)).thenReturn(transcriptStudentDemog);
//        when(this.tswService.getTranscriptStudentCourses(pen)).thenReturn(Arrays.asList(tswCourse1, tswCourse2, tswCourse3, tswAssessment));
//
//        var result = traxCommonService.getStudentMasterDataFromTrax(pen, "123");
//
//        assertThat(result).hasSize(1);
//        ConvGradStudent responseObject = result.get(0);
//        assertThat(responseObject.getPen()).isEqualTo(pen);
//        assertThat(responseObject.getStudentStatus()).isEqualTo(status.toString());
//        assertThat(responseObject.getStudentLoadType()).isEqualTo(StudentLoadType.GRAD_ONE);
//        assertThat(responseObject.getTranscriptStudentDemog()).isNotNull();
//        assertThat(responseObject.getTranscriptStudentCourses()).hasSize(4);
//        assertThat(responseObject.isAdult19Rule()).isTrue();
//    }
//
//    @Test
//    public void testGetGraduatedStudentMasterDataFromTraxForNone() {
//        Object[] obj = new Object[] {
//                "123456789", "1234567", "1234567", "12", Character.valueOf('A'),Character.valueOf('A'), "SCCP", "201206",
//                Integer.valueOf(0), Integer.valueOf(0), null, null, null, null, null, "S", null, "E", null, Character.valueOf('C'), Character.valueOf('N'), null
//        };
//        List<Object[]> results = new ArrayList<>();
//        results.add(obj);
//
//        final String pen = (String) obj[0];
//        final String mincode = (String) obj[1];
//        Character status = (Character)obj[4];
//
//        // TSW Demographics
//        TranscriptStudentDemog transcriptStudentDemog = new TranscriptStudentDemog();
//        transcriptStudentDemog.setStudNo(pen);
//        transcriptStudentDemog.setMincode(mincode);
//        transcriptStudentDemog.setGradDate(202201L);
//
//        // TSW Courses
//        TranscriptStudentCourse tswCourse1 = new TranscriptStudentCourse();
//        tswCourse1.setStudNo(pen);
//        tswCourse1.setReportType("1");
//        tswCourse1.setCourseCode("Generic");
//        tswCourse1.setCourseName("Generic Course Name");
//        tswCourse1.setCourseLevel("12");
//        tswCourse1.setFinalPercentage("91.00");
//        tswCourse1.setFinalLG("A");
//        tswCourse1.setCourseSession("202206");
//        tswCourse1.setNumberOfCredits("4");
//        tswCourse1.setUsedForGrad("4");
//        tswCourse1.setFoundationReq("10");
//        tswCourse1.setUpdateDate(20220601L);
//
//        TranscriptStudentCourse tswCourse2 = new TranscriptStudentCourse();
//        tswCourse2.setStudNo(pen);
//        tswCourse2.setReportType("2");
//        tswCourse2.setCourseCode("TestCourse");
//        tswCourse2.setCourseName("Test Course Name");
//        tswCourse2.setCourseLevel("12");
//        tswCourse2.setFinalPercentage("92.00");
//        tswCourse2.setFinalLG("A");
//        tswCourse2.setCourseSession("202206");
//        tswCourse2.setNumberOfCredits("4");
//        tswCourse2.setUsedForGrad("4");
//        tswCourse2.setFoundationReq("11");
//        tswCourse2.setSpecialCase("E");
//        tswCourse2.setUpdateDate(20220601L);
//
//        TranscriptStudentCourse tswCourse3 = new TranscriptStudentCourse();
//        tswCourse2.setStudNo(pen);
//        tswCourse2.setReportType("2");
//        tswCourse2.setCourseCode("TestCourse2");
//        tswCourse2.setCourseName("Test Course2 Name");
//        tswCourse2.setCourseLevel("12");
//        tswCourse2.setFinalPercentage("XMT");
//        tswCourse2.setFinalLG("A");
//        tswCourse2.setCourseSession("202206");
//        tswCourse2.setNumberOfCredits("4");
//        tswCourse2.setUsedForGrad("4");
//        tswCourse2.setFoundationReq("11");
//        tswCourse2.setUpdateDate(20220601L);
//
//        TranscriptStudentCourse tswAssessment = new TranscriptStudentCourse();
//        tswAssessment.setStudNo(pen);
//        tswAssessment.setReportType("3");
//        tswAssessment.setCourseCode("TestAssmt");
//        tswAssessment.setCourseName("Test Assessment Name");
//        tswAssessment.setCourseLevel("12");
//        tswAssessment.setFinalPercentage("XMT");
//        tswAssessment.setCourseSession("202206");
//        tswAssessment.setFoundationReq("15");
//        tswAssessment.setUpdateDate(new Date(System.currentTimeMillis() - 100000L).getTime());
//
//        School school = new School();
//        school.setMinCode(mincode);
//        school.setSchoolCategory("02");
//
//        CommonSchool commonSchool = new CommonSchool();
//        commonSchool.setSchlNo(mincode);
//        commonSchool.setSchoolName("Test School");
//        commonSchool.setSchoolCategoryCode("02");
//
//        Object[] cols = new Object[] {
//                "SCCP", Integer.valueOf(201206), Integer.valueOf(0), Integer.valueOf(0)
//        };
//        List<Object[]> list = new ArrayList<>();
//        list.add(cols);
//
//        when(this.traxStudentRepository.getGraduationData(pen)).thenReturn(list);
//        when(this.traxStudentRepository.loadTraxGraduatedStudent(pen)).thenReturn(results);
//        when(this.tswService.existsTranscriptStudentDemog(pen)).thenReturn(true);
//        when(this.tswService.getTranscriptStudentDemog(pen)).thenReturn(transcriptStudentDemog);
//        when(this.tswService.getTranscriptStudentCourses(pen)).thenReturn(Arrays.asList(tswCourse1, tswCourse2, tswCourse3, tswAssessment));
//        when(this.schoolService.getSchoolDetails(mincode, "123")).thenReturn(school);
//        when(this.schoolService.getCommonSchool("123", mincode)).thenReturn(commonSchool);
//
//        var result = traxCommonService.getStudentMasterDataFromTrax(pen, "123");
//
//        assertThat(result).hasSize(1);
//        ConvGradStudent responseObject = result.get(0);
//        assertThat(responseObject.getPen()).isEqualTo(pen);
//        assertThat(responseObject.getStudentLoadType()).isEqualTo(StudentLoadType.NONE);
//        assertThat(responseObject.getResult()).isEqualTo(ConversionResultType.FAILURE);
//    }
//
//    @Test
//    public void testGetGraduatedStudentMasterDataFromTraxForTwoPrograms() {
//        Object[] obj = new Object[] {
//                "123456789", "1234567", "1234567", "12", Character.valueOf('A'),Character.valueOf('A'), "2018", "0",
//                Integer.valueOf(202206), Integer.valueOf(202206), null, null, null, null, null, "S", null, "E", null, Character.valueOf('C'), Character.valueOf('N'), null
//        };
//        List<Object[]> results = new ArrayList<>();
//        results.add(obj);
//
//        final String pen = (String) obj[0];
//        final String mincode = (String) obj[1];
//        Character status = (Character)obj[4];
//
//        // TSW Demographics
//        TranscriptStudentDemog transcriptStudentDemog = new TranscriptStudentDemog();
//        transcriptStudentDemog.setStudNo(pen);
//        transcriptStudentDemog.setMincode(mincode);
//        transcriptStudentDemog.setGradDate(202201L);
//
//        // TSW Courses
//        TranscriptStudentCourse tswCourse1 = new TranscriptStudentCourse();
//        tswCourse1.setStudNo(pen);
//        tswCourse1.setReportType("1");
//        tswCourse1.setCourseCode("Generic");
//        tswCourse1.setCourseName("Generic Course Name");
//        tswCourse1.setCourseLevel("12");
//        tswCourse1.setFinalPercentage("91.00");
//        tswCourse1.setFinalLG("A");
//        tswCourse1.setCourseSession("202206");
//        tswCourse1.setNumberOfCredits("4");
//        tswCourse1.setUsedForGrad("4");
//        tswCourse1.setFoundationReq("10");
//        tswCourse1.setUpdateDate(20220601L);
//
//        TranscriptStudentCourse tswCourse2 = new TranscriptStudentCourse();
//        tswCourse2.setStudNo(pen);
//        tswCourse2.setReportType("2");
//        tswCourse2.setCourseCode("TestCourse");
//        tswCourse2.setCourseName("Test Course Name");
//        tswCourse2.setCourseLevel("12");
//        tswCourse2.setFinalPercentage("92.00");
//        tswCourse2.setFinalLG("A");
//        tswCourse2.setCourseSession("202206");
//        tswCourse2.setNumberOfCredits("4");
//        tswCourse2.setUsedForGrad("4");
//        tswCourse2.setFoundationReq("11");
//        tswCourse2.setSpecialCase("E");
//        tswCourse2.setUpdateDate(20220601L);
//
//        TranscriptStudentCourse tswCourse3 = new TranscriptStudentCourse();
//        tswCourse2.setStudNo(pen);
//        tswCourse2.setReportType("2");
//        tswCourse2.setCourseCode("TestCourse2");
//        tswCourse2.setCourseName("Test Course2 Name");
//        tswCourse2.setCourseLevel("12");
//        tswCourse2.setFinalPercentage("XMT");
//        tswCourse2.setFinalLG("A");
//        tswCourse2.setCourseSession("202206");
//        tswCourse2.setNumberOfCredits("4");
//        tswCourse2.setUsedForGrad("4");
//        tswCourse2.setFoundationReq("11");
//        tswCourse2.setUpdateDate(20220601L);
//
//        TranscriptStudentCourse tswAssessment = new TranscriptStudentCourse();
//        tswAssessment.setStudNo(pen);
//        tswAssessment.setReportType("3");
//        tswAssessment.setCourseCode("TestAssmt");
//        tswAssessment.setCourseName("Test Assessment Name");
//        tswAssessment.setCourseLevel("12");
//        tswAssessment.setFinalPercentage("XMT");
//        tswAssessment.setCourseSession("202206");
//        tswAssessment.setFoundationReq("15");
//        tswAssessment.setUpdateDate(new Date(System.currentTimeMillis() - 100000L).getTime());
//
//        School school = new School();
//        school.setMinCode(mincode);
//        school.setSchoolCategory("02");
//
//        CommonSchool commonSchool = new CommonSchool();
//        commonSchool.setSchlNo(mincode);
//        commonSchool.setSchoolName("Test School");
//        commonSchool.setSchoolCategoryCode("02");
//
//        // grad_reqt_year, grad_date, scc_date, slp_date
//        Object[] cols = new Object[] {
//                "2018", Integer.valueOf(0), Integer.valueOf(202206), Integer.valueOf(202206)
//        };
//        List<Object[]> list = new ArrayList<>();
//        list.add(cols);
//
//        when(this.traxStudentRepository.getGraduationData(pen)).thenReturn(list);
//        when(this.traxStudentRepository.loadTraxGraduatedStudent(pen)).thenReturn(results);
//        when(this.tswService.existsTranscriptStudentDemog(pen)).thenReturn(true);
//        when(this.tswService.getTranscriptStudentDemog(pen)).thenReturn(transcriptStudentDemog);
//        when(this.tswService.getTranscriptStudentCourses(pen)).thenReturn(Arrays.asList(tswCourse1, tswCourse2, tswCourse3, tswAssessment));
//        when(this.schoolService.getSchoolDetails(mincode, "123")).thenReturn(school);
//        when(this.schoolService.getCommonSchool("123", mincode)).thenReturn(commonSchool);
//
//        var result = traxCommonService.getStudentMasterDataFromTrax(pen, "123");
//
//        assertThat(result).hasSize(1);
//        ConvGradStudent responseObject = result.get(0);
//        assertThat(responseObject.getPen()).isEqualTo(pen);
//        assertThat(responseObject.getStudentLoadType()).isEqualTo(StudentLoadType.GRAD_TWO);
//        assertThat(responseObject.getResult()).isEqualTo(ConversionResultType.SUCCESS);
//    }

    @Test
    public void testGetStudentDemographicsDataFromTrax() {
        Object[] obj = new Object[] {
                "123456789", "Test", "QA", "", Character.valueOf('A'),Character.valueOf('A'), "12345678", "12", "V4N3Y2", Character.valueOf('M'), "19800111",  BigDecimal.valueOf(202005), null, "            "
        };
        List<Object[]> results = new ArrayList<>();
        results.add(obj);

        String pen = (String) obj[0];
        Character status = (Character)obj[4];

        when(this.traxStudentRepository.loadStudentDemographicsData(pen)).thenReturn(results);

        var result = traxCommonService.getStudentDemographicsDataFromTrax(pen);

        assertThat(result).hasSize(1);
        Student responseObject = result.get(0);
        assertThat(responseObject.getPen()).isEqualTo(pen);
        assertThat(responseObject.getStatusCode()).isEqualTo(status.toString());
    }

    @Test
    public void testLoadTraxStudentNoByPage() {
        int pageNumber = 1;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("studNo").ascending());

        TraxStudentNoEntity traxStudentNoEntity = new TraxStudentNoEntity();
        traxStudentNoEntity.setStudNo("123456789");

        Page<TraxStudentNoEntity> pages = new PageImpl<TraxStudentNoEntity>(Arrays.asList(traxStudentNoEntity), pageable, 1);

        when(this.traxStudentNoRepository.findAllByStatus(null, pageable)).thenReturn(pages);

        var result = traxCommonService.loadTraxStudentNoByPage(pageable);

        assertThat(result).hasSize(1);
        TraxStudentNo responseObject = result.get(0);
        assertThat(responseObject.getStudNo()).isEqualTo(traxStudentNoEntity.getStudNo());

    }

    @Test
    public void testGetTotalNumberOfTraxStudentNo() {
        when(this.traxStudentNoRepository.countAllByStatus(null)).thenReturn(1);

        var result = traxCommonService.getTotalNumberOfTraxStudentNo();

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(1);
    }

    @Test
    public void testLoadGradCourseRestrictionsDataFromTrax() {
        Object[] obj = new Object[] {
                "main", "12", "test", "12", null, null
        };
        List<Object[]> results = new ArrayList<>();
        results.add(obj);

        String pen = (String) obj[0];
        Character status = (Character)obj[4];

        when(this.traxStudentRepository.loadInitialCourseRestrictionRawData()).thenReturn(results);

        var result = traxCommonService.loadGradCourseRestrictionsDataFromTrax();

        assertThat(result).hasSize(1);
        CourseRestriction responseObject = result.get(0);
        assertThat(responseObject.getMainCourse()).isEqualTo("main");
        assertThat(responseObject.getMainCourseLevel()).isEqualTo("12");
        assertThat(responseObject.getRestrictedCourse()).isEqualTo("test");
        assertThat(responseObject.getRestrictedCourseLevel()).isEqualTo("12");
    }

    @Test
    public void testLoadGradCourseRequirementsDataFromTrax() {
        GradCourseEntity gradCourseEntity = new GradCourseEntity();
        GradCourseKey key = new GradCourseKey();
        key.setCourseCode("main");
        key.setCourseLevel("12");
        key.setGradReqtYear("2018");

        gradCourseEntity.setGradCourseKey(key);
        gradCourseEntity.setEnglish10("Y");

        when(this.gradCourseRepository.findAll()).thenReturn(Arrays.asList(gradCourseEntity));

        var result = traxCommonService.loadGradCourseRequirementsDataFromTrax();

        assertThat(result).hasSize(1);
        GradCourse responseObject = result.get(0);
        assertThat(responseObject.getCourseCode()).isEqualTo("main");
        assertThat(responseObject.getCourseLevel()).isEqualTo("12");
        assertThat(responseObject.getGradReqtYear()).isEqualTo("2018");

    }

    @Test
    public void testSaveTraxStudentNo() {
        TraxStudentNo traxStudentNo = new TraxStudentNo();
        traxStudentNo.setStudNo("123456789");
        traxStudentNo.setStatus("Y");

        TraxStudentNoEntity traxStudentNoEntity = traxStudentNoTransformer.transformToEntity(traxStudentNo);

        when(traxStudentNoRepository.findById(traxStudentNo.getStudNo())).thenReturn(Optional.of(traxStudentNoEntity));
        when(traxStudentNoRepository.save(traxStudentNoEntity)).thenReturn(traxStudentNoEntity);

        var result = traxCommonService.saveTraxStudentNo(traxStudentNo);
        assertThat(result).isNotNull();
        assertThat(traxStudentNo.getStudNo()).isEqualTo(result.getStudNo());
        assertThat(traxStudentNo.getStatus()).isEqualTo(result.getStatus());
    }

//    @Test
//    public void testStudentIsNotGraduated() {
//        // Student is graduated or not
//        Boolean result = isStudentGraduated("2004", 0, 0, false);
//
//        assertThat(result).isNotNull();
//        assertThat(result).isFalse();
//    }
//
//    @Test
//    public void testStudent_whenStatus_isNotInGraduatedOrUnGraduated_then_ReturnsNone() {
//        // Student is graduated or not
//        Boolean result = isStudentGraduated("SCCP", 202206, 0, false);
//
//        assertThat(result).isNotNull();
//        assertThat(result).isFalse();
//    }
//
//    @Test
//    public void testStudent_whenUTG_Data_doesNotExist_then_ReturnsUnGraduated() {
//        // Student is graduated or not
//        Boolean result = isStudentGraduated("1996", 202206, 0, false);
//
//        when(this.tswService.existsTranscriptStudentDemog("123456789")).thenReturn(false);
//
//        assertThat(result).isNotNull();
//        assertThat(result).isFalse();
//    }
//
//    @Test
//    public void testStudentForTwoPrograms_whenUTG_Data_doesNotExist_then_ReturnsNO_UTG() {
//        // Student is graduated or not
//        Boolean result = isStudentGraduated("1996", 202206, 202206, false);
//
//        when(this.tswService.existsTranscriptStudentDemog("123456789")).thenReturn(false);
//
//        assertThat(result).isNotNull();
//        assertThat(result).isFalse();
//    }
//
//    @ParameterizedTest
//    @CsvSource({
//            "'1950', 202206, 0",  // graduated 1 program
//            "'SCCP', 0, 202206",  // graduated 1 program
//            "'1950', 0, 202206"   // graduated 2 programs
//    })
//    void testStudentIsGraduated(String gradReqtYear, int gradDate, int sccDate) {
//        // Student is graduated or not
//        Boolean result = isStudentGraduated(gradReqtYear, gradDate, sccDate, true);
//
//        assertThat(result).isNotNull();
//        assertThat(result).isTrue();
//    }
//
//    private Boolean isStudentGraduated(String gradReqtYear, int gradDate, int sccDate, boolean existsUTG) {
//        final String pen = "123456789";
//
//        Object[] cols = new Object[] {
//                gradReqtYear, gradDate, sccDate, Integer.valueOf(0)
//        };
//        List<Object[]> list = new ArrayList<>();
//        list.add(cols);
//
//        when(this.tswService.existsTranscriptStudentDemog(pen)).thenReturn(existsUTG);
//        when(this.traxStudentRepository.getGraduationData(pen)).thenReturn(list);
//        return traxCommonService.isGraduatedStudent(pen);
//    }

}
