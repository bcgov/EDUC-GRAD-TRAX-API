package ca.bc.gov.educ.api.trax.util;

import ca.bc.gov.educ.api.trax.messaging.NatsConnection;
import ca.bc.gov.educ.api.trax.messaging.jetstream.Publisher;
import ca.bc.gov.educ.api.trax.messaging.jetstream.Subscriber;
import ca.bc.gov.educ.api.trax.model.entity.EventEntity;
import ca.bc.gov.educ.api.trax.model.entity.EventHistoryEntity;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;

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


    protected EventEntity createEventData() {
        return EventEntity.builder()
                .eventId(UUID.randomUUID())
                .eventPayload("""
                        {
                          "authorityContactId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
                          "independentAuthorityId": "d3b07384-d9a0-4c1e-8b0e-6e2b6b7b8b8b",
                          "authorityContactTypeCode": "string",
                          "phoneNumber": "string",
                          "jobTitle": "string",
                          "phoneExtension": "string",
                          "alternatePhoneNumber": "string",
                          "alternatePhoneExtension": "string",
                          "email": "string",
                          "firstName": "string",
                          "lastName": "string",
                          "effectiveDate": "string",
                          "expiryDate": "string"
                        }""")
                .eventStatus("PROCESSED")
                .eventType("CREATE_AUTHORITY_CONTACT")
                .eventOutcome("AUTHORITY_CONTACT_CREATED")
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
