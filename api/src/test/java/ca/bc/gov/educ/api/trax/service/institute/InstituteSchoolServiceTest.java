package ca.bc.gov.educ.api.trax.service.institute;

import ca.bc.gov.educ.api.trax.constant.CacheKey;
import ca.bc.gov.educ.api.trax.model.dto.ResponseObj;
import ca.bc.gov.educ.api.trax.model.dto.institute.School;
import ca.bc.gov.educ.api.trax.model.dto.institute.SchoolDetail;
import ca.bc.gov.educ.api.trax.model.entity.institute.*;
import ca.bc.gov.educ.api.trax.model.transformer.institute.SchoolDetailTransformer;
import ca.bc.gov.educ.api.trax.model.transformer.institute.SchoolTransformer;
import ca.bc.gov.educ.api.trax.repository.redis.SchoolDetailRedisRepository;
import ca.bc.gov.educ.api.trax.repository.redis.SchoolRedisRepository;
import ca.bc.gov.educ.api.trax.service.RESTService;
import ca.bc.gov.educ.api.trax.util.EducGradTraxApiConstants;
import ca.bc.gov.educ.api.trax.util.RestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import redis.clients.jedis.JedisCluster;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"unchecked","rawtypes"})
class InstituteSchoolServiceTest {

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
	private RESTService restServiceMock;
	@MockBean
	private SchoolTransformer schoolTransformerMock;
	@Autowired
	private SchoolTransformer schoolTransformer;
	@MockBean
	private SchoolDetailTransformer schoolDetailTransformerMock;
	@Autowired
	private SchoolDetailTransformer schoolDetailTransformer;

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
	void whenGetSchoolsFromInstituteApi_returnsListOfSchools() {
		List<SchoolEntity> schoolEntities = new ArrayList<>();
		SchoolEntity schoolEntity = new SchoolEntity();

		schoolEntity.setSchoolId("ID");
		schoolEntity.setDistrictId("DistID");
		schoolEntity.setSchoolNumber("12345");
		schoolEntity.setSchoolCategoryCode("SCC");
		schoolEntity.setEmail("abc@xyz.ca");
		schoolEntity.setDisplayName("Tk̓emlúps te Secwépemc");
		schoolEntity.setDisplayNameNoSpecialChars("Tkkemlups te Secwepemc");
		schoolEntities.add(schoolEntity);

		List<School> schools = new ArrayList<>();
		School school = new School();
		school.setSchoolId("ID");
		school.setDistrictId("DistID");
		school.setSchoolNumber("12345");
		school.setSchoolCategoryCode("SCC");
		school.setEmail("abc@xyz.ca");
		school.setDisplayName("Tk̓emlúps te Secwépemc");
		school.setDisplayNameNoSpecialChars("Tkkemlups te Secwepemc");
		schools.add(school);

		when(this.schoolTransformer.transformToDTO(schoolEntities)).thenReturn(schools);
		when(this.restServiceMock.get(constants.getAllSchoolsFromInstituteApiUrl(),
				List.class, instWebClient)).thenReturn(schoolEntities);

		List<School> result = schoolService.getSchoolsFromInstituteApi();
		assertEquals(schools, result);
	}

	@Test
	void whenLoadSchoolsIntoRedisCache_DoesNotThrow() {
		List<SchoolEntity> schoolEntities = Arrays.asList(new SchoolEntity());
		List<School> schools = Arrays.asList(new School());
		when(this.schoolRedisRepository.saveAll(schoolEntities))
				.thenReturn(schoolEntities);
		assertDoesNotThrow(() -> schoolService.loadSchoolsIntoRedisCache(schools));
	}

