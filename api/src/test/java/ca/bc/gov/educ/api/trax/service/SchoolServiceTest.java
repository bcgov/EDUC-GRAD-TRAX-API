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
import ca.bc.gov.educ.api.trax.util.EducGradTraxApiConstants;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
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
        gradSchoolList.add(school1);

        assertThat(StringUtils.isBlank(school1.getMinCode())).isFalse();

        // District data
        final DistrictEntity districtEntity = new DistrictEntity();
        districtEntity.setDistrictNumber("123");
        districtEntity.setDistrictName("Test District");

        when(schoolRepository.findAll()).thenReturn(gradSchoolList);
        when(districtRepository.findById("123")).thenReturn(Optional.of(districtEntity));

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
        school.setMinCode("1234567");
        school.setSchoolName("Test School");
        school.setCountryCode("CA");
        school.setProvCode("BC");

        // District
        final DistrictEntity districtEntity = new DistrictEntity();
        districtEntity.setDistrictNumber("123");
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

        when(schoolRepository.findById("1234567")).thenReturn(Optional.of(school));
        when(districtRepository.findById("123")).thenReturn(Optional.of(districtEntity));

        District district = districtTransformer.transformToDTO(districtEntity);
        assertThat(district).isNotNull();

        when(codeService.getSpecificCountryCode(country.getCountryCode())).thenReturn(country);
        when(codeService.getSpecificProvinceCode(province.getProvCode())).thenReturn(province);

        mockCommonSchool("1234567", "Test School");
        var result = schoolService.getSchoolDetails("1234567", "accessToken");

        assertThat(result).isNotNull();
        assertThat(result.getMinCode()).isEqualTo("1234567");
        assertThat(result.getSchoolName()).isEqualToIgnoringCase("Test School");
    }

    @Test
    public void testGetSchoolDetails_empty() {
        // School
        final SchoolEntity school = new SchoolEntity();
        school.setMinCode("1234567");
        school.setSchoolName("Test School");
        school.setCountryCode("CA");
        school.setProvCode("BC");

        // District
        final DistrictEntity district = new DistrictEntity();
        district.setDistrictNumber("123");
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

        when(schoolRepository.findById("1234567")).thenReturn(Optional.empty());
        when(districtRepository.findById("123")).thenReturn(Optional.of(district));

        when(codeService.getSpecificCountryCode(country.getCountryCode())).thenReturn(country);
        when(codeService.getSpecificProvinceCode(province.getProvCode())).thenReturn(province);

        mockCommonSchool("1234567", null);
        var result = schoolService.getSchoolDetails("1234567", "accessToken");

        assertThat(result).isNull();
    }

    @Test
    public void testGetSchoolDetails_empty_null() {
        // School
        final SchoolEntity school = new SchoolEntity();
        school.setMinCode("1234567");
        school.setSchoolName("Test School");
        school.setCountryCode("CA");
        school.setProvCode("BC");

        // District
        final DistrictEntity district = new DistrictEntity();
        district.setDistrictNumber("123");
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

        when(schoolRepository.findById("1234567")).thenReturn(Optional.empty());
        when(districtRepository.findById("123")).thenReturn(null);

        when(codeService.getSpecificCountryCode(country.getCountryCode())).thenReturn(null);
        when(codeService.getSpecificProvinceCode(province.getProvCode())).thenReturn(null);

        mockCommonSchool("1234567", null);

        var result = schoolService.getSchoolDetails("1234567", "accessToken");

        assertThat(result).isNull();
    }

    @Test
    public void testGetSchoolsByParams() {
        // School
        final SchoolEntity school = new SchoolEntity();
        school.setMinCode("1234567");
        school.setSchoolName("TEST SCHOOL");
        school.setCountryCode("CA");
        school.setProvCode("BC");

        // District
        final DistrictEntity district = new DistrictEntity();
        district.setDistrictNumber("123");
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

        when(schoolRepository.findSchools("Test School".toUpperCase(Locale.ROOT),"1234567", null)).thenReturn(List.of(school));
        when(districtRepository.findById("123")).thenReturn(Optional.of(district));

        when(codeService.getSpecificCountryCode(country.getCountryCode())).thenReturn(country);
        when(codeService.getSpecificProvinceCode(province.getProvCode())).thenReturn(province);

        mockCommonSchool("1234567", "Test School");

        var result = schoolService.getSchoolsByParams("Test School", "1234567", null, null,"accessToken");
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getMinCode()).isEqualTo("1234567");
        assertThat(result.get(0).getSchoolName()).isEqualToIgnoringCase("TEST SCHOOL");
    }

    @Test
    public void testExistsSchool() {
        when(schoolRepository.countTabSchools("1234567")).thenReturn(1L);
        mockCommonSchool("1234567", null);
        var result = schoolService.existsSchool("1234567", "accessToken");
        assertThat(result).isTrue();
    }

    private void mockCommonSchool(String minCode, String schoolName) {
        CommonSchool commonSchool = new CommonSchool();
        commonSchool.setSchlNo(minCode);
        commonSchool.setSchoolName(schoolName);
        commonSchool.setSchoolCategoryCode("02");

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getSchoolByMincodeSchoolApiUrl(), minCode))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(CommonSchool.class)).thenReturn(Mono.just(commonSchool));

    }
}
