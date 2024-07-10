package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.messaging.NatsConnection;
import ca.bc.gov.educ.api.trax.messaging.jetstream.Publisher;
import ca.bc.gov.educ.api.trax.messaging.jetstream.Subscriber;
import ca.bc.gov.educ.api.trax.model.dto.GradCountry;
import ca.bc.gov.educ.api.trax.model.dto.GradProvince;
import ca.bc.gov.educ.api.trax.model.dto.Psi;
import ca.bc.gov.educ.api.trax.model.dto.StudentPsi;
import ca.bc.gov.educ.api.trax.model.entity.PsiEntity;
import ca.bc.gov.educ.api.trax.repository.PsiRepository;
import ca.bc.gov.educ.api.trax.repository.PsiSearchCriteria;
import ca.bc.gov.educ.api.trax.repository.PsiSearchSpecification;
import ca.bc.gov.educ.api.trax.repository.StudentPsiRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import redis.clients.jedis.JedisCluster;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;


@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class PsiServiceTest {

    @Autowired
    private PsiService psiService;

    @MockBean
    private PsiRepository psiRepository;

	@MockBean
	private StudentPsiRepository studentPsiRepository;

    @MockBean
    private CodeService codeService;

	// NATS
	@MockBean
	private NatsConnection natsConnection;

	@MockBean
	private Publisher publisher;

	@MockBean
	private Subscriber subscriber;
	@MockBean
	private JedisConnectionFactory jedisConnectionFactoryMock;
	@MockBean
	private JedisCluster jedisClusterMock;

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

    @Before
    public void setUp() {
        openMocks(this);
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testGetPSIList() {
        // psi data
    	List<PsiEntity> gradPSIList = new ArrayList<>();
		PsiEntity obj = new PsiEntity();
		obj.setPsiCode("AB");
		obj.setPsiName("Autobody");
		obj.setAddress1("ABC");
		obj.setAddress2("DEF");
		obj.setAddress3("FGF");
		obj.setAttentionName("XZA");
		obj.setCity("Abbotsford");
		obj.setCountryCode("CDD");
		obj.setCslCode("SW@");
		obj.setFax("23432234234");
		obj.setOpenFlag("Y");
		obj.setPhone1("123213");
		obj.setPostal("V3T1C4");
		gradPSIList.add(obj);
		obj = new PsiEntity();
		obj.setPsiCode("AB");
		obj.setPsiName("Autobody");
		obj.setAddress1("ABC");
		obj.setAddress2("DEF");
		obj.setAddress3("FGF");
		obj.setAttentionName("XZA");
		obj.setCity("Abbotsford");
		obj.setCountryCode("CDD");
		obj.setCslCode("SW@");
		obj.setFax("23432234234");
		obj.setOpenFlag("Y");
		obj.setPhone1("123213");
		obj.setPostal("V3T1C4");
		gradPSIList.add(obj);
		

        when(psiRepository.findAll()).thenReturn(gradPSIList);
        List<Psi> results = psiService.getPSIList();

        assertThat(results).isNotNull();
        assertThat(results.size()).isEqualTo(2);
    }

    @Test
    public void testGetPsiDetails() {
        // School
    	final PsiEntity obj = new PsiEntity();
		obj.setPsiCode("AB");
		obj.setPsiName("Autobody");
		obj.setAddress1("ABC");
		obj.setAddress2("DEF");
		obj.setAddress3("FGF");
		obj.setAttentionName("XZA");
		obj.setCity("Abbotsford");
		obj.setCountryCode("CDD");
		obj.setCslCode("SW@");
		obj.setFax("23432234234");
		obj.setOpenFlag("Y");
		obj.setPhone1("123213");
		obj.setPostal("V3T1C4");
		obj.setProvinceCode("BC");

        // Country
        final GradCountry country = new GradCountry();
        country.setCountryCode("CA");
        country.setCountryName("Canada");

        // Provice
        final GradProvince province = new GradProvince();
        province.setCountryCode("CA");
        province.setProvCode("BC");
        province.setProvName("British Columbia");

        when(psiRepository.findById("AB")).thenReturn(Optional.of(obj));

        when(codeService.getSpecificCountryCode(obj.getCountryCode())).thenReturn(country);
        when(codeService.getSpecificProvinceCode(obj.getProvinceCode())).thenReturn(province);

        var result = psiService.getPSIDetails("AB");

        assertThat(result).isNotNull();
        assertThat(result.getPsiCode()).isEqualTo("AB");
        assertThat(result.getPsiName()).isEqualTo("Autobody");
    }

	@Test
	public void testGetPsiDetails_countryNull() {
		// School
		final PsiEntity obj = new PsiEntity();
		obj.setPsiCode("AB");
		obj.setPsiName("Autobody");
		obj.setAddress1("ABC");
		obj.setAddress2("DEF");
		obj.setAddress3("FGF");
		obj.setAttentionName("XZA");
		obj.setCity("Abbotsford");
		obj.setCountryCode("CDD");
		obj.setCslCode("SW@");
		obj.setFax("23432234234");
		obj.setOpenFlag("Y");
		obj.setPhone1("123213");
		obj.setPostal("V3T1C4");
		obj.setProvinceCode("BC");

		// Provice
		final GradProvince province = new GradProvince();
		province.setCountryCode("CA");
		province.setProvCode("BC");
		province.setProvName("British Columbia");

		when(psiRepository.findById("AB")).thenReturn(Optional.of(obj));

		when(codeService.getSpecificCountryCode(obj.getCountryCode())).thenReturn(null);
		when(codeService.getSpecificProvinceCode(obj.getProvinceCode())).thenReturn(province);

		var result = psiService.getPSIDetails("AB");

		assertThat(result).isNotNull();
		assertThat(result.getPsiCode()).isEqualTo("AB");
		assertThat(result.getPsiName()).isEqualTo("Autobody");
	}

	@Test
	public void testGetPsiDetails_null() {
		// School
		final PsiEntity obj = new PsiEntity();
		obj.setPsiCode("AB");
		obj.setPsiName("Autobody");
		obj.setAddress1("ABC");
		obj.setAddress2("DEF");
		obj.setAddress3("FGF");
		obj.setAttentionName("XZA");
		obj.setCity("Abbotsford");
		obj.setCountryCode("CDD");
		obj.setCslCode("SW@");
		obj.setFax("23432234234");
		obj.setOpenFlag("Y");
		obj.setPhone1("123213");
		obj.setPostal("V3T1C4");

		// Country
		final GradCountry country = new GradCountry();
		country.setCountryCode("CA");
		country.setCountryName("Canada");

		// Provice
		final GradProvince province = new GradProvince();
		province.setCountryCode("CA");
		province.setProvCode("BC");
		province.setProvName("British Columbia");

		when(psiRepository.findById("AB")).thenReturn(Optional.empty());

		when(codeService.getSpecificCountryCode(obj.getCountryCode())).thenReturn(country);
		when(codeService.getSpecificProvinceCode(obj.getProvinceCode())).thenReturn(province);

		var result = psiService.getPSIDetails("AB");

		assertThat(result).isNull();
	}
    
    @Test
    public void testGetPSIsByParamsStar() {
        // School
    	PsiEntity obj = new PsiEntity();
		obj.setPsiCode("AB");
		obj.setPsiName("Autobody");
		obj.setAddress1("ABC ");
		obj.setAddress2("DEF ");
		obj.setAddress3("FGF ");
		obj.setAttentionName("XZA");
		obj.setCity("Abbotsford");
		obj.setCountryCode("CDD");
		obj.setProvinceCode("BC");
		obj.setCslCode("SW@");
		obj.setFax("23432234234");
		obj.setOpenFlag("Y");
		obj.setPhone1("123213 ");
		obj.setPostal("V3T1C4 ");
		List<PsiEntity> list = new ArrayList<>();
		list.add(obj);
        // Country
        GradCountry country = new GradCountry();
        country.setCountryCode("CA");
        country.setCountryName("Canada");

        // Provice
        GradProvince province = new GradProvince();
        province.setCountryCode("CA");
        province.setProvCode("BC");
        province.setProvName("British Columbia");

		PsiSearchCriteria searchCriteria = PsiSearchCriteria.builder()
				.psiCode("AB")
				.psiName("Autobody".toUpperCase(Locale.ROOT))
				.cslCode(null)
				.transmissionMode(null)
				.openFlag(null)
				.build();
		Specification<PsiEntity> spec = new PsiSearchSpecification(searchCriteria);
        when(psiRepository.findAll(Specification.where(spec))).thenReturn(list);

        when(codeService.getSpecificCountryCode(obj.getCountryCode())).thenReturn(country);
        when(codeService.getSpecificProvinceCode(obj.getProvinceCode())).thenReturn(province);

        var result = psiService.getPSIByParams("Autobody", "AB*", null,null,null);
        assertThat(result).isNotNull();
    }

	@Test
	public void testGetPSIsByParamsStar_2() {
		// School
		PsiEntity obj = new PsiEntity();
		obj.setPsiCode("AB");
		obj.setPsiName("Autobody");
		obj.setAddress1("ABC ");
		obj.setAddress2("DEF ");
		obj.setAddress3("FGF ");
		obj.setAttentionName("XZA");
		obj.setCity("Abbotsford");
		obj.setCountryCode("CDD");
		obj.setProvinceCode("BC");
		obj.setCslCode("SW@");
		obj.setFax("23432234234");
		obj.setOpenFlag("Y");
		obj.setPhone1("123213 ");
		obj.setPostal("V3T1C4 ");
		List<PsiEntity> list = new ArrayList<>();
		list.add(obj);
		// Country
		GradCountry country = new GradCountry();
		country.setCountryCode("CA");
		country.setCountryName("Canada");

		// Provice
		GradProvince province = new GradProvince();
		province.setCountryCode("CA");
		province.setProvCode("BC");
		province.setProvName("British Columbia");

		PsiSearchCriteria searchCriteria = PsiSearchCriteria.builder()
				.psiCode("AB")
				.psiName("Autobody".toUpperCase(Locale.ROOT))
				.cslCode("C")
				.transmissionMode("P")
				.openFlag("Y")
				.build();
		Specification<PsiEntity> spec = new PsiSearchSpecification(searchCriteria);
		when(psiRepository.findAll(Specification.where(spec))).thenReturn(list);

		when(codeService.getSpecificCountryCode(obj.getCountryCode())).thenReturn(country);
		when(codeService.getSpecificProvinceCode(obj.getProvinceCode())).thenReturn(province);

		var result = psiService.getPSIByParams("Autobody*", "AB*", "C*","P*","Y");
		assertThat(result).isNotNull();

	}


	@Test
	public void testGetStudentPSIDetails() {
		final String transmissionMode = "paper";
		final String psiYear = "2021";
		final String psiCode="001";
		List<Object[]> results = new ArrayList<>();
		Object[] obj = new Object[5];
		obj[0]="123123131";
		obj[1] = "001";
		obj[2]= "2021";
		obj[3]= "";
		obj[4] = "A";
		results.add(obj);
		Mockito.when(studentPsiRepository.findStudentsUsingPSI(transmissionMode,psiYear,"Yes",List.of(psiCode))).thenReturn(results);

		List<StudentPsi> res = psiService.getStudentPSIDetails(transmissionMode,psiYear,psiCode);
		assertThat(res).isNotNull().hasSize(1);
	}

	@Test
	public void testGetStudentPSIDetails_all() {
		final String transmissionMode = "paper";
		final String psiYear = "2021";
		final String psiCode="all";
		List<Object[]> results = new ArrayList<>();
		Object[] obj = new Object[5];
		obj[0]="123123131";
		obj[1] = "001";
		obj[2]= "2021";
		obj[3]= "";
		obj[4] = "A";
		results.add(obj);
		Mockito.when(studentPsiRepository.findStudentsUsingPSI(transmissionMode,psiYear,null,List.of(psiCode))).thenReturn(results);

		List<StudentPsi> res = psiService.getStudentPSIDetails(transmissionMode,psiYear,psiCode);
		assertThat(res).isNotNull().hasSize(1);
	}
}
