package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.messaging.NatsConnection;
import ca.bc.gov.educ.api.trax.messaging.jetstream.Publisher;
import ca.bc.gov.educ.api.trax.messaging.jetstream.Subscriber;
import ca.bc.gov.educ.api.trax.model.dto.SnapshotResponse;
import ca.bc.gov.educ.api.trax.repository.SnapshotRepository;
import ca.bc.gov.educ.api.trax.util.EducGradTraxApiConstants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
import redis.clients.jedis.JedisCluster;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class EdwServiceTest {

    @Autowired
    private EdwService edwService;

    @MockBean
    private SnapshotRepository snapshotRepository;

    @Autowired
    private EducGradTraxApiConstants constants;

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
    public void testGetUniqueSchoolList() {
        Integer gradYear = 2023;
        List<String> schoolList = Arrays.asList("12345678", "87654321");

        when(this.snapshotRepository.getSchools(gradYear)).thenReturn(schoolList);

        var result = edwService.getUniqueSchoolList(gradYear);

        assertThat(result).hasSize(2);
    }

    @Test
    public void testGetStudents() {
        Integer gradYear = 2023;
        // graduated student
        SnapshotResponse snapshot1 = new SnapshotResponse();
        snapshot1.setPen("123456789");
        snapshot1.setGraduatedDate("202306");
        snapshot1.setGpa(BigDecimal.valueOf(3.60));
        // non-graduated student
        SnapshotResponse snapshot2 = new SnapshotResponse();
        snapshot1.setPen("111222333");

        List<SnapshotResponse> snapshots = Arrays.asList(snapshot1, snapshot2);

        when(this.snapshotRepository.getStudentsByGradYear(gradYear)).thenReturn(snapshots);

        var result = edwService.getStudents(gradYear);

        assertThat(result).hasSize(2);
    }

    @Test
    public void testGetStudentsByGradYearAndSchool() {
        Integer gradYear = 2023;
        String minCode = "12345678";
        // graduated student
        SnapshotResponse snapshot1 = new SnapshotResponse();
        snapshot1.setPen("123456789");
        snapshot1.setGraduatedDate("202306");
        snapshot1.setGpa(BigDecimal.valueOf(3.60));
        // non-graduated student
        SnapshotResponse snapshot2 = new SnapshotResponse();
        snapshot1.setPen("111222333");

        List<SnapshotResponse> snapshots = Arrays.asList(snapshot1, snapshot2);

        when(this.snapshotRepository.getStudentsByGradYearAndSchoolOfRecord(gradYear, minCode)).thenReturn(snapshots);

        var result = edwService.getStudents(gradYear, minCode);

        assertThat(result).hasSize(2);
    }


}
