package ca.bc.gov.educ.api.trax.service.institute;

import ca.bc.gov.educ.api.trax.constant.CacheKey;
import ca.bc.gov.educ.api.trax.constant.CacheStatus;
import ca.bc.gov.educ.api.trax.messaging.NatsConnection;
import ca.bc.gov.educ.api.trax.messaging.jetstream.Publisher;
import ca.bc.gov.educ.api.trax.messaging.jetstream.Subscriber;
import ca.bc.gov.educ.api.trax.model.dto.ResponseObj;
import ca.bc.gov.educ.api.trax.model.dto.institute.SchoolCategoryCode;
import ca.bc.gov.educ.api.trax.model.dto.institute.SchoolFundingGroupCode;
import ca.bc.gov.educ.api.trax.model.entity.institute.SchoolCategoryCodeEntity;
import ca.bc.gov.educ.api.trax.model.entity.institute.SchoolFundingGroupCodeEntity;
import ca.bc.gov.educ.api.trax.model.transformer.institute.SchoolCategoryCodeTransformer;
import ca.bc.gov.educ.api.trax.repository.GradCountryRepository;
import ca.bc.gov.educ.api.trax.repository.GradProvinceRepository;
import ca.bc.gov.educ.api.trax.repository.redis.SchoolCategoryCodeRedisRepository;
import ca.bc.gov.educ.api.trax.repository.redis.SchoolFundingGroupCodeRedisRepository;
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
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"unchecked","rawtypes"})
public class InstituteCodeServiceTest {

	@Autowired
	private EducGradTraxApiConstants constants;
	@Autowired
	private CodeService codeService;
	@MockBean
	private SchoolCategoryCodeRedisRepository schoolCategoryCodeRedisRepository;
	@MockBean
	private SchoolFundingGroupCodeRedisRepository schoolFundingGroupCodeRedisRepository;
	@MockBean
	private GradCountryRepository gradCountryRepository;
	@MockBean
	private GradProvinceRepository gradProvinceRepository;
	@MockBean
	private JedisConnectionFactory jedisConnectionFactory;

	@MockBean
	@Qualifier("default")
	WebClient webClientMock;
	@MockBean
	RedisTemplate<String, String> redisTemplateMock;
	@Mock
	ValueOperations valueOperationsMock;
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
	private Mono<List<SchoolCategoryCode>> schoolCategoryCodesMock;
	@Mock
	private Mono<List<SchoolCategoryCodeEntity>> schoolCategoryCodeEntitiesMock;
	@Mock
	private Mono<List<SchoolFundingGroupCodeEntity>> schoolFundingGroupCodeEntitiesMock;
	@Mock
	SchoolCategoryCodeTransformer schoolCategoryCodeTransformer;
	@MockBean
	private RestUtils restUtils;

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

	@Test
	public void whenGetSchoolCategoryCodesFromInstituteApi_returnsListOfSchoolCategoryCode() {
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
		when(this.responseSpecMock.bodyToMono(new ParameterizedTypeReference<List<SchoolCategoryCodeEntity>>(){}))
				.thenReturn(schoolCategoryCodeEntitiesMock);
		when(this.schoolCategoryCodeEntitiesMock.block())
				.thenReturn(schoolCategoryCodes);

		List<SchoolCategoryCode> result = codeService.getSchoolCategoryCodesFromInstituteApi();
		//assertThat(result).hasSize(1);
	}

	@Test
	public void whenGetSchoolFundingGroupCodesFromInstituteApi_returnsListOfSchoolFundingGroupCode() {
		List<SchoolFundingGroupCodeEntity> schoolFundingGroupCodes = new ArrayList<>();
		SchoolFundingGroupCodeEntity sfgc = new SchoolFundingGroupCodeEntity();

		sfgc.setSchoolFundingGroupCode("CODE");
		sfgc.setDescription("Description");
		sfgc.setLabel("Label");
		sfgc.setEffectiveDate("01-01-2024");
		sfgc.setExpiryDate("01-01-2024");
		sfgc.setDisplayOrder("10");
		schoolFundingGroupCodes.add(sfgc);

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
		when(this.responseSpecMock.bodyToMono(new ParameterizedTypeReference<List<SchoolFundingGroupCodeEntity>>(){}))
				.thenReturn(Mono.just(schoolFundingGroupCodes));
		when(this.schoolFundingGroupCodeEntitiesMock.block())
				.thenReturn(schoolFundingGroupCodes);

		List<SchoolFundingGroupCode> result = codeService.getSchoolFundingGroupCodesFromInstituteApi();
		//assertThat(result).hasSize(1);

	}

