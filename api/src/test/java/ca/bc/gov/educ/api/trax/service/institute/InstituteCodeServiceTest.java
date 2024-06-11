package ca.bc.gov.educ.api.trax.service.institute;

import ca.bc.gov.educ.api.trax.messaging.NatsConnection;
import ca.bc.gov.educ.api.trax.messaging.jetstream.Publisher;
import ca.bc.gov.educ.api.trax.messaging.jetstream.Subscriber;
import ca.bc.gov.educ.api.trax.model.dto.ResponseObj;
import ca.bc.gov.educ.api.trax.model.entity.institute.SchoolCategoryCodeEntity;
import ca.bc.gov.educ.api.trax.repository.GradCountryRepository;
import ca.bc.gov.educ.api.trax.repository.GradProvinceRepository;
import ca.bc.gov.educ.api.trax.util.EducGradTraxApiConstants;
import ca.bc.gov.educ.api.trax.util.RestUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;


@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"rawtypes"})
public class InstituteCodeServiceTest {

	@Autowired
	private EducGradTraxApiConstants constants;
	@Autowired
	private CodeService codeService;

	@MockBean
	private GradCountryRepository gradCountryRepository;

	@MockBean
	private GradProvinceRepository gradProvinceRepository;

	@MockBean
	@Qualifier("traxClient")
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
	private Mono<List<SchoolCategoryCodeEntity>> schoolCategoryCodeEntitiesMock;
	@MockBean
	private RestUtils restUtilsMock;

	// NATS
	@MockBean
	private NatsConnection natsConnection;

	@MockBean
	private Publisher publisher;

	@MockBean
	private Subscriber subscriber;

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

	@Before
	public void setUp() {
		openMocks(this);
	}

	@After
	public void tearDown() {

	}

	@Test
	public void whenGetSchoolCategoryCodesFromInstituteApi_returnsListOfSchoolCategoryCodeEntity() {
		List<SchoolCategoryCodeEntity> schoolCategoryCodes = new ArrayList<>();
		SchoolCategoryCodeEntity scce = new SchoolCategoryCodeEntity();

		scce.setSchoolCategoryCode("11");
		scce.setDescription("Description");
		scce.setLegacyCode("LegacyCode");
		scce.setLabel("Label");
		scce.setEffectiveDate("01-01-2024");
		scce.setExpiryDate("01-01-2024");
		scce.setDisplayOrder("10");
		schoolCategoryCodes.add(scce);

		ResponseObj tokenObj = new ResponseObj();
		tokenObj.setAccess_token("123");

		when(webClientMock.get())
				.thenReturn(requestHeadersUriSpecMock);
		when(requestHeadersUriSpecMock.uri(anyString()))
				.thenReturn(requestHeadersSpecMock);
		when(this.restUtilsMock.getTokenResponseObject(anyString(), anyString()))
				.thenReturn(tokenObj);
		when(this.responseObjectMock.getAccess_token())
				.thenReturn("accessToken");
		when(requestHeadersSpecMock.retrieve())
				.thenReturn(responseSpecMock);
		when(this.responseSpecMock.bodyToMono(new ParameterizedTypeReference<List<SchoolCategoryCodeEntity>>(){}))
				.thenReturn(schoolCategoryCodeEntitiesMock);
		when(this.schoolCategoryCodeEntitiesMock.block())
				.thenReturn(schoolCategoryCodes);


		/*when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
		when(this.requestHeadersUriMock.uri(constants.getAllSchoolCategoryCodesFromInstituteApiUrl())).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
		when(this.responseMock.bodyToMono(schoolCategoryCodeEntities)).thenReturn(Mono.just(schoolCategoryCodes));
		when(this.restUtilsMock.getTokenResponseObject("edx-grad-client-name", "xyz")).thenReturn(this.responseObjectMock);
		when(this.responseObjectMock.getAccess_token()).thenReturn("accessToken");
*/
		List<SchoolCategoryCodeEntity> result = codeService.getSchoolCategoryCodesFromInstituteApi();
		//assertThat(result).hasSize(1);
	}
	
	/*@Test
	public void testGetAllProvinceList() {
		List<GradProvinceEntity> gradProvinceList = new ArrayList<>();
		GradProvinceEntity obj = new GradProvinceEntity();
		obj.setProvCode("BC");
		obj.setProvName("British Columbia");
		gradProvinceList.add(obj);
		obj = new GradProvinceEntity();
		obj.setProvCode("AB");
		obj.setProvName("Alberta");
		gradProvinceList.add(obj);
		Mockito.when(gradProvinceRepository.findAll()).thenReturn(gradProvinceList);
		List<GradProvince> provinces = codeService.getAllProvinceCodeList();
		Assert.assertTrue(provinces.size() == 2);
	}
	
	@Test
	public void testGetSpecificProvinceCode() {
		String provCode = "BC";
		GradProvince obj = new GradProvince();
		obj.setProvCode("BC");
		obj.setProvName("British Columbia");
		obj.toString();
		GradProvinceEntity objEntity = new GradProvinceEntity();
		objEntity.setProvCode("BC");
		objEntity.setProvName("British Columbia");
		Mockito.when(gradProvinceRepository.findById(provCode)).thenReturn(Optional.of(objEntity));
		GradProvince gradProvince = codeService.getSpecificProvinceCode(provCode);
		assertThat(gradProvince).isNotNull();

	}
	
	@Test
	public void testGetSpecificProvinceCodeReturnsNull() {
		String provCode = "BC";
		Mockito.when(gradProvinceRepository.findById(provCode)).thenReturn(Optional.empty());
		GradProvince gradProvince = codeService.getSpecificProvinceCode(provCode);
		assertThat(gradProvince).isNull();
	}
	
	@Test
	public void testGetAllCountryList() {
		List<GradCountryEntity> gradCountryList = new ArrayList<>();
		GradCountryEntity obj = new GradCountryEntity();
		obj.setCountryCode("CA");
		obj.setCountryName("Canada");
		gradCountryList.add(obj);
		obj = new GradCountryEntity();
		obj.setCountryCode("USA");
		obj.setCountryName("America");
		gradCountryList.add(obj);
		Mockito.when(gradCountryRepository.findAll()).thenReturn(gradCountryList);
		List<GradCountry> gradCountries = codeService.getAllCountryCodeList();
		assertThat(gradCountries).isNotNull();
	}
	
	@Test
	public void testGetSpecificCountryCode() {
		String countryCode = "AB";
		GradCountry obj = new GradCountry();
		obj.setCountryCode("CA");
		obj.setCountryName("Canada");
		obj.toString();
		GradCountryEntity objEntity = new GradCountryEntity();
		objEntity.setCountryCode("CA");
		objEntity.setCountryName("Canada");
		Optional<GradCountryEntity> ent = Optional.of(objEntity);
		Mockito.when(gradCountryRepository.findById(countryCode)).thenReturn(ent);
		GradCountry country = codeService.getSpecificCountryCode(countryCode);
		assertThat(country).isNotNull();
	}
	
	@Test
	public void testGetSpecificCountryCodeReturnsNull() {
		String countryCode = "CA";
		Mockito.when(gradCountryRepository.findById(countryCode)).thenReturn(Optional.empty());
		GradCountry country = codeService.getSpecificCountryCode(countryCode);
		assertThat(country).isNull();
	}*/
}
