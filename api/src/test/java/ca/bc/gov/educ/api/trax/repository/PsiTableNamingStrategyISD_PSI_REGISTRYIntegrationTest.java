package ca.bc.gov.educ.api.trax.repository;

import ca.bc.gov.educ.api.trax.EducGradTraxApiApplication;
import ca.bc.gov.educ.api.trax.support.MockConfiguration;
import ca.bc.gov.educ.api.trax.support.TestRedisConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {EducGradTraxApiApplication.class})
@AutoConfigureMockMvc
@ActiveProfiles({"test", "redisTest"})
@ContextConfiguration(classes = {TestRedisConfiguration.class, MockConfiguration.class})
@TestPropertySource(properties = {"trax.psi.table-name=ISD_PSI_REGISTRY"})
@Transactional
class PsiTableNamingStrategyISDPSIREGISTRYIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PsiRepository psiRepository;

    @Autowired
    private Environment environment;

    /**
     * Gets the configured table name from Spring Environment.
     */
    private String getConfiguredTableName() {
        return environment.getProperty("trax.psi.table-name", "TAB_POSTSEC");
    }

    @Test
    void testTableNamingStrategy_withISD_PSI_REGISTRY() throws Exception {
        // Verify the property is correctly configured
        String configuredTableName = getConfiguredTableName();
        assertEquals("ISD_PSI_REGISTRY", configuredTableName, 
                "Spring property should be set to ISD_PSI_REGISTRY");

        // Verify repository query works - if the table name is wrong, this would fail
        assertDoesNotThrow(() -> psiRepository.findAll(),
                "Repository query should execute successfully with ISD_PSI_REGISTRY table");

        // Verify endpoint works
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/trax/psi")
                        .with(jwt().jwt(jwt -> jwt.claim("scope", "READ_GRAD_PSI_DATA")))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}