	@Test
	public void whenLoadSchoolCategoryCodesIntoRedisCache_DoesNotThrow() {
		List<SchoolFundingGroupCodeEntity> schoolFundingGroupCodeEntities = Arrays.asList(new SchoolFundingGroupCodeEntity());
		List<SchoolFundingGroupCode> schoolFundingGroupCodes = Arrays.asList(new SchoolFundingGroupCode());
		when(this.schoolFundingGroupCodeRedisRepository.saveAll(schoolFundingGroupCodeEntities))
				.thenReturn(schoolFundingGroupCodeEntities);
		assertDoesNotThrow(() -> codeService.loadSchoolFundingGroupCodesIntoRedisCache(schoolFundingGroupCodes));
	}

	@Test
	public void whenLoadSchoolFundingGroupCodesIntoRedisCache_DoesNotThrow() {
		List<SchoolFundingGroupCodeEntity> schoolFundingGroupCodeEntities = Arrays.asList(new SchoolFundingGroupCodeEntity());
		List<SchoolFundingGroupCode> schoolFundingGroupCodes = Arrays.asList(new SchoolFundingGroupCode());
		when(this.schoolFundingGroupCodeRedisRepository.saveAll(schoolFundingGroupCodeEntities))
				.thenReturn(schoolFundingGroupCodeEntities);
		assertDoesNotThrow(() -> codeService.loadSchoolFundingGroupCodesIntoRedisCache(schoolFundingGroupCodes));
	}

	@Test
	public void whenGetSchoolCategoryCodesFromRedisCache_GetSchoolCategoryCodes() {
		SchoolCategoryCodeEntity scce = new SchoolCategoryCodeEntity();
		List<SchoolCategoryCodeEntity> scces = new ArrayList<>();
		scce.setSchoolCategoryCode("SCC1");
		scce.setLabel("SCC1-label");
		scces.add(scce);
		scce = new SchoolCategoryCodeEntity();
		scce.setSchoolCategoryCode("SCC2");
		scce.setLabel("SCC2-label");
		scces.add(scce);
		when(schoolCategoryCodeRedisRepository.findAll()).thenReturn(scces);
		//assertTrue(codeService.getSchoolCategoryCodesFromRedisCache().size() == 2);
	}

	@Test
	public void whenInitializeSchoolCategoryCodeCache_WithLoadingAndFalse_DoNotForceLoad() {
		when(redisTemplateMock.opsForValue())
				.thenReturn(valueOperationsMock);
		when(valueOperationsMock.get(CacheKey.SCHOOL_CATEGORY_CODE_CACHE.name()))
				.thenReturn(String.valueOf(CacheStatus.LOADING));
		doNothing().when(valueOperationsMock).set(CacheKey.SCHOOL_CATEGORY_CODE_CACHE.name(), CacheStatus.LOADING.name());
		codeService.initializeSchoolCategoryCodeCache(false);
	}

	@Test
	public void whenInitializeSchoolCategoryCodeCache_WithReadyAndFalse_DoNotForceLoad() {
		when(redisTemplateMock.opsForValue())
				.thenReturn(valueOperationsMock);
		when(valueOperationsMock.get(CacheKey.SCHOOL_CATEGORY_CODE_CACHE.name()))
				.thenReturn(String.valueOf(CacheStatus.READY));
		doNothing().when(valueOperationsMock).set(CacheKey.SCHOOL_CATEGORY_CODE_CACHE.name(), CacheStatus.READY.name());
		codeService.initializeSchoolCategoryCodeCache(false);
	}

