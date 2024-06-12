package ca.bc.gov.educ.api.trax.service.institute;

import ca.bc.gov.educ.api.trax.messaging.NatsConnection;
import ca.bc.gov.educ.api.trax.messaging.jetstream.Publisher;
import ca.bc.gov.educ.api.trax.messaging.jetstream.Subscriber;
import ca.bc.gov.educ.api.trax.model.dto.ResponseObj;
import ca.bc.gov.educ.api.trax.model.dto.institute.District;
import ca.bc.gov.educ.api.trax.model.dto.institute.DistrictContact;
import ca.bc.gov.educ.api.trax.model.entity.institute.DistrictContactEntity;
import ca.bc.gov.educ.api.trax.model.entity.institute.DistrictEntity;
import ca.bc.gov.educ.api.trax.model.entity.institute.SchoolCategoryCodeEntity;
import ca.bc.gov.educ.api.trax.model.entity.institute.SchoolFundingGroupCodeEntity;
import ca.bc.gov.educ.api.trax.model.transformer.institute.DistrictTransformer;
import ca.bc.gov.educ.api.trax.repository.GradCountryRepository;
import ca.bc.gov.educ.api.trax.repository.GradProvinceRepository;
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
	private GradCountryRepository gradCountryRepository;

	@MockBean
	private GradProvinceRepository gradProvinceRepository;

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
	private RestUtils restUtilsMock;
	@MockBean
	private DistrictTransformer districtTransformerMock;

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
	public void whenGetDistrictsFromInstituteApi_returnsListOfDistricts() {
		List<DistrictEntity> districts = new ArrayList<>();
		DistrictEntity district = new DistrictEntity();

		district.setDistrictId("ID");
		district.setDistrictNumber("1234");
		district.setDistrictStatusCode("SC");
		district.setDistrictRegionCode("RC");
		district.setContacts(Arrays.asList(new DistrictContactEntity(), new DistrictContactEntity()));

		districts.add(district);

		ResponseObj tokenObj = new ResponseObj();
		tokenObj.setAccess_token("123");

		when(webClientMock.get())
				.thenReturn(requestHeadersUriSpecMock);
		when(requestHeadersUriSpecMock.uri(anyString()))
				.thenReturn(requestHeadersSpecMock);
		when(this.restUtilsMock.getTokenResponseObject(anyString(), anyString()))
				.thenReturn(tokenObj);
		when(this.responseObjectMock.getAccess_token())
				.thenReturn("bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJtbUhsTG4tUFlpdTl3MlVhRnh5Yk5nekQ3d2ZIb3ZBRFhHSzNROTk0cHZrIn0.eyJleHAiOjE3MTgxNzU4OTMsImlhdCI6MTcxODE3NTU5MywianRpIjoiMjRjNTc3NWItNzliMi00NDY3LWE3MDUtNWQwZWNjNjgxYzllIiwiaXNzIjoiaHR0cHM6Ly9zb2FtLWRldi5hcHBzLnNpbHZlci5kZXZvcHMuZ292LmJjLmNhL2F1dGgvcmVhbG1zL21hc3RlciIsImF1ZCI6ImFjY291bnQiLCJzdWIiOiI3NmY1NGMzZi01YzQ3LTRjYTQtYWMxYy04MGQ2MWQ2ZGEwMjUiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJlZHgtZ3JhZC1hcGktc2VydmljZSIsImFjciI6IjEiLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsib2ZmbGluZV9hY2Nlc3MiLCJ1bWFfYXV0aG9yaXphdGlvbiJdfSwicmVzb3VyY2VfYWNjZXNzIjp7ImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoiUkVBRF9JTlNUSVRVVEVfQ09ERVMgUkVBRF9ESVNUUklDVCBSRUFEX0NPTExFQ1RJT05fQ09ERVMgUkVBRF9TQ0hPT0wgcHJvZmlsZSBlbWFpbCIsImNsaWVudElkIjoiZWR4LWdyYWQtYXBpLXNlcnZpY2UiLCJjbGllbnRIb3N0IjoiMTk4LjUzLjEzNy40OSIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwicHJlZmVycmVkX3VzZXJuYW1lIjoic2VydmljZS1hY2NvdW50LWVkeC1ncmFkLWFwaS1zZXJ2aWNlIiwiY2xpZW50QWRkcmVzcyI6IjE5OC41My4xMzcuNDkifQ.WhtXQre2HFObmunUU9iYyVPCRZ7y8hFXtKBqAf6rP_ePXrRaVHDuTcVvRBtzJTdfs4-eSJ74YeDXsfpAHt6VneJyBzkqhkxsVc71rmqRDT9AX5zGHy-8mzg5fOI0FGwzxzkHRKI2phHDaCYPTeT2MST_5IDzazehDLeltkDqGwwQLP-ZPabbInoAeoBJHwI2vB-0GBEbumqav_wykcTapnYbMUdYID5rgt1tcKtjM8fXpj32LA_F6_eR1HdHTgAHX9C2sOR_Rygw_pmWrJpqzHX4wyx1pyY-PrN71Pc6i8Vl9YYYUgyH2DVYa8xnrhx_gd-eoZDpegROTMQdKV7nXg");
		when(this.requestHeadersSpecMock.retrieve())
				.thenReturn(responseSpecMock);
		when(this.responseSpecMock.bodyToMono(new ParameterizedTypeReference<List<DistrictEntity>>(){}))
				.thenReturn(districtEntitiesMock);
		when(this.districtEntitiesMock.block()).thenReturn(districts);

		when(this.districtTransformerMock.transformToDTO(districts))
				.thenReturn(districtsMock);

		List<District> result = districtService.getDistrictsFromInstituteApi();
	}

	/*@Test
	public void whenGetSchoolFundingGroupCodesFromInstituteApi_returnsListOfSchoolFundingGroupCodeEntity() {
		List<SchoolFundingGroupCodeEntity> schoolFundingGroupCodes = new ArrayList<>();
		SchoolFundingGroupCodeEntity sfgc = new SchoolFundingGroupCodeEntity();

		sfgc.setSchoolFundingGroupCode("CODE");
		sfgc.setDescription("Description");
		sfgc.setLabel("Label");
		sfgc.setEffectiveDate("01-01-2024");
		sfgc.setExpiryDate("01-01-2024");
		sfgc.setDisplayOrder("10");
		schoolFundingGroupCodes.add(sfgc);

		ResponseObj tokenObj = new ResponseObj();
		tokenObj.setAccess_token("123");

		ParameterizedTypeReference<List<SchoolFundingGroupCodeEntity>> schoolFundingGroupCodeEntityType =
				new ParameterizedTypeReference<List<SchoolFundingGroupCodeEntity>>() {};

		when(webClientMock.get())
				.thenReturn(this.requestHeadersUriSpecMock);
		when(requestHeadersUriSpecMock.uri(anyString()))
				.thenReturn(this.requestHeadersSpecMock);
		when(this.restUtilsMock.getTokenResponseObject(anyString(), anyString()))
				.thenReturn(tokenObj);
		when(this.responseObjectMock.getAccess_token())
				.thenReturn("accessToken");
		when(this.requestHeadersSpecMock.headers(any(Consumer.class)))
				.thenReturn(this.requestHeadersSpecMock);
		when(requestHeadersSpecMock.retrieve())
				.thenReturn(this.responseSpecMock);
		when(this.responseSpecMock.bodyToMono(schoolFundingGroupCodeEntityType))
				.thenReturn(Mono.just(schoolFundingGroupCodes));
		when(this.schoolFundingGroupCodeEntitiesMock.block())
				.thenReturn(schoolFundingGroupCodes);

		List<SchoolFundingGroupCodeEntity> result = codeService.getSchoolFundingGroupCodesFromInstituteApi();
		//assertThat(result).hasSize(1);
	}*/

}
