package ca.bc.gov.educ.api.trax.service.institute;

import ca.bc.gov.educ.api.trax.constant.CacheKey;
import ca.bc.gov.educ.api.trax.constant.CacheStatus;
import ca.bc.gov.educ.api.trax.messaging.NatsConnection;
import ca.bc.gov.educ.api.trax.messaging.jetstream.Publisher;
import ca.bc.gov.educ.api.trax.messaging.jetstream.Subscriber;
import ca.bc.gov.educ.api.trax.model.dto.ResponseObj;
import ca.bc.gov.educ.api.trax.model.dto.institute.District;
import ca.bc.gov.educ.api.trax.model.dto.institute.DistrictContact;
import ca.bc.gov.educ.api.trax.model.dto.institute.SchoolDetail;
import ca.bc.gov.educ.api.trax.model.entity.institute.DistrictContactEntity;
import ca.bc.gov.educ.api.trax.model.entity.institute.DistrictEntity;
import ca.bc.gov.educ.api.trax.model.entity.institute.SchoolDetailEntity;
import ca.bc.gov.educ.api.trax.model.transformer.institute.DistrictTransformer;
import ca.bc.gov.educ.api.trax.model.transformer.institute.SchoolDetailTransformer;
import ca.bc.gov.educ.api.trax.repository.redis.DistrictRedisRepository;
import ca.bc.gov.educ.api.trax.repository.redis.SchoolDetailRedisRepository;
import ca.bc.gov.educ.api.trax.service.RESTService;
import ca.bc.gov.educ.api.trax.support.TestUtils;
import ca.bc.gov.educ.api.trax.util.EducGradTraxApiConstants;
import ca.bc.gov.educ.api.trax.util.RestUtils;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import redis.clients.jedis.JedisCluster;

import java.util.*;
import java.util.function.Consumer;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doNothing;


@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"unchecked","rawtypes"})
public class InstituteDistrictServiceTest {

	@Autowired
	private EducGradTraxApiConstants constants;
	@Autowired
	private DistrictTransformer districtTransformer;
	@Autowired
	private DistrictService districtService;
	@Autowired
	private SchoolService schoolService;
	@MockBean
	private DistrictRedisRepository districtRedisRepository;
	@MockBean
	private SchoolDetailRedisRepository schoolDetailRedisRepository;
	@MockBean
	private JedisConnectionFactory jedisConnectionFactoryMock;
	@MockBean
	private JedisCluster jedisClusterMock;

	@MockBean
	@Qualifier("default")
	WebClient webClientMock;
	@MockBean
	@Qualifier("instituteWebClient")
	private WebClient instWebClient;


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
	@MockBean
	private SchoolDetailTransformer schoolDetailTransformer;


	// NATS
	@MockBean
	private NatsConnection natsConnection;

	@MockBean
	private Publisher publisher;

	@MockBean
	private Subscriber subscriber;
	@MockBean
	private RestUtils restUtils;
	@MockBean
	private ServiceHelper serviceHelper;
	@MockBean
	private RESTService restServiceMock;

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
		List<DistrictEntity> districtEntities = new ArrayList<>();
		DistrictEntity districtEntity = new DistrictEntity();
		districtEntity.setDistrictId("ID");
		districtEntity.setDistrictNumber("1234");
		districtEntity.setDistrictStatusCode("SC");
		districtEntity.setDistrictRegionCode("RC");
		districtEntity.setContacts(Arrays.asList(new DistrictContactEntity(), new DistrictContactEntity()));
		districtEntities.add(districtEntity);

		List<District> districts = new ArrayList<>();
		District district = new District();
		district.setDistrictId("ID");
		district.setDistrictNumber("1234");
		district.setDistrictStatusCode("SC");
		district.setDistrictRegionCode("RC");
		district.setContacts(Arrays.asList(new DistrictContact(), new DistrictContact()));
		districts.add(district);

		when(restServiceMock.get(constants.getAllDistrictsFromInstituteApiUrl(), List.class, instWebClient))
				.thenReturn(districtEntities);
		when(districtTransformerMock.transformToDTO(districtEntities))
				.thenReturn(districts);

