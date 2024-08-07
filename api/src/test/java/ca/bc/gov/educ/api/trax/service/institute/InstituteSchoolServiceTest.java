package ca.bc.gov.educ.api.trax.service.institute;

import ca.bc.gov.educ.api.trax.constant.CacheKey;
import ca.bc.gov.educ.api.trax.messaging.NatsConnection;
import ca.bc.gov.educ.api.trax.messaging.jetstream.Publisher;
import ca.bc.gov.educ.api.trax.messaging.jetstream.Subscriber;
import ca.bc.gov.educ.api.trax.model.dto.ResponseObj;
import ca.bc.gov.educ.api.trax.model.dto.institute.District;
import ca.bc.gov.educ.api.trax.model.dto.institute.School;
import ca.bc.gov.educ.api.trax.model.dto.institute.SchoolDetail;
import ca.bc.gov.educ.api.trax.model.entity.institute.*;
import ca.bc.gov.educ.api.trax.model.transformer.institute.DistrictTransformer;
import ca.bc.gov.educ.api.trax.model.transformer.institute.SchoolDetailTransformer;
import ca.bc.gov.educ.api.trax.model.transformer.institute.SchoolTransformer;
import ca.bc.gov.educ.api.trax.repository.GradCountryRepository;
import ca.bc.gov.educ.api.trax.repository.GradProvinceRepository;
import ca.bc.gov.educ.api.trax.repository.redis.SchoolDetailRedisRepository;
import ca.bc.gov.educ.api.trax.repository.redis.SchoolRedisRepository;
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
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import redis.clients.jedis.JedisCluster;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;


@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"unchecked","rawtypes"})
public class InstituteSchoolServiceTest {

	@Autowired
	private EducGradTraxApiConstants constants;
	@Autowired
	private SchoolService schoolService;
	@MockBean
	private ServiceHelper serviceHelperMock;
	@MockBean
	private SchoolRedisRepository schoolRedisRepository;
	@MockBean
	private SchoolDetailRedisRepository schoolDetailRedisRepository;
	@MockBean
	private JedisConnectionFactory jedisConnectionFactoryMock;
	@MockBean
	private JedisCluster jedisClusterMock;
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
	private Mono<List<SchoolEntity>> schoolEntitiesMock;
	@Mock
	private Mono<List<SchoolDetailEntity>> schoolDetailEntitiesMock;
	@Mock
	private List<School> schoolsMock;
	@Mock
	private List<SchoolDetail> schoolDetailsMock;
	@MockBean
	private RestUtils restUtils;
	@MockBean
	private SchoolTransformer schoolTransformerMock;
	@Autowired
	private SchoolTransformer schoolTransformer;
	@MockBean
	private SchoolDetailTransformer schoolDetailTransformerMock;

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
	public void whenGetSchoolsFromInstituteApi_returnsListOfSchools() {
		List<SchoolEntity> schools = new ArrayList<>();
		SchoolEntity school = new SchoolEntity();

		school.setSchoolId("ID");
		school.setDistrictId("DistID");
		school.setSchoolNumber("12345");
		school.setSchoolCategoryCode("SCC");
		school.setEmail("abc@xyz.ca");

		schools.add(school);

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
		when(this.responseSpecMock.bodyToMono(new ParameterizedTypeReference<List<SchoolEntity>>(){}))
				.thenReturn(schoolEntitiesMock);
		when(this.schoolEntitiesMock.block()).thenReturn(schools);

		when(this.schoolTransformerMock.transformToDTO(schools))
				.thenReturn(schoolsMock);


		List<School> result = schoolService.getSchoolsFromInstituteApi();
	}

	@Test
	public void whenLoadSchoolsIntoRedisCache_DoesNotThrow() {
		List<SchoolEntity> schoolEntities = Arrays.asList(new SchoolEntity());
		List<School> schools = Arrays.asList(new School());
		when(this.schoolRedisRepository.saveAll(schoolEntities))
				.thenReturn(schoolEntities);
		assertDoesNotThrow(() -> schoolService.loadSchoolsIntoRedisCache(schools));
	}

	@Test
	public void whenGetSchoolsFromRedisCache_ReturnSchools() {
		String mincode = "12345678";
		List<School> schools = new ArrayList<>();
		School school = new School();
		school.setSchoolId("ID");
		school.setDistrictId("DistID");
		school.setSchoolNumber("12345");
		school.setMincode(mincode);
		school.setSchoolCategoryCode("SCC");
		school.setEmail("abc@xyz.ca");
		schools.add(school);

		school = new School();
		school.setSchoolId("ID");
		school.setDistrictId("DistID");
		school.setSchoolNumber("12345");
		school.setMincode(mincode);
		school.setSchoolCategoryCode("SCC");
		school.setEmail("abc@xyz.ca");
		schools.add(school);

		List<SchoolEntity> schoolEntities = new ArrayList<>();
		SchoolEntity schoolEntity = new SchoolEntity();
		schoolEntity.setSchoolId("ID");
		schoolEntity.setDistrictId("DistID");
		schoolEntity.setSchoolNumber("12345");
		schoolEntity.setMincode(mincode);
		schoolEntity.setSchoolCategoryCode("SCC");
		schoolEntity.setEmail("abc@xyz.ca");
		schoolEntities.add(schoolEntity);

		schoolEntity = new SchoolEntity();
		schoolEntity.setSchoolId("ID");
		schoolEntity.setDistrictId("DistID");
		schoolEntity.setSchoolNumber("12345");
		schoolEntity.setMincode(mincode);
		schoolEntity.setSchoolCategoryCode("SCC");
		schoolEntity.setEmail("abc@xyz.ca");
		schoolEntities.add(schoolEntity);

		when(this.schoolRedisRepository.findAll())
				.thenReturn(schoolEntities);
		when(this.schoolTransformer.transformToDTO(schoolEntities))
				.thenReturn(schools);
		assertEquals(schools, schoolService.getSchoolsFromRedisCache());
	}

