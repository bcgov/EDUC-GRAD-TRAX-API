package ca.bc.gov.educ.api.trax.service.institute;

import ca.bc.gov.educ.api.trax.messaging.NatsConnection;
import ca.bc.gov.educ.api.trax.messaging.jetstream.Publisher;
import ca.bc.gov.educ.api.trax.messaging.jetstream.Subscriber;
import ca.bc.gov.educ.api.trax.model.dto.ResponseObj;
import ca.bc.gov.educ.api.trax.model.dto.institute.School;
import ca.bc.gov.educ.api.trax.model.dto.institute.SchoolDetail;
import ca.bc.gov.educ.api.trax.model.entity.institute.SchoolDetailEntity;
import ca.bc.gov.educ.api.trax.model.entity.institute.SchoolEntity;
import ca.bc.gov.educ.api.trax.model.transformer.institute.SchoolDetailTransformer;
import ca.bc.gov.educ.api.trax.model.transformer.institute.SchoolTransformer;
import ca.bc.gov.educ.api.trax.repository.redis.SchoolDetailRedisRepository;
import ca.bc.gov.educ.api.trax.repository.redis.SchoolRedisRepository;
import ca.bc.gov.educ.api.trax.service.RESTService;
import ca.bc.gov.educ.api.trax.support.TestUtils;
import ca.bc.gov.educ.api.trax.util.EducGradTraxApiConstants;
import ca.bc.gov.educ.api.trax.util.RestUtils;
import org.junit.Assert;
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
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import redis.clients.jedis.JedisCluster;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class intstituteSchoolServiceTestToo {

    @Autowired
    private EducGradTraxApiConstants constants;
    @MockBean
    private SchoolService schoolService;
    @MockBean
    private SchoolRedisRepository schoolRedisRepository;
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
    private SchoolDetailTransformer schoolDetailTransformer;
    @MockBean
    private RESTService restServiceMock;
    @MockBean
    private SchoolDetailRedisRepository schoolDetailRedisRepository;
    // NATS
    @MockBean
    private NatsConnection natsConnection;

    @MockBean
    private Publisher publisher;

    @MockBean
    private Subscriber subscriber;
    @Autowired
    private ca.bc.gov.educ.api.trax.model.transformer.SchoolTransformer schoolTransformer;

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
    public void updateSchoolCache_givenValidShcoolId_shouldUpdate() {
        SchoolDetail schoolDetail = TestUtils.createSchoolDetail();
        SchoolDetailEntity schoolDetailEntity = schoolDetailTransformer.transformToEntity(schoolDetail);
        Optional<SchoolDetailEntity> schoolDetailEntityOptional = Optional.of(schoolDetailEntity);
        Optional<SchoolEntity> schoolEntityOptional = Optional.of(schoolDetailTransformer.transformToSchoolEntity(schoolDetail));
        when(this.restServiceMock.get(String.format(constants.getSchoolDetailsByIdFromInstituteApiUrl(), schoolDetail.getSchoolId()),
                SchoolDetail.class, this.webClientMock)).thenReturn(schoolDetail);
        when(this.schoolDetailRedisRepository.findById(schoolDetail.getSchoolId())).thenReturn(schoolDetailEntityOptional);
        when(this.schoolRedisRepository.findById(schoolDetail.getSchoolId())).thenReturn(schoolEntityOptional);
        doNothing().when(this.schoolService).updateSchoolCache(schoolDetail.getSchoolId());
        this.schoolService.updateSchoolCache(schoolDetail.getSchoolId());
        Optional<SchoolDetailEntity> response = this.schoolDetailRedisRepository.findById(schoolDetail.getSchoolId());
        Optional<SchoolEntity> schoolResponse = this.schoolRedisRepository.findById(schoolDetail.getSchoolId());
        if (response.isPresent()) {
            Assert.assertEquals(response.get().getSchoolId(), schoolResponse.get().getSchoolId());
        } else {
            Assert.fail();
        }
    }

    @Test
    public void updateSchoolCache_givenValidShcoolIdList_shouldUpdate() {
        SchoolDetail schoolDetail1 = TestUtils.createSchoolDetail();
        SchoolDetail schoolDetail2 = TestUtils.createSchoolDetail();
        SchoolDetailEntity schoolDetailEntity1 = schoolDetailTransformer.transformToEntity(schoolDetail1);
        SchoolDetailEntity schoolDetailEntity2 = schoolDetailTransformer.transformToEntity(schoolDetail2);
        Optional<SchoolDetailEntity> schoolDetailEntityOptional1 = Optional.of(schoolDetailEntity1);
        Optional<SchoolDetailEntity> schoolDetailEntityOptional2 = Optional.of(schoolDetailEntity2);
        when(this.restServiceMock.get(String.format(constants.getSchoolDetailsByIdFromInstituteApiUrl(), schoolDetail1.getSchoolId()),
                SchoolDetail.class, this.webClientMock)).thenReturn(schoolDetail1);
        when(this.restServiceMock.get(String.format(constants.getSchoolDetailsByIdFromInstituteApiUrl(), schoolDetail2.getSchoolId()),
                SchoolDetail.class, this.webClientMock)).thenReturn(schoolDetail2);
        when(this.schoolDetailRedisRepository.findById(schoolDetail1.getSchoolId())).thenReturn(schoolDetailEntityOptional1);
        when(this.schoolDetailRedisRepository.findById(schoolDetail2.getSchoolId())).thenReturn(schoolDetailEntityOptional2);
        List<String> schoolIds = Arrays.asList(schoolDetail1.getSchoolId(), schoolDetail2.getSchoolId());
        doNothing().when(this.schoolService).updateSchoolCache(schoolIds);
        this.schoolService.updateSchoolCache(schoolIds);
        Optional<SchoolDetailEntity> response1 = this.schoolDetailRedisRepository.findById(schoolDetail1.getSchoolId());
        Optional<SchoolDetailEntity> response2 = this.schoolDetailRedisRepository.findById(schoolDetail2.getSchoolId());
        if (response1.isPresent() && response2.isPresent()) {
            Assert.assertTrue(response1.get().getSchoolId() == schoolDetail1.getSchoolId() && response2.get().getSchoolId() == schoolDetail2.getSchoolId());
        } else {
            Assert.fail();
        }

    }

}