		List<District> result = districtService.getDistrictsFromInstituteApi();
		assertEquals(districts, result);
	}

	@Test
	public void whenLoadDistrictsIntoRedisCache_DoesNotThrow() {
		List<DistrictEntity> districtEntities = Arrays.asList(new DistrictEntity());
		List<District> districts = Arrays.asList(new District());
		when(this.districtRedisRepository.saveAll(districtEntities))
				.thenReturn(districtEntities);
		assertDoesNotThrow(() -> districtService.loadDistrictsIntoRedisCache(districts));
	}

	@Test
	public void whenGetDistrictsFromRedisCache_ReturnDistricts() {
		List<District> districts = new ArrayList<>();
		District district = new District();
		district.setDistrictId("ID");
		district.setDistrictNumber("1234");
		district.setDistrictStatusCode("SC");
		district.setDistrictRegionCode("RC");
		district.setContacts(Arrays.asList(new DistrictContact(), new DistrictContact()));
		districts.add(district);

		district = new District();
		district.setDistrictId("ID");
		district.setDistrictNumber("1234");
		district.setDistrictStatusCode("SC");
		district.setDistrictRegionCode("RC");
		district.setContacts(Arrays.asList(new DistrictContact(), new DistrictContact()));
		districts.add(district);

		List<DistrictEntity> districtEntities = new ArrayList<>();
		DistrictEntity districtEntity = new DistrictEntity();
		districtEntity.setDistrictId("ID");
		districtEntity.setDistrictNumber("1234");
		districtEntity.setDistrictStatusCode("SC");
		districtEntity.setDistrictRegionCode("RC");
		districtEntity.setContacts(Arrays.asList(new DistrictContactEntity(), new DistrictContactEntity()));
		districtEntities.add(districtEntity);

		districtEntity = new DistrictEntity();
		districtEntity.setDistrictId("ID");
		districtEntity.setDistrictNumber("1234");
		districtEntity.setDistrictStatusCode("SC");
		districtEntity.setDistrictRegionCode("RC");
		districtEntity.setContacts(Arrays.asList(new DistrictContactEntity(), new DistrictContactEntity()));
		districtEntities.add(districtEntity);

		when(this.districtRedisRepository.findAll())
				.thenReturn(districtEntities);
		when(this.districtTransformerMock.transformToDTO(districtEntities))
				.thenReturn(districts);
		assertEquals(districts, districtService.getDistrictsFromRedisCache());
	}

	@Test
	public void whenInitializeDistrictCache_WithLoadingAndFalse_DoNotForceLoad() {
		when(jedisClusterMock.get(CacheKey.DISTRICT_CACHE.name()))
				.thenReturn(String.valueOf(CacheStatus.LOADING));
		doThrow(new MockitoException("")).when(jedisClusterMock).set(CacheKey.DISTRICT_CACHE.name(), CacheStatus.LOADING.name());
		districtService.initializeDistrictCache(false);
	}

	@Test
	public void whenInitializeDistrictCache_WithReadyAndFalse_DoNotForceLoad() {
		when(jedisClusterMock.get(CacheKey.DISTRICT_CACHE.name()))
				.thenReturn(String.valueOf(CacheStatus.READY));
		doThrow(new MockitoException("")).when(jedisClusterMock).set(CacheKey.DISTRICT_CACHE.name(), CacheStatus.READY.name());
		districtService.initializeDistrictCache(false);
	}

	@Test
	public void whenInitializeDistrictCache_WithLoadingAndTrue_ThenForceLoad() {
		DistrictEntity de = new DistrictEntity();
		List<DistrictEntity> des = new ArrayList<DistrictEntity>();
		de.setDistrictId("123");
		de.setDistrictNumber("456");
		des.add(de);
		de = new DistrictEntity();
		de.setDistrictId("789");
		de.setDistrictNumber("012");
		des.add(de);

		District d = new District();
		List<District> ds = new ArrayList<District>();
		d.setDistrictId("123");
		d.setDistrictNumber("456");
		ds.add(d);
		d = new District();
		d.setDistrictId("789");
		d.setDistrictNumber("012");
		ds.add(d);

		when(webClientMock.get())
				.thenReturn(requestHeadersUriSpecMock);
		when(requestHeadersUriSpecMock.uri(anyString()))
				.thenReturn(requestHeadersSpecMock);
		when(requestHeadersSpecMock.headers(any(Consumer.class)))
				.thenReturn(requestHeadersSpecMock);
		when(this.restUtils.getTokenResponseObject(anyString(), anyString()))
				.thenReturn(responseObjectMock);
		when(this.responseObjectMock.getAccess_token())
				.thenReturn("accessToken");
		when(requestHeadersSpecMock.retrieve())
				.thenReturn(responseSpecMock);
		when(this.responseSpecMock.bodyToMono(new ParameterizedTypeReference<List<DistrictEntity>>(){}))
				.thenReturn(districtEntitiesMock);
		when(this.districtEntitiesMock.block())
				.thenReturn(des);

		when(this.restUtils.getTokenResponseObject(anyString(), anyString()))
				.thenReturn(responseObjectMock);
		when(this.responseObjectMock.getAccess_token())
				.thenReturn("accessToken");

		when(jedisClusterMock.get(CacheKey.DISTRICT_CACHE.name()))
				.thenReturn(String.valueOf(CacheStatus.LOADING));


		DistrictService districtServiceMock = mock(DistrictService.class);
		when(districtServiceMock.getDistrictsFromInstituteApi()).thenReturn(ds);
		doNothing().when(districtServiceMock).loadDistrictsIntoRedisCache(ds);

		districtService.initializeDistrictCache(true);
	}

	@Test
	public void whenGetDistrictByDistNoFromRedisCache_ReturnDistrict() {
		String distNo = "123";
		District district = new District();
		district.setDistrictId("ID");
		district.setDistrictNumber("123");
		district.setDistrictStatusCode("SC");
		district.setDistrictRegionCode("RC");
		district.setContacts(Arrays.asList(new DistrictContact(), new DistrictContact()));

		DistrictEntity districtEntity = new DistrictEntity();
		districtEntity.setDistrictId("ID");
		districtEntity.setDistrictNumber("456");
		districtEntity.setDistrictStatusCode("SC");
		districtEntity.setDistrictRegionCode("RC");
		districtEntity.setContacts(Arrays.asList(new DistrictContactEntity(), new DistrictContactEntity()));

		when(this.districtRedisRepository.findByDistrictNumber(distNo))
				.thenReturn(districtEntity);
		when(this.districtTransformerMock.transformToDTO(districtEntity))
				.thenReturn(district);
		assertEquals(district, districtService.getDistrictByDistNoFromRedisCache(distNo));
	}

	@Test
	public void whenGetDistrictByIdFromRedisCache_ReturnDistrict() {
		District district = new District();
		district.setDistrictId("ID");
		district.setDistrictNumber("1234");
		district.setDistrictStatusCode("SC");
		district.setDistrictRegionCode("RC");
		district.setContacts(Arrays.asList(new DistrictContact(), new DistrictContact()));

		DistrictEntity districtEntity = new DistrictEntity();
		districtEntity.setDistrictId("ID");
		districtEntity.setDistrictNumber("1234");
		districtEntity.setDistrictStatusCode("SC");
		districtEntity.setDistrictRegionCode("RC");
		districtEntity.setContacts(Arrays.asList(new DistrictContactEntity(), new DistrictContactEntity()));

		when(this.districtRedisRepository.findById("ID"))
				.thenReturn(Optional.of(districtEntity));
		when(this.districtTransformerMock.transformToDTO(Optional.of(districtEntity)))
				.thenReturn(district);
		assertEquals(district, districtService.getDistrictByIdFromRedisCache("ID"));
	}

	@Test
	public void whenInitializeDistrictCache_WithReadyAndTrue_ThenForceLoad() {

		District d = new District();
		List<District> ds = new ArrayList<District>();
		d.setDistrictId("123");
		d.setDistrictNumber("456");
		ds.add(d);
		d = new District();
		d.setDistrictId("789");
		d.setDistrictNumber("012");
		ds.add(d);

		when(jedisClusterMock.get(CacheKey.DISTRICT_CACHE.name()))
				.thenReturn(String.valueOf(CacheStatus.READY));
		when(jedisClusterMock.set(CacheKey.DISTRICT_CACHE.name(), CacheStatus.READY.name()))
				.thenReturn("OK");
		districtService.initializeDistrictCache(true);
		verify(serviceHelper).initializeCache(true, CacheKey.DISTRICT_CACHE, districtService);
	}

	@Test
	public void whenGetDistrictsBySchoolCategoryCode_ReturnDistricts() {
		String schoolCategoryCode = "ABC";
		List<District> districts = new ArrayList<>();
		District district = new District();
		district.setDistrictId("ID");
		district.setDistrictNumber("1234");
		district.setDistrictStatusCode("SC");
		district.setDistrictRegionCode("RC");
		district.setContacts(Arrays.asList(new DistrictContact(), new DistrictContact()));
		districts.add(district);

		district = new District();
		district.setDistrictId("ID");
		district.setDistrictNumber("1234");
		district.setDistrictStatusCode("SC");
		district.setDistrictRegionCode("RC");
		district.setContacts(Arrays.asList(new DistrictContact(), new DistrictContact()));
		districts.add(district);

		List<DistrictEntity> districtEntities = new ArrayList<>();
		DistrictEntity districtEntity = new DistrictEntity();
		districtEntity.setDistrictId("ID");
		districtEntity.setDistrictNumber("1234");
		districtEntity.setDistrictStatusCode("SC");
		districtEntity.setDistrictRegionCode("RC");
		districtEntity.setContacts(Arrays.asList(new DistrictContactEntity(), new DistrictContactEntity()));
		districtEntities.add(districtEntity);

		districtEntity = new DistrictEntity();
		districtEntity.setDistrictId("ID");
		districtEntity.setDistrictNumber("1234");
		districtEntity.setDistrictStatusCode("SC");
		districtEntity.setDistrictRegionCode("RC");
		districtEntity.setContacts(Arrays.asList(new DistrictContactEntity(), new DistrictContactEntity()));
		districtEntities.add(districtEntity);

		List<SchoolDetail> schoolDetails = new ArrayList<>();
		SchoolDetail schoolDetail = new SchoolDetail();
		schoolDetail.setSchoolId("ID");
		schoolDetail.setDistrictId("DistID");
		schoolDetail.setSchoolNumber("12345");
		schoolDetail.setSchoolCategoryCode("SCC");
		schoolDetail.setEmail("abc@xyz.ca");
		schoolDetails.add(schoolDetail);

		schoolDetail = new SchoolDetail();
		schoolDetail.setSchoolId("ID");
		schoolDetail.setDistrictId("DistID");
		schoolDetail.setSchoolNumber("12345");
		schoolDetail.setSchoolCategoryCode("SCC");
		schoolDetail.setEmail("abc@xyz.ca");
		schoolDetails.add(schoolDetail);

		List<SchoolDetailEntity> schoolDetailEntities = new ArrayList<>();
		SchoolDetailEntity schoolDetailEntity = new SchoolDetailEntity();
		schoolDetailEntity.setSchoolId("ID");
		schoolDetailEntity.setDistrictId("DistID");
		schoolDetailEntity.setSchoolNumber("12345");
		schoolDetailEntity.setSchoolCategoryCode("SCC");
		schoolDetailEntity.setEmail("abc@xyz.ca");
		schoolDetailEntities.add(schoolDetailEntity);

		schoolDetailEntity = new SchoolDetailEntity();
		schoolDetailEntity.setSchoolId("ID");
		schoolDetailEntity.setDistrictId("DistID");
		schoolDetailEntity.setSchoolNumber("12345");
		schoolDetailEntity.setSchoolCategoryCode("SCC");
		schoolDetailEntity.setEmail("abc@xyz.ca");
		schoolDetailEntities.add(schoolDetailEntity);


		when(this.schoolDetailRedisRepository.findBySchoolCategoryCode(schoolCategoryCode))
				.thenReturn(schoolDetailEntities);
		when(this.schoolDetailTransformer.transformToDTO(schoolDetailEntities))
				.thenReturn(schoolDetails);
		when(this.districtService.getDistrictByIdFromRedisCache("ID"))
				.thenReturn(district);
		when(this.districtTransformerMock.transformToDTO(districtEntities))
				.thenReturn(districts);
		when(this.schoolService.getSchoolDetailsBySchoolCategoryCode(schoolCategoryCode))
				.thenReturn(schoolDetails);
		assertEquals(districts, districtService.getDistrictsBySchoolCategoryCode(schoolCategoryCode));
	}

	@Test
	public void updateDistrictCache_givenValidDistrictId_shouldUpdateCache() {
		// given
		// set up initial district in redis mock
		District district = new District();
		district.setDistrictId(UUID.randomUUID().toString());
		district.setDistrictNumber("002");
		district.setFaxNumber("1233216547");
		district.setPhoneNumber("3216549874");
		district.setEmail("district@district.ca");
		district.setWebsite("www.district.ca");
		district.setDisplayName("Test Display Name");
		district.setDistrictRegionCode("NOT_APPLIC");
		district.setDistrictStatusCode("INACTIVE");

		DistrictEntity districtEntity = new DistrictEntity();
		districtEntity.setDistrictId(UUID.randomUUID().toString());
		districtEntity.setDistrictNumber("002");
		districtEntity.setFaxNumber("1233216547");
		districtEntity.setPhoneNumber("3216549874");
		districtEntity.setEmail("district@district.ca");
		districtEntity.setWebsite("www.district.ca");
		districtEntity.setDisplayName("Test Display Name");
		districtEntity.setDistrictRegionCode("NOT_APPLIC");
		districtEntity.setDistrictStatusCode("INACTIVE");
		// when
		// call updateDistrictCache with district id and mock a return from webclient
		Mockito.when(this.restServiceMock.get(anyString(),
				any(), any(WebClient.class))).thenReturn(district);
		// then
		Mockito.when(this.districtRedisRepository.save(districtEntity)).thenReturn(districtEntity);
		Mockito.when(this.districtRedisRepository.findById(districtEntity.getDistrictId())).thenReturn(Optional.of(districtEntity));
		// mock return of district from redis cache and compare
		districtService.updateDistrictCache(districtEntity.getDistrictId());
		assertTrue(this.districtRedisRepository.findById(districtEntity.getDistrictId()).isPresent());
	}

	private DistrictEntity createDistrictEntity() {
		District d = TestUtils.createDistrict();
        return districtTransformer.transformToEntity(d);
	}
}