	@Test
	public void whenGetSchoolByMincodeFromRedisCache_ReturnSchool() {
		String mincode = "12345678";
		School school = new School();
		school.setSchoolId("ID");
		school.setDistrictId("DistID");
		school.setSchoolNumber("12345");
		school.setMincode(mincode);
		school.setSchoolCategoryCode("SCC");
		school.setEmail("abc@xyz.ca");

		SchoolEntity schoolEntity = new SchoolEntity();
		schoolEntity.setSchoolId("ID");
		schoolEntity.setDistrictId("DistID");
		schoolEntity.setSchoolNumber("12345");
		schoolEntity.setMincode(mincode);
		schoolEntity.setSchoolCategoryCode("SCC");
		schoolEntity.setEmail("abc@xyz.ca");

		when(this.schoolRedisRepository.findByMincode(mincode))
				.thenReturn(schoolEntity);
		when(this.schoolTransformer.transformToDTO(schoolEntity))
				.thenReturn(school);
		assertEquals(school, schoolService.getSchoolByMincodeFromRedisCache(mincode));
	}

	@Test
	public void whenInitializeSchoolCache_DoNothing() {
		doNothing().when(serviceHelperMock).initializeCache(false, CacheKey.SCHOOL_CACHE, serviceHelperMock);
		schoolService.initializeSchoolCache(false);
	}

	@Test
	public void whenGetSchoolDetailsFromInstituteApi_returnsListOfSchoolDetails() {
		List<SchoolDetailEntity> schoolDetails = new ArrayList<>();
		SchoolDetailEntity schoolDetail = new SchoolDetailEntity();
		schoolDetail.setSchoolId("ID");
		schoolDetail.setDistrictId("DistID");
		schoolDetail.setSchoolNumber("12345");
		schoolDetail.setSchoolCategoryCode("SCC");
		schoolDetail.setEmail("abc@xyz.ca");

		schoolDetails.add(schoolDetail);

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
		when(this.responseSpecMock.bodyToMono(new ParameterizedTypeReference<List<SchoolDetailEntity>>(){}))
				.thenReturn(schoolDetailEntitiesMock);
		when(this.schoolDetailEntitiesMock.block()).thenReturn(schoolDetails);

		when(this.schoolDetailTransformerMock.transformToDTO(schoolDetails))
				.thenReturn(schoolDetailsMock);

		List<SchoolDetail> result = schoolService.getSchoolDetailsFromInstituteApi();
	}

	@Test
	public void whenGetSchoolDetailByIdFromInstituteApi_ReturnSchoolDetail() {
		String schoolId = "school-id";
		SchoolDetailEntity schoolDetailEntity = new SchoolDetailEntity();
		schoolDetailEntity.setSchoolId("ID");
		schoolDetailEntity.setDistrictId("DistID");
		schoolDetailEntity.setSchoolNumber("12345");
		schoolDetailEntity.setSchoolCategoryCode("SCC");
		schoolDetailEntity.setEmail("abc@xyz.ca");

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
		when(this.responseSpecMock.bodyToMono(new ParameterizedTypeReference<SchoolDetailEntity>(){}))
				.thenReturn(Mono.just(schoolDetailEntity));

		SchoolDetail result = schoolService.getSchoolDetailByIdFromInstituteApi(schoolId);
	}

	@Test
	public void whenLoadSchoolDetailsIntoRedisCache_DoesNotThrow() {
		List<SchoolDetailEntity> schoolDetailEntities = Arrays.asList(new SchoolDetailEntity());
		List<SchoolDetail> schoolDetails = Arrays.asList(new SchoolDetail());
		when(this.schoolDetailRedisRepository.saveAll(schoolDetailEntities))
				.thenReturn(schoolDetailEntities);
		assertDoesNotThrow(() -> schoolService.loadSchoolDetailsIntoRedisCache(schoolDetails));
	}

	@Test
	public void whenInitializeSchoolDetailCache_DoNothing() {
		doNothing().when(serviceHelperMock).initializeCache(false, CacheKey.SCHOOL_DETAIL_CACHE, serviceHelperMock);
		schoolService.initializeSchoolDetailCache(false);
	}
}
