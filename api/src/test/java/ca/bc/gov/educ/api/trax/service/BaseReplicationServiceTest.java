package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.EducGradTraxApiApplication;
import ca.bc.gov.educ.api.trax.messaging.NatsConnection;
import ca.bc.gov.educ.api.trax.messaging.jetstream.Publisher;
import ca.bc.gov.educ.api.trax.messaging.jetstream.Subscriber;
import ca.bc.gov.educ.api.trax.util.ReplicationTestUtils;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import redis.clients.jedis.JedisCluster;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {EducGradTraxApiApplication.class})
@ActiveProfiles("test")
@AutoConfigureMockMvc
public abstract class BaseReplicationServiceTest {

    @Autowired
    protected ReplicationTestUtils replicationTestUtils;

    @MockBean
    public Publisher publisher;

    @MockBean
    public Subscriber subscriber;

    @MockBean
    public NatsConnection natsConnection;

    @MockBean
    public ClientRegistrationRepository clientRegistrationRepository;

    @MockBean
    public OAuth2AuthorizedClientRepository oAuth2AuthorizedClientRepository;
    @MockBean
    public OAuth2AuthorizedClientService oAuth2AuthorizedClientService;
    @MockBean
    private JedisConnectionFactory jedisConnectionFactoryMock;
    @MockBean
    private JedisCluster jedisClusterMock;

    @Before
    public void resetState() {
        this.replicationTestUtils.cleanDB();
    }

}
