package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.messaging.NatsConnection;
import ca.bc.gov.educ.api.trax.messaging.jetstream.Publisher;
import ca.bc.gov.educ.api.trax.messaging.jetstream.Subscriber;
import ca.bc.gov.educ.api.trax.model.dto.*;
import ca.bc.gov.educ.api.trax.model.entity.GradCourseEntity;
import ca.bc.gov.educ.api.trax.model.entity.GradCourseKey;
import ca.bc.gov.educ.api.trax.model.entity.TranscriptStudentDemogEntity;
import ca.bc.gov.educ.api.trax.model.entity.TraxStudentNoEntity;
import ca.bc.gov.educ.api.trax.model.transformer.GradCourseTransformer;
import ca.bc.gov.educ.api.trax.model.transformer.TraxStudentNoTransformer;
import ca.bc.gov.educ.api.trax.repository.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
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
    TswService tswService;

    // NATS
    @MockBean
    private NatsConnection natsConnection;

    @MockBean
    private Publisher publisher;

    @MockBean
    private Subscriber subscriber;

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
                "123456789", "12345678", "12345678", "12", Character.valueOf('A'),Character.valueOf('A'), "2020", BigDecimal.ZERO,
                BigDecimal.ZERO, null, null, null, null, null, null, null, null, null, null, Character.valueOf('C'), Character.valueOf('N')
        };
        List<Object[]> results = new ArrayList<>();
        results.add(obj);

        String pen = (String) obj[0];
        Character status = (Character)obj[4];

        when(this.traxStudentRepository.loadTraxStudent(pen)).thenReturn(results);

        var result = traxCommonService.getStudentMasterDataFromTrax(pen);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        ConvGradStudent responseObject = result.get(0);
        assertThat(responseObject.getPen()).isEqualTo(pen);
        assertThat(responseObject.getStudentStatus()).isEqualTo(status.toString());
    }

    @Test
    public void testGetGraduatedStudentMasterDataFromTrax() {
        Object[] obj = new Object[] {
                "123456789", "1234567", "1234567", "12", Character.valueOf('A'),Character.valueOf('A'), "2020", "202201",
                BigDecimal.ZERO, BigDecimal.ZERO, null, null, null, null, null, "S", null, "E", null, Character.valueOf('C'), Character.valueOf('N')
        };
        List<Object[]> results = new ArrayList<>();
        results.add(obj);

        final String pen = (String) obj[0];
        final String mincode = (String) obj[1];
        Character status = (Character)obj[4];

        // TSW Demographics
        TranscriptStudentDemog transcriptStudentDemog = new TranscriptStudentDemog();
        transcriptStudentDemog.setStudNo(pen);
        transcriptStudentDemog.setFirstName("Test");
        transcriptStudentDemog.setLastName("QA");
        transcriptStudentDemog.setMincode(mincode);
        transcriptStudentDemog.setSchoolName("Test School");
        transcriptStudentDemog.setGradDate(202201L);

        // TSW Courses
        TranscriptStudentCourse tswCourse1 = new TranscriptStudentCourse();
        tswCourse1.setStudNo(pen);
        tswCourse1.setReportType("1");
        tswCourse1.setCourseCode("Generic");
        tswCourse1.setCourseName("Generic Course Name");
        tswCourse1.setCourseLevel("12");
        tswCourse1.setFinalPercentage("91.00");
        tswCourse1.setFinalLG("A");
        tswCourse1.setCourseSession("202206");
        tswCourse1.setNumberOfCredits("4");
        tswCourse1.setUsedForGrad("4");
        tswCourse1.setFoundationReq("10");
        tswCourse1.setUpdateDate(20220601L);

        TranscriptStudentCourse tswCourse2 = new TranscriptStudentCourse();
        tswCourse2.setStudNo(pen);
        tswCourse2.setReportType("2");
        tswCourse2.setCourseCode("TestCourse");
        tswCourse2.setCourseName("Test Course Name");
        tswCourse2.setCourseLevel("12");
        tswCourse2.setFinalPercentage("92.00");
        tswCourse2.setFinalLG("A");
        tswCourse2.setCourseSession("202206");
        tswCourse2.setNumberOfCredits("4");
        tswCourse2.setUsedForGrad("4");
        tswCourse2.setFoundationReq("11");
        tswCourse2.setSpecialCase("E");
        tswCourse2.setUpdateDate(20220601L);

        TranscriptStudentCourse tswCourse3 = new TranscriptStudentCourse();
        tswCourse2.setStudNo(pen);
        tswCourse2.setReportType("2");
        tswCourse2.setCourseCode("TestCourse2");
        tswCourse2.setCourseName("Test Course2 Name");
        tswCourse2.setCourseLevel("12");
        tswCourse2.setFinalPercentage("XMT");
        tswCourse2.setFinalLG("A");
        tswCourse2.setCourseSession("202206");
        tswCourse2.setNumberOfCredits("4");
        tswCourse2.setUsedForGrad("4");
        tswCourse2.setFoundationReq("11");
        tswCourse2.setUpdateDate(20220601L);

        TranscriptStudentCourse tswAssessment = new TranscriptStudentCourse();
        tswAssessment.setStudNo(pen);
        tswAssessment.setReportType("3");
        tswAssessment.setCourseCode("TestAssmt");
        tswAssessment.setCourseName("Test Assessment Name");
        tswAssessment.setCourseLevel("12");
        tswAssessment.setFinalPercentage("XMT");
        tswAssessment.setCourseSession("202206");
        tswAssessment.setFoundationReq("15");
        tswAssessment.setUpdateDate(new Date(System.currentTimeMillis() - 100000L).getTime());

        when(this.traxStudentRepository.loadTraxGraduatedStudent(pen)).thenReturn(results);
        when(this.traxStudentRepository.countGradDateByPen(pen)).thenReturn(Integer.valueOf(1));
        when(this.tswService.getTranscriptStudentDemog(pen)).thenReturn(transcriptStudentDemog);
        when(this.tswService.getTranscriptStudentCourses(pen)).thenReturn(Arrays.asList(tswCourse1, tswCourse2, tswCourse3, tswAssessment));

        var result = traxCommonService.getStudentMasterDataFromTrax(pen);

        assertThat(result).hasSize(1);
        ConvGradStudent responseObject = result.get(0);
        assertThat(responseObject.getPen()).isEqualTo(pen);
        assertThat(responseObject.getStudentStatus()).isEqualTo(status.toString());
        assertThat(responseObject.isGraduated()).isTrue();
        assertThat(responseObject.getTranscriptStudentDemog()).isNotNull();
        assertThat(responseObject.getTranscriptStudentCourses()).hasSize(4);
    }

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

    @Test
    public void testStudentIsNotGraduated() {
        // Student is graduated or not
        Boolean result = isStudentGraduated(0, 0);

        assertThat(result).isNotNull();
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @CsvSource({
            "1, 0",
            "0, 1",
            "1, 1"
    })
    void testStudentIsGraduated(int gradDateCount, int sccDateCount) {
        // Student is graduated or not
        Boolean result = isStudentGraduated(gradDateCount, sccDateCount);

        assertThat(result).isNotNull();
        assertThat(result).isTrue();
    }

    private Boolean isStudentGraduated(int gradDateCount, int sccDateCount) {
        final TranscriptStudentDemogEntity transcriptStudentDemogEntity = new TranscriptStudentDemogEntity();
        transcriptStudentDemogEntity.setStudNo("123456789");
        transcriptStudentDemogEntity.setFirstName("Test");
        transcriptStudentDemogEntity.setLastName("QA");
        transcriptStudentDemogEntity.setMincode("7654321");
        transcriptStudentDemogEntity.setSchoolName("Test2 School");
        transcriptStudentDemogEntity.setGradDate(20201031L);

        when(traxStudentRepository.countGradDateByPen("123456789")).thenReturn(gradDateCount);
        when(traxStudentRepository.countSccDateByPen("123456789")).thenReturn(sccDateCount);

       return traxCommonService.isGraduatedStudent(transcriptStudentDemogEntity.getStudNo());
    }

}
