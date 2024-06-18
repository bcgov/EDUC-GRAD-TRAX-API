package ca.bc.gov.educ.api.trax.service.institute;

import ca.bc.gov.educ.api.trax.messaging.NatsConnection;
import ca.bc.gov.educ.api.trax.messaging.jetstream.Publisher;
import ca.bc.gov.educ.api.trax.messaging.jetstream.Subscriber;
import ca.bc.gov.educ.api.trax.model.dto.ResponseObj;
import ca.bc.gov.educ.api.trax.model.dto.institute.District;
import ca.bc.gov.educ.api.trax.model.dto.institute.DistrictContact;
import ca.bc.gov.educ.api.trax.model.dto.institute.SchoolCategoryCode;
import ca.bc.gov.educ.api.trax.model.entity.institute.DistrictContactEntity;
import ca.bc.gov.educ.api.trax.model.entity.institute.DistrictEntity;
import ca.bc.gov.educ.api.trax.model.entity.institute.SchoolCategoryCodeEntity;
import ca.bc.gov.educ.api.trax.model.entity.institute.SchoolFundingGroupCodeEntity;
import ca.bc.gov.educ.api.trax.model.transformer.institute.DistrictTransformer;
import ca.bc.gov.educ.api.trax.repository.GradCountryRepository;
import ca.bc.gov.educ.api.trax.repository.GradProvinceRepository;
import ca.bc.gov.educ.api.trax.repository.redis.DistrictRedisRepository;
import ca.bc.gov.educ.api.trax.util.EducGradTraxApiConstants;
import ca.bc.gov.educ.api.trax.util.RestUtils;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;


@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"unchecked","rawtypes"})
public class InstituteDistrictServiceTest {

	@Autowired
	private EducGradTraxApiConstants constants;
	@Autowired
	private DistrictService districtService;
	@MockBean
	private DistrictRedisRepository districtRedisRepository;

	@MockBean
	@Qualifier("default")
	WebClient webClientMock;
	@Mock
	private WebClient.RequestHeadersSpec requestHeadersSpecMock;
	@Mock
	private WebClient.RequestHeadersUriSpec requestHeadersUriSpecMock;
	@Mock
	private WebClient.ResponseSpec responseSpecMock;
	@Mock
	private HttpHeaders httpHeadersMock;
	@Mock
	private ResponseObj responseObjectMock;
	@Mock
	private Mono<List<DistrictEntity>> districtEntitiesMock;
	@Mock
	private List<District> districtsMock;
	@MockBean
	private DistrictTransformer districtTransformerMock;

	// NATS
	@MockBean
	private NatsConnection natsConnection;

	@MockBean
	private Publisher publisher;

	@MockBean
	private Subscriber subscriber;
	@MockBean
	private RestUtils restUtils;

	@TestConfiguration
	static class TestConfigInstitute {
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

	@Test
	public void whenGetDistrictsFromInstituteApi_returnsListOfDistricts() {
		List<DistrictEntity> districts = new ArrayList<>();
		DistrictEntity district = new DistrictEntity();

		district.setDistrictId("ID");
		district.setDistrictNumber("1234");
		district.setDistrictStatusCode("SC");
		district.setDistrictRegionCode("RC");
		district.setContacts(Arrays.asList(new DistrictContactEntity(), new DistrictContactEntity()));

		districts.add(district);

		when(this.restUtils.getTokenResponseObject(anyString(), anyString()))
				.thenReturn(responseObjectMock);
		when(this.responseObjectMock.getAccess_token())
				.thenReturn("accessToken");
		when(webClientMock.get())
				.thenReturn(requestHeadersUriSpecMock);
		when(requestHeadersUriSpecMock.uri(anyString()))
				.thenReturn(requestHeadersSpecMock);
		when(requestHeadersSpecMock.headers(any(Consumer.class)))
				.thenReturn(requestHeadersSpecMock);
		when(requestHeadersSpecMock.retrieve())
				.thenReturn(responseSpecMock);
		when(this.responseSpecMock.bodyToMono(new ParameterizedTypeReference<List<DistrictEntity>>(){}))
				.thenReturn(districtEntitiesMock);
		when(this.districtEntitiesMock.block()).thenReturn(districts);

		List<District> result = districtService.getDistrictsFromInstituteApi();
	}

	@Test
	public void whenLoadDistrictsIntoRedisCache_DoesNotThrow() {
		List<DistrictEntity> districtEntities = Arrays.asList(new DistrictEntity());
		List<District> districts = Arrays.asList(new District());
		when(this.districtRedisRepository.saveAll(districtEntities))
				.thenReturn(districtEntities);
		assertDoesNotThrow(() -> districtService.loadDistrictsIntoRedisCache(districts));
	}
}
