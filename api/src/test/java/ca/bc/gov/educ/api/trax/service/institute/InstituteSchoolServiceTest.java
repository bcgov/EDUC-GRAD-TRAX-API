package ca.bc.gov.educ.api.trax.service.institute;

import ca.bc.gov.educ.api.trax.messaging.NatsConnection;
import ca.bc.gov.educ.api.trax.messaging.jetstream.Publisher;
import ca.bc.gov.educ.api.trax.messaging.jetstream.Subscriber;
import ca.bc.gov.educ.api.trax.model.dto.ResponseObj;
import ca.bc.gov.educ.api.trax.model.dto.institute.District;
import ca.bc.gov.educ.api.trax.model.dto.institute.School;
import ca.bc.gov.educ.api.trax.model.entity.institute.DistrictContactEntity;
import ca.bc.gov.educ.api.trax.model.entity.institute.DistrictEntity;
import ca.bc.gov.educ.api.trax.model.entity.institute.SchoolEntity;
import ca.bc.gov.educ.api.trax.model.transformer.institute.DistrictTransformer;
import ca.bc.gov.educ.api.trax.model.transformer.institute.SchoolTransformer;
import ca.bc.gov.educ.api.trax.repository.GradCountryRepository;
import ca.bc.gov.educ.api.trax.repository.GradProvinceRepository;
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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyString;
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
	private SchoolRedisRepository schoolRedisRepository;
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
	private List<School> schoolsMock;
	@MockBean
	private RestUtils restUtilsMock;
	@MockBean
	private SchoolTransformer schoolTransformerMock;

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

		ResponseObj tokenObj = new ResponseObj();
		tokenObj.setAccess_token("123");

		when(webClientMock.get())
				.thenReturn(requestHeadersUriSpecMock);
		when(requestHeadersUriSpecMock.uri(anyString()))
				.thenReturn(requestHeadersSpecMock);
		when(this.restUtilsMock.getTokenResponseObject(anyString(), anyString()))
				.thenReturn(tokenObj);
		when(this.responseObjectMock.getAccess_token())
				.thenReturn("AccessToken");
		when(this.requestHeadersSpecMock.retrieve())
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
}