	@Test
	public void whenInitializeSchoolCategoryCodeCache_WithLoadingAndTrue_ThenForceLoad() {

		SchoolCategoryCodeEntity scce = new SchoolCategoryCodeEntity();
		List<SchoolCategoryCodeEntity> scces = new ArrayList<SchoolCategoryCodeEntity>();
		scce.setSchoolCategoryCode("SCC1");
		scce.setLabel("SCC1-label");
		scces.add(scce);
		scce = new SchoolCategoryCodeEntity();
		scce.setSchoolCategoryCode("SCC2");
		scce.setLabel("SCC2-label");
		scces.add(scce);

		SchoolCategoryCode scc = new SchoolCategoryCode();
		List<SchoolCategoryCode> sccs = new ArrayList<SchoolCategoryCode>();
		scc.setSchoolCategoryCode("SCC1");
		scc.setLabel("SCC1-label");
		scc.setDescription("Desc");
		scc.setLegacyCode("SCC1-legacy");
		scc.setDisplayOrder("10");
		scc.setEffectiveDate("01-01-2024");
		scc.setExpiryDate("01-01-2024");
		sccs.add(scc);
		scc = new SchoolCategoryCode();
		scc.setSchoolCategoryCode("SCC2");
		scc.setLabel("SCC2-label");
		scc.setDescription("Desc");
		scc.setLegacyCode("SCC2-legacy");
		scc.setDisplayOrder("20");
		scc.setEffectiveDate("01-01-2024");
		scc.setExpiryDate("01-01-2024");
		sccs.add(scc);


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
		when(this.responseSpecMock.bodyToMono(new ParameterizedTypeReference<List<SchoolCategoryCodeEntity>>(){}))
				.thenReturn(schoolCategoryCodeEntitiesMock);
		when(this.schoolCategoryCodeEntitiesMock.block())
				.thenReturn(scces);

		when(this.restUtils.getTokenResponseObject(anyString(), anyString()))
				.thenReturn(responseObjectMock);
		when(this.responseObjectMock.getAccess_token())
				.thenReturn("accessToken");

		when(redisTemplateMock.opsForValue())
				.thenReturn(valueOperationsMock);
		when(valueOperationsMock.get(CacheKey.SCHOOL_CATEGORY_CODE_CACHE.name()))
				.thenReturn(String.valueOf(CacheStatus.LOADING));
		doNothing().when(valueOperationsMock).set(CacheKey.SCHOOL_CATEGORY_CODE_CACHE.name(), CacheStatus.LOADING.name());

		CodeService codeServicemock = mock(CodeService.class);
		when(codeServicemock.getSchoolCategoryCodesFromInstituteApi()).thenReturn(sccs);
		doNothing().when(codeServicemock).loadSchoolCategoryCodesIntoRedisCache(sccs);

		codeService.initializeSchoolCategoryCodeCache(true);
		//verify(codeServicemock).loadSchoolCategoryCodesIntoRedisCache(sccs);
	}

	@Test
	public void whenInitializeSchoolCategoryCodeCache_WithReadyAndTrue_ThenForceLoad() {
		when(redisTemplateMock.opsForValue())
				.thenReturn(valueOperationsMock);
		when(valueOperationsMock.get(CacheKey.SCHOOL_CATEGORY_CODE_CACHE.name()))
				.thenReturn(String.valueOf(CacheStatus.READY));
		doNothing().when(valueOperationsMock).set(CacheKey.SCHOOL_CATEGORY_CODE_CACHE.name(), CacheStatus.READY.name());
		codeService.initializeSchoolCategoryCodeCache(true);
	}

	@Test
	public void whenGetSchoolFundingGroupCodesFromRedisCache_GetSchoolFundingGroupCodes() {
		SchoolFundingGroupCodeEntity sfgce = new SchoolFundingGroupCodeEntity();
		List<SchoolFundingGroupCodeEntity> sfgces = new ArrayList<SchoolFundingGroupCodeEntity>();
		sfgce.setSchoolFundingGroupCode("SFGC1");
		sfgce.setLabel("SFGC1-label");
		sfgces.add(sfgce);
		sfgce = new SchoolFundingGroupCodeEntity();
		sfgce.setSchoolFundingGroupCode("SFGC2");
		sfgce.setLabel("SFGC2-label");
		sfgces.add(sfgce);
		when(schoolFundingGroupCodeRedisRepository.findAll()).thenReturn(sfgces);
		//assertTrue(codeService.getSchoolCategoryCodesFromRedisCache().size() == 2);
	}

	@Test
	public void whenInitializeSchoolFundingGroupCodeCache_WithLoadingAndFalse_DoNotForceLoad() {
		when(redisTemplateMock.opsForValue())
				.thenReturn(valueOperationsMock);
		when(valueOperationsMock.get(CacheKey.SCHOOL_FUNDING_GROUP_CODE_CACHE.name()))
				.thenReturn(String.valueOf(CacheStatus.LOADING));
		doNothing().when(valueOperationsMock).set(CacheKey.SCHOOL_FUNDING_GROUP_CODE_CACHE.name(), CacheStatus.LOADING.name());
		codeService.initializeSchoolFundingGroupCodeCache(false);
	}

