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
import ca.bc.gov.educ.api.trax.util.CommonSchoolCache;
import ca.bc.gov.educ.api.trax.util.EducGradTraxApiConstants;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.MockitoAnnotations.openMocks;

@SpringBootTest
@ActiveProfiles("test")
class SchoolServiceTest {

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
    @Qualifier("default")
    private WebClient webClient;

    @MockBean
    private CommonSchoolCache commonSchoolCache;
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


    @Mock WebClient.RequestHeadersSpec requestHeadersMock;
    @Mock WebClient.RequestHeadersUriSpec requestHeadersUriMock;
    @Mock WebClient.RequestBodySpec requestBodyMock;
    @Mock WebClient.RequestBodyUriSpec requestBodyUriMock;
    @Mock WebClient.ResponseSpec responseMock;

    @Autowired
    private DistrictTransformer districtTransformer;

    @BeforeEach
    void setUp() {
        openMocks(this);
    }


    /**
     * NOTE: This test disabled as 404 NOT FOUND handled by front end as new mulls are now handled as NOT FOUND
     * @ExtendWith(OutputCaptureExtension.class)
    @Test
    void testGetCommonSchool_GivenAPICalloutNotFoundError_ExpectNullAndLoggedError(CapturedOutput output){
        final String MESSAGE = "Common School not exists for Ministry Code";
        WebClientResponseException webClientException  = new WebClientResponseException("Not Found", 404, "404 NOT FOUND", null, null, null);
        // mock webclient
        Mockito.when(this.traxWebClient.get()).thenThrow(webClientException);
        CommonSchool commonSchool = schoolService.getCommonSchool(null);
        Assertions.assertNull(commonSchool);
        // assert logging
        Assertions.assertTrue(output.getOut().contains(MESSAGE));
    }**/

/**
 * NOTE: This test disabled as 404 NOT FOUND handled by front end as new mulls are now handled as NOT FOUND@ExtendWith(OutputCaptureExtension.class)
    @Test
    void testGetCommonSchool_GivenAPICalloutServiceUnavailableError_ExpectNullAndLoggedError(CapturedOutput output){
        final String MESSAGE = "Response error while calling school-api";
        WebClientResponseException webClientException  = new WebClientResponseException("Service Unavailable", 503, "503 SERVICE UNAVAILABLE", null, null, null);
        // mock webclient
        Mockito.when(this.webClient.get()).thenThrow(webClientException);
        CommonSchool commonSchool = schoolService.getCommonSchool(null);
        Assertions.assertNull(commonSchool);
        // assert logging
        Assertions.assertTrue(output.getOut().contains(MESSAGE));
    }
 **/

    @Test
    void testGetSchoolList() {
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
    void testGetSchoolList_null() {
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
    void testGetSchoolDetails() {
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
    void testGetSchoolDetails_empty() {
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
    void testGetSchoolDetails_not_empty_null() {
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
    void testGetSchoolsByParams() {
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

        var result = schoolService.getSchoolsByParams(null, searchCriteria.getMinCode(), null, "accessToken");
        assertThat(result).isNotNull();

    }

    @Test
    void testGetSchoolsBySchoolCategoryCode() {
        mockCommonSchools();
        var result = schoolService.getSchoolsBySchoolCategory("02");
        assertThat(result).isNotNull().isNotEmpty();
        result = schoolService.getSchoolsBySchoolCategory("01");
        assertThat(result).isNotNull().isEmpty();
        result = schoolService.getSchoolsBySchoolCategory("");
        assertThat(result).isNotNull().isNotEmpty();

    }

    @Test
    void testGetSchoolsByParams_params() {
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

        var result = schoolService.getSchoolsByParams("TH*", null, "02*", "accessToken");
        assertThat(result).isNotNull();

    }

    @Test
    void testExistsSchool() {
        Mockito.when(schoolRepository.countTabSchools("02121000")).thenReturn(1L);
        mockCommonSchool("02121000", null);
        var result = schoolService.existsSchool("02121000");
        assertThat(result).isTrue();
    }

    @Test
    void testNotExistsSchool() {
        Mockito.when(schoolRepository.countTabSchools("02121000")).thenReturn(0L);
        mockCommonSchool("02121000", null);
        var result = schoolService.existsSchool("02121000");
        assertThat(result).isFalse();
    }

    @Test
    void testCommonSchool() {
        mockCommonSchool("02121000", "THE GATEWAY COMMUNITY LEARNING CENTRE");
        var result = schoolService.getCommonSchool("02121000");
        assertThat(result).isNotNull();
        List<CommonSchool> commonSchools = schoolService.getCommonSchools();
        assertThat(commonSchools).isNotNull().isNotEmpty();
    }

    @Test
    void testCommonSchool_mincode_null() {
        var result = schoolService.getCommonSchool(null);
        assertThat(result).isNull();
    }

    @Test
    void testCommonSchoolIsNull() {
        mockCommonSchool("02121000", "THE GATEWAY COMMUNITY LEARNING CENTRE");
        var result = schoolService.getCommonSchool("12345678");
        assertThat(result).isNull();
    }

    void mockCommonSchool(String minCode, String schoolName) {
        CommonSchool commonSchool = new CommonSchool();
        commonSchool.setSchlNo(minCode);
        commonSchool.setSchoolName(schoolName);
        commonSchool.setSchoolCategoryCode("02");

        Mockito.when(this.commonSchoolCache.getAllCommonSchools()).thenReturn(List.of(commonSchool));
        Mockito.when(this.commonSchoolCache.getSchoolByMincode(minCode)).thenReturn(commonSchool);

        Mockito.when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        Mockito.when(this.requestHeadersUriMock.uri(String.format(constants.getSchoolByMincodeSchoolApiUrl(), minCode))).thenReturn(this.requestHeadersMock);
        Mockito.when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        Mockito.when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        Mockito.when(this.responseMock.bodyToMono(CommonSchool.class)).thenReturn(Mono.just(commonSchool));

        Mockito.when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        Mockito.when(this.requestHeadersUriMock.uri(constants.getAllSchoolSchoolApiUrl())).thenReturn(this.requestHeadersMock);
        Mockito.when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        Mockito.when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        Mockito.when(this.responseMock.bodyToMono(new ParameterizedTypeReference<List<CommonSchool>>() {
        })).thenReturn(Mono.just(List.of(commonSchool)));
    }

    void mockCommonSchools() {
        CommonSchool commonSchool = new CommonSchool();
        commonSchool.setSchlNo("1234567");
        commonSchool.setSchoolName("Test School");
        commonSchool.setSchoolCategoryCode("02");

        Mockito.when(this.commonSchoolCache.getAllCommonSchools()).thenReturn(List.of(commonSchool));

        Mockito.when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        Mockito.when(this.requestHeadersUriMock.uri(constants.getAllSchoolSchoolApiUrl())).thenReturn(this.requestHeadersMock);
        Mockito.when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        Mockito.when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        Mockito.when(this.responseMock.bodyToMono(new ParameterizedTypeReference<List<CommonSchool>>() {
        })).thenReturn(Mono.just(List.of(commonSchool)));
    }
}
