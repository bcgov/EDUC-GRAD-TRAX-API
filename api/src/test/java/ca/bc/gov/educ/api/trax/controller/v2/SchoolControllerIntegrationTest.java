package ca.bc.gov.educ.api.trax.controller.v2;

import ca.bc.gov.educ.api.trax.EducGradTraxApiApplication;
import ca.bc.gov.educ.api.trax.model.dto.institute.School;
import ca.bc.gov.educ.api.trax.model.dto.institute.SchoolDetail;
import ca.bc.gov.educ.api.trax.model.transformer.institute.SchoolTransformer;
import ca.bc.gov.educ.api.trax.model.transformer.institute.SchoolDetailTransformer;
import ca.bc.gov.educ.api.trax.repository.redis.SchoolRedisRepository;
import ca.bc.gov.educ.api.trax.repository.redis.SchoolDetailRedisRepository;
import ca.bc.gov.educ.api.trax.service.institute.SchoolService;
import ca.bc.gov.educ.api.trax.support.MockConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ca.bc.gov.educ.api.trax.support.TestRedisConfiguration;

import java.util.UUID;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {EducGradTraxApiApplication.class})
@AutoConfigureMockMvc
@ActiveProfiles({"test", "redisTest"})
@ContextConfiguration(classes = {TestRedisConfiguration.class, MockConfiguration.class})
class SchoolControllerIntegrationTest {

  @Autowired
  private MockMvc mockMvc;
  @MockBean
  private SchoolService schoolServiceMock;

  @Autowired
  private SchoolRedisRepository schoolRedisRepository;

  @Autowired
  private SchoolDetailRedisRepository schoolDetailRedisRepository;

  @Autowired
  private SchoolTransformer schoolTransformer;

  @Autowired
  private SchoolDetailTransformer schoolDetailTransformer;

  private final String schoolId = UUID.randomUUID().toString();
  private final String districtId = UUID.randomUUID().toString();

  @BeforeEach
  void setup() {
    schoolRedisRepository.deleteAll();
    schoolDetailRedisRepository.deleteAll();

    School school = new School();
    school.setSchoolId(schoolId);
    school.setMincode("1234567");
    school.setDistrictId(districtId);
    schoolRedisRepository.save(schoolTransformer.transformToEntity(school));

    SchoolDetail schoolDetail = new SchoolDetail();
    schoolDetail.setSchoolId(schoolId);
    schoolDetail.setDistrictId(districtId);
    schoolDetailRedisRepository.save(schoolDetailTransformer.transformToEntity(schoolDetail));
  }

  @Test
  void testGetSchoolDetails_givenValidPayload_shouldReturnOk() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/trax/school/{schoolId}", schoolId)
                    .with(jwt().jwt(jwt -> jwt.claim("scope", "READ_GRAD_SCHOOL_DATA")))
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.schoolId").value(schoolId))
            .andExpect(jsonPath("$.mincode").value("1234567"));
  }

  @Test
  void testGetSchoolDetails_givenInvalidSchoolId_shouldReturnNotFound() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/trax/school/{schoolId}", "invalid_id")
                    .with(jwt().jwt(jwt -> jwt.claim("scope", "READ_GRAD_SCHOOL_DATA")))
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.message").value("Parameter 'schoolId' with value 'invalid_id' could not be converted to type 'UUID'."));
  }

  @Test
  void testGetSchoolDetails_givenInvalidScope_shouldReturnUnauthorized() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/trax/school/{schoolId}", schoolId)
                    .with(jwt().jwt(jwt -> jwt.claim("scope", "BAD_SCOPE")))
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
  }

  @Test
  void testGetSchoolsByParams_givenValidPayload_shouldReturnOk() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/trax/school/search")
                    .param("districtId", districtId)
                    .with(jwt().jwt(jwt -> jwt.claim("scope", "READ_GRAD_SCHOOL_DATA")))
                    .param("mincode", "1234567")
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json("[{\"schoolId\":\""+schoolId+"\",\"districtId\":\""+districtId+"\"}]"));
  }

  @Test
  void testGetSchoolsByParams_givenNoResult_shouldReturnOk() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/trax/school/search")
                    .param("districtId", UUID.randomUUID().toString())
                    .with(jwt().jwt(jwt -> jwt.claim("scope", "READ_GRAD_SCHOOL_DATA")))
                    .param("mincode", "1234567")
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json("[]"));
  }

  @Test
  void testGetSchoolsByParams_givenBadDistrictId_shouldReturnBadRequest() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/trax/school/search")
                    .param("districtId", "BAD_ID123")
                    .with(jwt().jwt(jwt -> jwt.claim("scope", "READ_GRAD_SCHOOL_DATA")))
                    .param("mincode", "1234567")
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.message").value("Parameter 'districtId' with value 'BAD_ID123' could not be converted to type 'UUID'."));
  }

  @Test
  void testGetSchoolsByParams_givenScope_shouldReturnForbidden() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/trax/school/search")
                    .param("districtId", districtId)
                    .with(jwt().jwt(jwt -> jwt.claim("scope", "BAD_SCOPE")))
                    .param("mincode", "1234567")
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
  }

  @Test
  void testReloadSchoolsIntoCache_shouldReturnOK() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.put("/api/v2/trax/school/cache/schools")
                    .with(jwt().jwt(jwt -> jwt.claim("scope", "UPDATE_GRAD_TRAX_CACHE")))
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
  }

  @Test
  void testReloadSchoolsIntoCache_shouldReturnUnprocessableEntity() throws Exception {
    doThrow(Exception.class).when(schoolServiceMock).initializeSchoolCache(true);
    mockMvc.perform(MockMvcRequestBuilders.put("/api/v2/trax/school/cache/schools")
                    .with(jwt().jwt(jwt -> jwt.claim("scope", "UPDATE_GRAD_TRAX_CACHE")))
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity());
  }

  @Test
  void testReloadSchoolDetailssIntoCache_shouldReturnOK() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.put("/api/v2/trax/school/cache/school-details")
                    .with(jwt().jwt(jwt -> jwt.claim("scope", "UPDATE_GRAD_TRAX_CACHE")))
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
  }

  @Test
  void testReloadSchoolDetailsIntoCache_shouldReturnUnprocessableEntity() throws Exception {
    doThrow(Exception.class).when(schoolServiceMock).initializeSchoolDetailCache(true);
    mockMvc.perform(MockMvcRequestBuilders.put("/api/v2/trax/school/cache/school-details")
                    .with(jwt().jwt(jwt -> jwt.claim("scope", "UPDATE_GRAD_TRAX_CACHE")))
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity());
  }
}