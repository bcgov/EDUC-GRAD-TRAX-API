package ca.bc.gov.educ.api.trax.util;

import ca.bc.gov.educ.api.trax.messaging.NatsConnection;
import ca.bc.gov.educ.api.trax.messaging.jetstream.Publisher;
import ca.bc.gov.educ.api.trax.messaging.jetstream.Subscriber;
import ca.bc.gov.educ.api.trax.model.entity.EventEntity;
import ca.bc.gov.educ.api.trax.model.entity.EventHistoryEntity;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import redis.clients.jedis.JedisCluster;

import java.time.LocalDateTime;
import java.util.UUID;

public abstract class BaseEventHistoryTest {

    protected static final String USER = "TEST";

    @MockBean
    private Publisher publisher;
    @MockBean
    private Subscriber subscriber;
    @MockBean
    private NatsConnection natsConnection;
    @MockBean
    private ClientRegistrationRepository clientRegistrationRepository;
    @MockBean
    private OAuth2AuthorizedClientRepository oAuth2AuthorizedClientRepository;
    @MockBean
    private OAuth2AuthorizedClientService oAuth2AuthorizedClientService;
    @MockBean
    private JedisConnectionFactory jedisConnectionFactoryMock;
    @MockBean
    private JedisCluster jedisClusterMock;

    protected EventEntity createEventData() {
        return EventEntity.builder()
                .eventId(UUID.randomUUID())
                .eventPayload("")
                .eventStatus("PROCESSED")
                .eventType("CREATE_SCHOOL_CONTACT")
                .eventOutcome("SCHOOL_CONTACT_CREATED")
                .createUser(USER)
                .createDate(LocalDateTime.now())
                .updateUser(USER)
                .updateDate(LocalDateTime.now())
                .activityCode("INSTITUTE_EVENT")
                .build();

    }

    protected EventHistoryEntity createEventHistoryData(EventEntity event) {
        return EventHistoryEntity.builder()
                .event(event)
                .acknowledgeFlag("N")
                .createDate(LocalDateTime.now())
                .createUser(USER)
                .updateDate(LocalDateTime.now())
                .updateUser(USER)
                .build();
    }

}
