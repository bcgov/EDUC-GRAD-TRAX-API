package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.messaging.NatsConnection;
import ca.bc.gov.educ.api.trax.messaging.jetstream.Publisher;
import ca.bc.gov.educ.api.trax.messaging.jetstream.Subscriber;
import ca.bc.gov.educ.api.trax.model.dto.CommonSchool;
import ca.bc.gov.educ.api.trax.model.dto.District;
import ca.bc.gov.educ.api.trax.model.entity.DistrictEntity;
import ca.bc.gov.educ.api.trax.repository.DistrictRepository;
import ca.bc.gov.educ.api.trax.util.EducGradTraxApiConstants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class DistrictServiceTest {

    @Autowired
    private DistrictService districtService;

    @MockBean
    private DistrictRepository districtRepository;

    @Mock
    WebClient.RequestHeadersSpec requestHeadersMock;
    @Mock WebClient.RequestHeadersUriSpec requestHeadersUriMock;
    @Mock WebClient.RequestBodySpec requestBodyMock;
    @Mock WebClient.RequestBodyUriSpec requestBodyUriMock;
    @Mock WebClient.ResponseSpec responseMock;

    @MockBean
    private WebClient webClient;

    @Autowired
    private EducGradTraxApiConstants constants;

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
    public void testGetDistrictDetails() {
        // District
        final DistrictEntity district = new DistrictEntity();
        district.setDistrictNumber("123");
        district.setDistrictName("Test District");

        when(districtRepository.findById("123")).thenReturn(Optional.of(district));
        Optional<DistrictEntity> entity = districtRepository.findById("123");
        assertThat(entity).isPresent();

        var result = districtService.getDistrictDetails("123");
        assertThat(result).isNotNull();
        assertThat(result.getDistrictNumber()).isEqualTo("123");
        assertThat(result.getDistrictName()).isEqualTo("Test District");
    }

    @Test
    public void testGetDistrictBySchoolCategory() {
        // District
        final DistrictEntity district = new DistrictEntity();
        district.setDistrictNumber("123");
        district.setDistrictName("Test District");

        when(districtRepository.findByActiveFlag("Y")).thenReturn(List.of(district));
        List<DistrictEntity> entitys = districtRepository.findByActiveFlag("Y");
        assertThat(entitys).isNotEmpty();

        when(districtRepository.findByDistrictNumberAndActiveFlag("123","Y")).thenReturn(Optional.of(district));
        Optional<DistrictEntity> entity = districtRepository.findByDistrictNumberAndActiveFlag("123","Y");
        assertThat(entity).isPresent();

        CommonSchool commonSchool = new CommonSchool();
        commonSchool.setDistNo("123");
        commonSchool.setSchlNo("456");
        commonSchool.setSchoolName("Test School");
        commonSchool.setSchoolCategoryCode("02");

        Mockito.when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        Mockito.when(this.requestHeadersUriMock.uri(constants.getAllSchoolSchoolApiUrl())).thenReturn(this.requestHeadersMock);
        Mockito.when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        Mockito.when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(new ParameterizedTypeReference<List<CommonSchool>>() {
        })).thenReturn(Mono.just(List.of(commonSchool)));

        when(districtRepository.findByDistrictNumberAndActiveFlag("123","Y")).thenReturn(Optional.of(district));

        List<District> districts = districtService.getDistrictBySchoolCategory("02");
        assertThat(districts).isNotEmpty();

        districts = districtService.getDistrictBySchoolCategory(null);
        assertThat(districts).isNotEmpty();
    }

    @Test
    public void testGetDistrictDetailsNull() {
        when(districtRepository.findById("123")).thenReturn(Optional.empty());
        Optional<DistrictEntity> entity = districtRepository.findById("123");
        assertThat(entity).isNotPresent();
        var result = districtService.getDistrictDetails("123");
        assertThat(result).isNull();
    }
}
