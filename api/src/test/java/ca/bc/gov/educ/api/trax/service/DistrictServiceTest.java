package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.messaging.NatsConnection;
import ca.bc.gov.educ.api.trax.messaging.jetstream.Publisher;
import ca.bc.gov.educ.api.trax.messaging.jetstream.Subscriber;
import ca.bc.gov.educ.api.trax.model.entity.DistrictEntity;
import ca.bc.gov.educ.api.trax.repository.DistrictRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class DistrictServiceTest {

    @Autowired
    private DistrictService districtService;

    @MockBean
    private DistrictRepository districtRepository;

    // NATS
    @MockBean
    private NatsConnection natsConnection;

    @MockBean
    private Publisher publisher;

    @MockBean
    private Subscriber subscriber;

    @Before
    public void setUp() {
        openMocks(this);
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testGetDistrictDetails() {
        // District
        final DistrictEntity district = new DistrictEntity();
        district.setDistrictNumber("123");
        district.setDistrictName("Test District");

        when(districtRepository.findById("123")).thenReturn(Optional.of(district));

        var result = districtService.getDistrictDetails("123");

        assertThat(result).isNotNull();
        assertThat(result.getDistrictNumber()).isEqualTo("123");
        assertThat(result.getDistrictName()).isEqualTo("Test District");
    }

    @Test
    public void testGetDistrictDetailsNull() {
        when(districtRepository.findById("123")).thenReturn(Optional.empty());
        var result = districtService.getDistrictDetails("123");
        assertThat(result).isNull();
    }
}
