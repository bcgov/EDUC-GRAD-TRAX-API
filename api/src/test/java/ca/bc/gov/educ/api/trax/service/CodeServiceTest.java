package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.messaging.NatsConnection;
import ca.bc.gov.educ.api.trax.messaging.jetstream.Publisher;
import ca.bc.gov.educ.api.trax.messaging.jetstream.Subscriber;
import ca.bc.gov.educ.api.trax.model.dto.GradCountry;
import ca.bc.gov.educ.api.trax.model.dto.GradProvince;
import ca.bc.gov.educ.api.trax.model.entity.GradCountryEntity;
import ca.bc.gov.educ.api.trax.model.entity.GradProvinceEntity;
import ca.bc.gov.educ.api.trax.repository.GradCountryRepository;
import ca.bc.gov.educ.api.trax.repository.GradProvinceRepository;
import ca.bc.gov.educ.api.trax.util.GradValidation;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@SuppressWarnings({"rawtypes"})
public class CodeServiceTest {

	@Autowired
	private CodeService codeService;

	@MockBean
	private GradCountryRepository gradCountryRepository;

	@MockBean
	private GradProvinceRepository gradProvinceRepository;

	@Autowired
	GradValidation validation;

	// NATS
	@MockBean
	private NatsConnection natsConnection;

	@MockBean
	private Publisher publisher;

	@MockBean
	private Subscriber subscriber;
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
	
	@Test
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
	}
}