	@Test
	void whenGetSchoolsFromRedisCache_ReturnSchools() {
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
	void whenGetSchoolByMincodeFromRedisCache_ReturnSchool() {
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
				.thenReturn(Optional.of(schoolEntity));
		when(this.schoolTransformer.transformToDTO(schoolEntity))
				.thenReturn(school);
		assertEquals(school, schoolService.getSchoolByMinCodeFromRedisCache(mincode));
	}

	@Test
	void whenGetSchoolDetailsByDistrictFromRedisCache_ReturnSchools() {
		String districtId = "DistID";
		String mincode = "12345678";
		SchoolDetail schoolDetail = new SchoolDetail();
		schoolDetail.setSchoolId("ID");
		schoolDetail.setDistrictId(districtId);
		schoolDetail.setSchoolNumber("12345");
		schoolDetail.setMincode(mincode);
		schoolDetail.setSchoolCategoryCode("SCC");
		schoolDetail.setEmail("abc@xyz.ca");

		List<SchoolDetail> schoolDetails = new ArrayList<>();
		schoolDetails.add(schoolDetail);

		SchoolDetailEntity schoolDetailEntity = new SchoolDetailEntity();
		schoolDetailEntity.setSchoolId("ID");
		schoolDetailEntity.setDistrictId(districtId);
		schoolDetailEntity.setSchoolNumber("12345");
		schoolDetailEntity.setMincode(mincode);
		schoolDetailEntity.setSchoolCategoryCode("SCC");
		schoolDetailEntity.setEmail("abc@xyz.ca");

		List<SchoolDetailEntity> schoolDetailEntities = new ArrayList<>();
		schoolDetailEntities.add(schoolDetailEntity);

		when(this.schoolDetailRedisRepository.findByDistrictId(districtId))
				.thenReturn(List.of(schoolDetailEntity));
		when(this.schoolDetailTransformer.transformToDTO(schoolDetailEntities))
				.thenReturn(schoolDetails);
		assertEquals(schoolDetails, schoolService.getSchoolDetailsByDistrictFromRedisCache(districtId));
	}

	@Test
	void whenCheckIfSchoolExists_returnTrue() {
		String minCode = "12345678";
		SchoolEntity schoolEntity = new SchoolEntity();
		schoolEntity.setSchoolId("ID");
		schoolEntity.setDistrictId("DistID");
		schoolEntity.setSchoolNumber("12345");
		schoolEntity.setMincode(minCode);
		schoolEntity.setSchoolCategoryCode("SCC");
		schoolEntity.setEmail("abc@xyz.ca");

		when(schoolRedisRepository.findByMincode(minCode)).thenReturn(Optional.of(schoolEntity));
		assertEquals(true, schoolService.checkIfSchoolExists(minCode));
	}

	@Test
	void whenCheckIfSchoolExists_returnFalse() {
		String minCode = "12345678";
		when(schoolRedisRepository.findByMincode(minCode)).thenReturn(Optional.empty());
		assertEquals(false, schoolService.checkIfSchoolExists(minCode));
	}

	@Test
	void whenInitializeSchoolCache_DoNothing() {
		doNothing().when(serviceHelperMock).initializeCache(false, CacheKey.SCHOOL_CACHE, serviceHelperMock);
		Assertions.assertDoesNotThrow(() -> schoolService.initializeSchoolCache(false));
    }

	@Test
	void whenGetSchoolDetailsFromInstituteApi_returnsListOfSchoolDetails() {
		List<School> schools = new ArrayList<>();
		School school = new School();
		school.setSchoolId("1");
		school.setMincode("234");
		schools.add(school);
		school = new School();
		school.setSchoolId("2");
		school.setMincode("345");
		schools.add(school);

		List<SchoolDetail> schoolDetails = new ArrayList<>();
		SchoolDetail schoolDetail1 = new SchoolDetail();
		schoolDetail1.setSchoolId("1");
		schoolDetail1.setDistrictId("DistID");
		schoolDetail1.setSchoolNumber("12345");
		schoolDetail1.setSchoolCategoryCode("SCC");
		schoolDetail1.setEmail("abc@xyz.ca");
		schoolDetails.add(schoolDetail1);
		SchoolDetail schoolDetail2 = new SchoolDetail();
		schoolDetail2.setSchoolId("2");
		schoolDetail2.setDistrictId("DistID");
		schoolDetail2.setSchoolNumber("12345");
		schoolDetail2.setSchoolCategoryCode("SCC");
		schoolDetail2.setEmail("abc@xyz.ca");
		schoolDetails.add(schoolDetail2);

		SchoolDetailEntity schoolDetailEntity1 = new SchoolDetailEntity();
		schoolDetailEntity1.setSchoolId("1");
		schoolDetailEntity1.setDistrictId("DistID");
		schoolDetailEntity1.setSchoolNumber("12345");
		schoolDetailEntity1.setSchoolCategoryCode("SCC");
		schoolDetailEntity1.setEmail("abc@xyz.ca");
		SchoolDetailEntity schoolDetailEntity2 = new SchoolDetailEntity();
		schoolDetailEntity2.setSchoolId("2");
		schoolDetailEntity2.setDistrictId("DistID");
		schoolDetailEntity2.setSchoolNumber("12345");
		schoolDetailEntity2.setSchoolCategoryCode("SCC");
		schoolDetailEntity2.setEmail("abc@xyz.ca");

		when(this.schoolService.getSchoolsFromRedisCache()).thenReturn(schools);
		when(this.schoolDetailTransformer.transformToDTO(schoolDetailEntity1)).thenReturn(schoolDetail1);
		when(this.schoolDetailTransformer.transformToDTO(schoolDetailEntity2)).thenReturn(schoolDetail2);
		when(this.restServiceMock.get(String.format(constants.getSchoolDetailsByIdFromInstituteApiUrl(), "1"),
				SchoolDetailEntity.class, instWebClient)).thenReturn(schoolDetailEntity1);
		when(this.restServiceMock.get(String.format(constants.getSchoolDetailsByIdFromInstituteApiUrl(), "2"),
				SchoolDetailEntity.class, instWebClient)).thenReturn(schoolDetailEntity2);

		List<SchoolDetail> result = schoolService.getSchoolDetailsFromInstituteApi();
		assertEquals(schoolDetails, result);
	}

	@Test
	void whenGetSchoolDetailByIdFromInstituteApi_ReturnSchoolDetail() {
		String schoolId = "1";

		SchoolDetail schoolDetail = new SchoolDetail();
		schoolDetail.setSchoolId("1");
		schoolDetail.setDistrictId("DistID");
		schoolDetail.setSchoolNumber("12345");
		schoolDetail.setSchoolCategoryCode("SCC");
		schoolDetail.setEmail("abc@xyz.ca");
		schoolDetail.setDisplayName("Stitó:s Lá:lém Totí:lt Elementary");
		schoolDetail.setDisplayNameNoSpecialChars("Stitos Lalem Totilt Elementary");

		SchoolDetailEntity schoolDetailEntity = new SchoolDetailEntity();
		schoolDetailEntity.setSchoolId("1");
		schoolDetailEntity.setDistrictId("DistID");
		schoolDetailEntity.setSchoolNumber("12345");
		schoolDetailEntity.setSchoolCategoryCode("SCC");
		schoolDetailEntity.setEmail("abc@xyz.ca");
		schoolDetailEntity.setDisplayName("Stitó:s Lá:lém Totí:lt Elementary");
		schoolDetailEntity.setDisplayNameNoSpecialChars("Stitos Lalem Totilt Elementary");

		when(this.schoolDetailTransformer.transformToDTO(schoolDetailEntity)).thenReturn(schoolDetail);
		when(this.restServiceMock.get(String.format(constants.getSchoolDetailsByIdFromInstituteApiUrl(), "1"),
				SchoolDetailEntity.class, instWebClient)).thenReturn(schoolDetailEntity);

		SchoolDetail result = schoolService.getSchoolDetailByIdFromInstituteApi(schoolId);
		assertEquals(schoolDetail, result);
	}

	@Test
	void whenLoadSchoolDetailsIntoRedisCache_DoesNotThrow() {
		List<SchoolDetailEntity> schoolDetailEntities = Arrays.asList(new SchoolDetailEntity());
		List<SchoolDetail> schoolDetails = Arrays.asList(new SchoolDetail());
		when(this.schoolDetailRedisRepository.saveAll(schoolDetailEntities))
				.thenReturn(schoolDetailEntities);
		assertDoesNotThrow(() -> schoolService.loadSchoolDetailsIntoRedisCache(schoolDetails));
	}

	@Test
	void whenInitializeSchoolDetailCache_DoNothing() {
		doNothing().when(serviceHelperMock).initializeCache(false, CacheKey.SCHOOL_DETAIL_CACHE, serviceHelperMock);
		Assertions.assertDoesNotThrow(() -> schoolService.initializeSchoolDetailCache(false));
	}

	@Test
	void whenGetSchoolDetailsFromRedisCache_ReturnSchoolDetails() {
		String mincode = "12345678";
		List<SchoolDetail> schoolDetails = new ArrayList<>();
		SchoolDetail schoolDetail = new SchoolDetail();
		schoolDetail.setSchoolId("ID");
		schoolDetail.setDistrictId("DistID");
		schoolDetail.setSchoolNumber("12345");
		schoolDetail.setMincode(mincode);
		schoolDetail.setSchoolCategoryCode("SCC");
		schoolDetail.setEmail("abc@xyz.ca");
		schoolDetails.add(schoolDetail);

		schoolDetail = new SchoolDetail();
		schoolDetail.setSchoolId("ID");
		schoolDetail.setDistrictId("DistID");
		schoolDetail.setSchoolNumber("12345");
		schoolDetail.setMincode(mincode);
		schoolDetail.setSchoolCategoryCode("SCC");
		schoolDetail.setEmail("abc@xyz.ca");
		schoolDetails.add(schoolDetail);

		List<SchoolDetailEntity> schoolDetailEntities = new ArrayList<>();
		SchoolDetailEntity schoolDetailEntity = new SchoolDetailEntity();
		schoolDetailEntity.setSchoolId("ID");
		schoolDetailEntity.setDistrictId("DistID");
		schoolDetailEntity.setSchoolNumber("12345");
		schoolDetailEntity.setMincode(mincode);
		schoolDetailEntity.setSchoolCategoryCode("SCC");
		schoolDetailEntity.setEmail("abc@xyz.ca");
		schoolDetailEntities.add(schoolDetailEntity);

		schoolDetailEntity = new SchoolDetailEntity();
		schoolDetailEntity.setSchoolId("ID");
		schoolDetailEntity.setDistrictId("DistID");
		schoolDetailEntity.setSchoolNumber("12345");
		schoolDetailEntity.setMincode(mincode);
		schoolDetailEntity.setSchoolCategoryCode("SCC");
		schoolDetailEntity.setEmail("abc@xyz.ca");
		schoolDetailEntities.add(schoolDetailEntity);

		when(this.schoolDetailRedisRepository.findAll())
				.thenReturn(schoolDetailEntities);
		when(this.schoolDetailTransformerMock.transformToDTO(schoolDetailEntities))
				.thenReturn(schoolDetails);
		assertEquals(schoolDetails, schoolService.getSchoolDetailsFromRedisCache());
	}

	@Test
	void whenGetSchoolDetailByMincodeFromRedisCache_ReturnSchoolDetail() {
		String mincode = "12345678";
		SchoolDetail schoolDetail = new SchoolDetail();
		schoolDetail.setSchoolId("ID");
		schoolDetail.setDistrictId("DistID");
		schoolDetail.setSchoolNumber("12345");
		schoolDetail.setMincode(mincode);
		schoolDetail.setSchoolCategoryCode("SCC");
		schoolDetail.setEmail("abc@xyz.ca");

		SchoolDetailEntity schoolDetailEntity = new SchoolDetailEntity();
		schoolDetailEntity.setSchoolId("ID");
		schoolDetailEntity.setDistrictId("DistID");
		schoolDetailEntity.setSchoolNumber("12345");
		schoolDetailEntity.setMincode(mincode);
		schoolDetailEntity.setSchoolCategoryCode("SCC");
		schoolDetailEntity.setEmail("abc@xyz.ca");

		when(this.schoolDetailRedisRepository.findByMincode(mincode))
				.thenReturn(Optional.of(schoolDetailEntity));
		when(this.schoolDetailTransformer.transformToDTO(schoolDetailEntity))
				.thenReturn(schoolDetail);
		assertEquals(schoolDetail, schoolService.getSchoolDetailByMincodeFromRedisCache(mincode));
	}

	@Test
	void whenGetSchoolDetailBySchoolCategoryCode_ReturnSchoolDetail() {
		String schoolCategoryCode = "ABC";
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
		assertEquals(schoolDetails, schoolService.getSchoolDetailsBySchoolCategoryCode(schoolCategoryCode));
	}

	@Test
	void testGetSchoolBySchoolId() {
		UUID schoolId = UUID.randomUUID();
		SchoolEntity schoolEntity = new SchoolEntity();
		schoolEntity.setSchoolId(schoolId.toString());
		schoolEntity.setMincode("1234567");

		School school = new School();
		school.setSchoolId(schoolId.toString());
		school.setMincode("1234567");

		Mockito.when(schoolRedisRepository.findById(schoolId.toString())).thenReturn(Optional.of(schoolEntity));
		Mockito.when(schoolTransformer.transformToDTO(schoolEntity)).thenReturn(school);

		Optional<School> result = schoolService.getSchoolBySchoolId(schoolId);

		assertTrue(result.isPresent());
		assertEquals(schoolId.toString(), result.get().getSchoolId());
		assertEquals("1234567", result.get().getMincode());
	}

	@Test
	void testGetSchoolBySchoolId_NotFound() {
		UUID schoolId = UUID.randomUUID();

		Mockito.when(schoolRedisRepository.findById(schoolId.toString())).thenReturn(Optional.empty());

		Optional<School> result = schoolService.getSchoolBySchoolId(schoolId);

		assertFalse(result.isPresent());
	}

	@Test
	void testGetSchoolsByParams() {
		String districtId = UUID.randomUUID().toString();
		String mincode = "1234567";
		String displayName = "ABC Elementary School";
		SchoolEntity schoolEntity = new SchoolEntity();
		schoolEntity.setSchoolId(UUID.randomUUID().toString());
		schoolEntity.setMincode(mincode);
		schoolEntity.setDistrictId(districtId);
		schoolEntity.setDisplayName(displayName);

		School school = new School();
		school.setSchoolId(schoolEntity.getSchoolId());
		school.setMincode(mincode);
		school.setDistrictId(districtId);
		school.setDisplayName(displayName);

		Mockito.when(schoolRedisRepository.findAll()).thenReturn(List.of(schoolEntity));
		Mockito.when(schoolTransformer.transformToDTO(List.of(schoolEntity))).thenReturn(List.of(school));
		Mockito.when(schoolTransformer.transformToDTO(schoolEntity)).thenReturn(school);

		// Test case when both districtId and mincode are null
		List<School> result = schoolService.getSchoolsByParams(null, null, null, null);
		assertNotNull(result);
		assertEquals(1, result.size());

		// Test case when mincode is null
		result = schoolService.getSchoolsByParams(districtId, null, displayName, "12*");
		assertNotNull(result);
		assertEquals(1, result.size());

		// Test case when districtId is null
		result = schoolService.getSchoolsByParams(null, mincode, "ABC*", "12*");
		assertNotNull(result);
		assertEquals(1, result.size());

		// Test case when both districtId and mincode are provided
		result = schoolService.getSchoolsByParams(districtId, mincode, "ABC*", "12*");
		assertNotNull(result);
		assertEquals(1, result.size());
	}

	@Test
	void testGetSchoolsByParams_EmptyResults() {
		String districtId = UUID.randomUUID().toString();
		String mincode = "1234567";
		String displayName = "ABC Elementary School";

		Mockito.when(schoolRedisRepository.findAll()).thenReturn(Collections.emptyList());

		// Test case when both districtId and mincode are null
		List<School> result = schoolService.getSchoolsByParams(null, null, null, null);
		assertNotNull(result);
		assertTrue(result.isEmpty());

		// Test case when mincode is null
		result = schoolService.getSchoolsByParams(districtId, null, displayName, "12*");
		assertNotNull(result);
		assertTrue(result.isEmpty());

		// Test case when districtId is null
		result = schoolService.getSchoolsByParams(null, mincode, "ABC*", "12*");
		assertNotNull(result);
		assertTrue(result.isEmpty());

		// Test case when both districtId and mincode are provided
		result = schoolService.getSchoolsByParams(districtId, mincode, "ABC*", "12*");
		assertNotNull(result);
		assertTrue(result.isEmpty());
	}
}
