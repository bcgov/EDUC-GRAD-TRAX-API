package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.messaging.NatsConnection;
import ca.bc.gov.educ.api.trax.messaging.jetstream.Publisher;
import ca.bc.gov.educ.api.trax.messaging.jetstream.Subscriber;
import ca.bc.gov.educ.api.trax.model.dto.*;
import ca.bc.gov.educ.api.trax.model.entity.DistrictEntity;
import ca.bc.gov.educ.api.trax.model.entity.SchoolEntity;
import ca.bc.gov.educ.api.trax.model.transformer.DistrictTransformer;
import ca.bc.gov.educ.api.trax.repository.DistrictRepository;
import ca.bc.gov.educ.api.trax.repository.SchoolRepository;
import ca.bc.gov.educ.api.trax.repository.TraxSchoolSearchCriteria;
import ca.bc.gov.educ.api.trax.repository.TraxSchoolSearchSpecification;
import ca.bc.gov.educ.api.trax.util.EducGradTraxApiConstants;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.MockitoAnnotations.openMocks;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class SchoolServiceTest {

    @Autowired
    private SchoolService schoolService;

    @MockBean
    private SchoolRepository schoolRepository;

    @MockBean
    private DistrictRepository districtRepository;

    @MockBean
    private CodeService codeService;

    @Autowired
    private EducGradTraxApiConstants constants;

    // NATS
    @MockBean
    private NatsConnection natsConnection;

    @MockBean
    private Publisher publisher;

    @MockBean
    private Subscriber subscriber;

    @MockBean
    private WebClient webClient;

    @Mock WebClient.RequestHeadersSpec requestHeadersMock;
    @Mock WebClient.RequestHeadersUriSpec requestHeadersUriMock;
    @Mock WebClient.RequestBodySpec requestBodyMock;
    @Mock WebClient.RequestBodyUriSpec requestBodyUriMock;
    @Mock WebClient.ResponseSpec responseMock;

    @Autowired
    private DistrictTransformer districtTransformer;

    @Before
    public void setUp() {
        openMocks(this);
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testGetSchoolList() {
        // School data
        final List<SchoolEntity> gradSchoolList = new ArrayList<>();
        final SchoolEntity school1 = new SchoolEntity();
        school1.setMinCode("1234567");
        school1.setSchoolName("Test1 School");
        school1.setCountryCode("CA");
        school1.setProvCode("BC");
        gradSchoolList.add(school1);

        assertThat(StringUtils.isBlank(school1.getMinCode())).isFalse();

        // District data
        DistrictEntity districtEntity = new DistrictEntity();
        districtEntity.setDistrictNumber("123");
        districtEntity.setDistrictName("Test District");

        GradCountry gC = new GradCountry();
        gC.setCountryCode("CA");
        gC.setCountryName("Canada");

        GradProvince gA = new GradProvince();
        gA.setCountryCode("BC");
        gA.setProvName("British Columbia");

        Mockito.when(schoolRepository.findAll()).thenReturn(gradSchoolList);
        Mockito.when(codeService.getSpecificCountryCode("CA")).thenReturn(gC);
        Mockito.when(codeService.getSpecificProvinceCode("BC")).thenReturn(gA);
        Mockito.when(districtRepository.findById("123")).thenReturn(Optional.of(districtEntity));

        District district = districtTransformer.transformToDTO(districtEntity);
        assertThat(district).isNotNull();

        mockCommonSchool("1234567", "Test1 School");
        List<School> results = schoolService.getSchoolList();

        assertThat(results).isNotNull().hasSize(1);
        School responseSchool = results.get(0);
        assertThat(responseSchool.getSchoolName()).isEqualTo(school1.getSchoolName());
        assertThat(responseSchool.getDistrictName()).isEqualTo(districtEntity.getDistrictName());

    }

    @Test
    public void testGetSchoolList_null() {
        // School data
        final List<SchoolEntity> gradSchoolList = new ArrayList<>();
        final SchoolEntity school1 = new SchoolEntity();
        school1.setMinCode("1234567");
        school1.setSchoolName("Test1 School");
        school1.setCountryCode("CA");
        school1.setProvCode("BC");
        gradSchoolList.add(school1);

        assertThat(StringUtils.isBlank(school1.getMinCode())).isFalse();

        // District data
        DistrictEntity districtEntity = new DistrictEntity();
        districtEntity.setDistrictNumber("123");
        districtEntity.setDistrictName("Test District");

        Mockito.when(schoolRepository.findAll()).thenReturn(gradSchoolList);
        Mockito.when(codeService.getSpecificCountryCode("CA")).thenReturn(null);
        Mockito.when(codeService.getSpecificProvinceCode("BC")).thenReturn(null);
        Mockito.when(districtRepository.findById("123")).thenReturn(Optional.of(districtEntity));

        District district = districtTransformer.transformToDTO(districtEntity);
        assertThat(district).isNotNull();

        mockCommonSchool("1234567", "Test1 School");
        List<School> results = schoolService.getSchoolList();

        assertThat(results).isNotNull().hasSize(1);
        School responseSchool = results.get(0);
        assertThat(responseSchool.getSchoolName()).isEqualTo(school1.getSchoolName());
        assertThat(responseSchool.getDistrictName()).isEqualTo(districtEntity.getDistrictName());

    }

    @Test
    public void testGetSchoolDetails() {
        // School
        final SchoolEntity school = new SchoolEntity();
        school.setMinCode("02121000");
        school.setSchoolName("Test School");
        school.setCountryCode("CA");
        school.setProvCode("BC");

        // District
        final DistrictEntity districtEntity = new DistrictEntity();
        districtEntity.setDistrictNumber("021");
        districtEntity.setDistrictName("Test District");
        districtEntity.setCountryCode("CA");
        districtEntity.setProvCode("BC");

        // Country
        final GradCountry country = new GradCountry();
        country.setCountryCode("CA");
        country.setCountryName("Canada");

        // Province
        final GradProvince province = new GradProvince();
        province.setCountryCode("CA");
        province.setProvCode("BC");
        province.setProvName("British Columbia");

        assertThat(StringUtils.isNotBlank(province.getProvCode())).isTrue();

        Mockito.when(schoolRepository.findById("02121000")).thenReturn(Optional.of(school));
        Mockito.when(districtRepository.findById("021")).thenReturn(Optional.of(districtEntity));

        District district = districtTransformer.transformToDTO(districtEntity);
        assertThat(district).isNotNull();

        Mockito.when(codeService.getSpecificCountryCode(country.getCountryCode())).thenReturn(country);
        Mockito.when(codeService.getSpecificProvinceCode(province.getProvCode())).thenReturn(province);

        mockCommonSchool("02121000", "Test School");
        var result = schoolService.getSchoolDetails("02121000", "accessToken");

        assertThat(result).isNotNull();
        assertThat(result.getMinCode()).isEqualTo("02121000");
        assertThat(result.getSchoolName()).isEqualToIgnoringCase("Test School");
    }

    @Test
    public void testGetSchoolDetails_empty() {
        // School
        final SchoolEntity school = new SchoolEntity();
        school.setMinCode("02121000");
        school.setSchoolName("Test School");
        school.setCountryCode("CA");
        school.setProvCode("BC");

        // District
        final DistrictEntity district = new DistrictEntity();
        district.setDistrictNumber("021");
        district.setDistrictName("Test District");
        district.setCountryCode("CA");
        district.setProvCode("BC");

        // Country
        final GradCountry country = new GradCountry();
        country.setCountryCode("CA");
        country.setCountryName("Canada");

        // Province
        final GradProvince province = new GradProvince();
        province.setCountryCode("CA");
        province.setProvCode("BC");
        province.setProvName("British Columbia");

        Mockito.when(schoolRepository.findById("02121000")).thenReturn(Optional.empty());
        Mockito.when(districtRepository.findById("021")).thenReturn(Optional.of(district));

        Mockito.when(codeService.getSpecificCountryCode(country.getCountryCode())).thenReturn(country);
        Mockito.when(codeService.getSpecificProvinceCode(province.getProvCode())).thenReturn(province);

        mockCommonSchool("02121000", null);
        var result = schoolService.getSchoolDetails("02121000", "accessToken");

        assertThat(result).isNull();
    }

    @Test
    public void testGetSchoolDetails_not_empty_null() {
        // School
        final SchoolEntity school = new SchoolEntity();
        school.setMinCode("02121000");
        school.setSchoolName("Test School");
        school.setCountryCode("CA");
        school.setProvCode("BC");

        // District
        final DistrictEntity district = new DistrictEntity();
        district.setDistrictNumber("021");
        district.setDistrictName("Test District");
        district.setCountryCode("CA");
        district.setProvCode("BC");

        // Country
        final GradCountry country = new GradCountry();
        country.setCountryCode("CA");
        country.setCountryName("Canada");

        // Province
        final GradProvince province = new GradProvince();
        province.setCountryCode("CA");
        province.setProvCode("BC");
        province.setProvName("British Columbia");

        Mockito.when(schoolRepository.findById("02121000")).thenReturn(Optional.of(school));
        Mockito.when(districtRepository.findById("021")).thenReturn(null);

        Mockito.when(codeService.getSpecificCountryCode(country.getCountryCode())).thenReturn(null);
        Mockito.when(codeService.getSpecificProvinceCode(province.getProvCode())).thenReturn(null);

        mockCommonSchool("02121000", null);

        var result = schoolService.getSchoolDetails("1234567", "accessToken");

        assertThat(result).isNull();
    }

    @Test
    public void testGetSchoolsByParams() {
        // School
        final SchoolEntity school = new SchoolEntity();
        school.setMinCode("02121000");
        school.setSchoolName("THE GATEWAY COMMUNITY LEARNING CENTRE");
        school.setCountryCode("CA");
        school.setProvCode("BC");

        // District
        final DistrictEntity district = new DistrictEntity();
        district.setDistrictNumber("021");
        district.setDistrictName("Test District");
        district.setCountryCode("CA");
        district.setProvCode("BC");

        // Country
        final GradCountry country = new GradCountry();
        country.setCountryCode("CA");
        country.setCountryName("Canada");

        // Province
        final GradProvince province = new GradProvince();
        province.setCountryCode("CA");
        province.setProvCode("BC");
        province.setProvName("British Columbia");

        TraxSchoolSearchCriteria searchCriteria = TraxSchoolSearchCriteria.builder()
                .district(null)
                .schoolName(null)
                .minCode(school.getMinCode())
                .build();
        Specification<SchoolEntity> spec = new TraxSchoolSearchSpecification(searchCriteria);

        Mockito.when(schoolRepository.findAll(Specification.where(spec))).thenReturn(List.of(school));
        Mockito.when(districtRepository.findById("021")).thenReturn(Optional.of(district));

        Mockito.when(codeService.getSpecificCountryCode(country.getCountryCode())).thenReturn(country);
        Mockito.when(codeService.getSpecificProvinceCode(province.getProvCode())).thenReturn(province);

        mockCommonSchool("02121000", "THE GATEWAY COMMUNITY LEARNING CENTRE");

        var result = schoolService.getSchoolsByParams(null, searchCriteria.getMinCode(), null, null,"accessToken");
        assertThat(result).isNotNull();

    }
    @Test
    public void testGetSchoolsByParams_params() {
        // School
        final SchoolEntity school = new SchoolEntity();
        school.setMinCode("02121000");
        school.setSchoolName("THE GATEWAY COMMUNITY LEARNING CENTRE");
        school.setCountryCode("CA");
        school.setProvCode("BC");

        // District
        final DistrictEntity district = new DistrictEntity();
        district.setDistrictNumber("021");
        district.setDistrictName("Test District");
        district.setCountryCode("CA");
        district.setProvCode("BC");

        // Country
        final GradCountry country = new GradCountry();
        country.setCountryCode("CA");
        country.setCountryName("Canada");

        // Province
        final GradProvince province = new GradProvince();
        province.setCountryCode("CA");
        province.setProvCode("BC");
        province.setProvName("British Columbia");

        TraxSchoolSearchCriteria searchCriteria = TraxSchoolSearchCriteria.builder()
                .district(null)
                .schoolName(null)
                .minCode(school.getMinCode())
                .build();
        Specification<SchoolEntity> spec = new TraxSchoolSearchSpecification(searchCriteria);

        Mockito.when(schoolRepository.findAll(Mockito.any(Specification.class))).thenReturn(List.of(school));
        Mockito.when(districtRepository.findById("021")).thenReturn(Optional.of(district));

        Mockito.when(codeService.getSpecificCountryCode(country.getCountryCode())).thenReturn(country);
        Mockito.when(codeService.getSpecificProvinceCode(province.getProvCode())).thenReturn(province);

        mockCommonSchool("02121000", "THE GATEWAY COMMUNITY LEARNING CENTRE");

        var result = schoolService.getSchoolsByParams("TH*", null, "02*", "43","accessToken");
        assertThat(result).isNotNull();

    }

    @Test
    public void testExistsSchool() {
        Mockito.when(schoolRepository.countTabSchools("02121000")).thenReturn(1L);
        mockCommonSchool("02121000", null);
        var result = schoolService.existsSchool("02121000");
        assertThat(result).isTrue();
    }

    @Test
    public void testNotExistsSchool() {
        Mockito.when(schoolRepository.countTabSchools("02121000")).thenReturn(0L);
        mockCommonSchool("02121000", null);
        var result = schoolService.existsSchool("02121000");
        assertThat(result).isFalse();
    }

    @Test
    public void testCommonSchool() {
        mockCommonSchool("02121000", "THE GATEWAY COMMUNITY LEARNING CENTRE");
        var result = schoolService.getCommonSchool("accessToken", "02121000");
        assertThat(result).isNotNull();
    }

    @Test
    public void testCommonSchool_mincode_null() {
        var result = schoolService.getCommonSchool("accessToken", null);
        assertThat(result).isNull();
    }

    @Test
    public void testCommonSchoolIsNull() {
        mockCommonSchool("02121000", "THE GATEWAY COMMUNITY LEARNING CENTRE");
        var result = schoolService.getCommonSchool("accessToken", "12345678");
        assertThat(result).isNull();
    }

    public void mockCommonSchool(String minCode, String schoolName) {
        CommonSchool commonSchool = new CommonSchool();
        commonSchool.setSchlNo(minCode);
        commonSchool.setSchoolName(schoolName);
        commonSchool.setSchoolCategoryCode("02");

        Mockito.when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        Mockito.when(this.requestHeadersUriMock.uri(String.format(constants.getSchoolByMincodeSchoolApiUrl(), minCode))).thenReturn(this.requestHeadersMock);
        Mockito.when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        Mockito.when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        Mockito.when(this.responseMock.bodyToMono(CommonSchool.class)).thenReturn(Mono.just(commonSchool));
    }
}
