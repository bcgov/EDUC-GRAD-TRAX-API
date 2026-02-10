package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.messaging.NatsConnection;
import ca.bc.gov.educ.api.trax.messaging.jetstream.Publisher;
import ca.bc.gov.educ.api.trax.messaging.jetstream.Subscriber;
import ca.bc.gov.educ.api.trax.model.dto.*;
import ca.bc.gov.educ.api.trax.model.dto.institute.School;
import ca.bc.gov.educ.api.trax.model.entity.GradCourseEntity;
import ca.bc.gov.educ.api.trax.model.entity.GradCourseKey;
import ca.bc.gov.educ.api.trax.model.entity.TraxStudentNoEntity;
import ca.bc.gov.educ.api.trax.model.transformer.GradCourseTransformer;
import ca.bc.gov.educ.api.trax.model.transformer.TraxStudentNoTransformer;
import ca.bc.gov.educ.api.trax.repository.*;
import ca.bc.gov.educ.api.trax.service.institute.CommonService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
import redis.clients.jedis.JedisCluster;

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
    CommonService commonService;

    @MockBean
    SchoolService schoolService;

    @MockBean
    @Qualifier("instituteSchoolService")
    ca.bc.gov.educ.api.trax.service.institute.SchoolService schoolServiceV2;

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
    public void testGetStudentMasterDataFromTrax() {
        Object[] obj = new Object[] {
                "123456789 ", "12345678", "12345678", "AD", Character.valueOf('A'),Character.valueOf('A'), "1950", Integer.valueOf(0),
                Integer.valueOf(0), null, null, null, null, null, null, null, null, null, null, Character.valueOf('C'), Character.valueOf('N'), null
        };
        List<Object[]> results = new ArrayList<>();
        results.add(obj);

        String pen = (String) obj[0];
        Character status = (Character)obj[4];

        School school = new School();
        school.setMincode("12345678");
        school.setSchoolId(UUID.randomUUID().toString());

        Object[] cols = new Object[] {
                "1950", Integer.valueOf(0), Integer.valueOf(0), null
        };
        List<Object[]> list = new ArrayList<>();
        list.add(cols);

        when(this.traxStudentRepository.loadTraxStudent(pen)).thenReturn(results);
        when(this.commonService.getSchoolIdFromRedisCache("12345678")).thenReturn(UUID.randomUUID());

        var result = traxCommonService.getStudentMasterDataFromTrax(pen);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        ConvGradStudent responseObject = result.get(0);
        assertThat(responseObject.getPen()).isEqualTo(pen);
        assertThat(responseObject.getStudentStatus()).isEqualTo(status.toString());
    }

    @Test
    public void testGetStudentDemographicsDataFromTrax() {
        Object[] obj = new Object[] {
                "123456789 ", "Test", "QA", "", Character.valueOf('A'),Character.valueOf('A'), "12345678", "12", "V4N3Y2", Character.valueOf('M'), "19800111",  BigDecimal.valueOf(202005), null, "            "
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