	@Test
	public void whenInitializeSchoolFundingGroupCodeCache_WithReadyAndFalse_DoNotForceLoad() {
		when(redisTemplateMock.opsForValue())
				.thenReturn(valueOperationsMock);
		when(valueOperationsMock.get(CacheKey.SCHOOL_FUNDING_GROUP_CODE_CACHE.name()))
				.thenReturn(String.valueOf(CacheStatus.READY));
		doNothing().when(valueOperationsMock).set(CacheKey.SCHOOL_FUNDING_GROUP_CODE_CACHE.name(), CacheStatus.READY.name());
		codeService.initializeSchoolFundingGroupCodeCache(false);
	}

	@Test
	public void whenInitializeSchoolFundingGroupCodeCache_WithLoadingAndTrue_ThenForceLoad() {

		SchoolFundingGroupCodeEntity sfgce = new SchoolFundingGroupCodeEntity();
		List<SchoolFundingGroupCodeEntity> sfgces = new ArrayList<SchoolFundingGroupCodeEntity>();
		sfgce.setSchoolFundingGroupCode("SCC1");
		sfgce.setLabel("SCC1-label");
		sfgces.add(sfgce);
		sfgce = new SchoolFundingGroupCodeEntity();
		sfgce.setSchoolFundingGroupCode("SCC2");
		sfgce.setLabel("SCC2-label");
		sfgces.add(sfgce);

		SchoolFundingGroupCode sfgc = new SchoolFundingGroupCode();
		List<SchoolFundingGroupCode> sfgcs = new ArrayList<SchoolFundingGroupCode>();
		sfgc.setSchoolFundingGroupCode("SCC1");
		sfgc.setLabel("SCC1-label");
		sfgc.setDescription("Desc");
		sfgc.setDisplayOrder("10");
		sfgc.setEffectiveDate("01-01-2024");
		sfgc.setExpiryDate("01-01-2024");
		sfgcs.add(sfgc);
		sfgc = new SchoolFundingGroupCode();
		sfgc.setSchoolFundingGroupCode("SCC2");
		sfgc.setLabel("SCC2-label");
		sfgc.setDescription("Desc");
		sfgc.setDisplayOrder("20");
		sfgc.setEffectiveDate("01-01-2024");
		sfgc.setExpiryDate("01-01-2024");
		sfgcs.add(sfgc);


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
		when(this.responseSpecMock.bodyToMono(new ParameterizedTypeReference<List<SchoolFundingGroupCodeEntity>>(){}))
				.thenReturn(schoolFundingGroupCodeEntitiesMock);
		when(this.schoolFundingGroupCodeEntitiesMock.block())
				.thenReturn(sfgces);

		when(this.restUtils.getTokenResponseObject(anyString(), anyString()))
				.thenReturn(responseObjectMock);
		when(this.responseObjectMock.getAccess_token())
				.thenReturn("accessToken");

		when(redisTemplateMock.opsForValue())
				.thenReturn(valueOperationsMock);
		when(valueOperationsMock.get(CacheKey.SCHOOL_CATEGORY_CODE_CACHE.name()))
				.thenReturn(String.valueOf(CacheStatus.LOADING));
		doNothing().when(valueOperationsMock).set(CacheKey.SCHOOL_CATEGORY_CODE_CACHE.name(), CacheStatus.LOADING.name());

		CodeService codeServicemock = mock(CodeService.class);
		when(codeServicemock.getSchoolFundingGroupCodesFromInstituteApi()).thenReturn(sfgcs);
		doNothing().when(codeServicemock).loadSchoolFundingGroupCodesIntoRedisCache(sfgcs);

		codeService.initializeSchoolFundingGroupCodeCache(true);
		//verify(codeServicemock).loadSchoolCategoryCodesIntoRedisCache(sccs);
	}

	@Test
	public void whenInitializeSchoolFundingGroupCodeCache_WithReadyAndTrue_ThenForceLoad() {
		when(redisTemplateMock.opsForValue())
				.thenReturn(valueOperationsMock);
		when(valueOperationsMock.get(CacheKey.SCHOOL_FUNDING_GROUP_CODE_CACHE.name()))
				.thenReturn(String.valueOf(CacheStatus.READY));
		doNothing().when(valueOperationsMock).set(CacheKey.SCHOOL_FUNDING_GROUP_CODE_CACHE.name(), CacheStatus.READY.name());
		codeService.initializeSchoolFundingGroupCodeCache(true);
	}
}
