package ca.bc.gov.educ.api.trax.service.institute;

import ca.bc.gov.educ.api.trax.constant.CacheKey;
import ca.bc.gov.educ.api.trax.model.dto.ResponseObj;
import ca.bc.gov.educ.api.trax.model.dto.institute.PaginatedResponse;
import ca.bc.gov.educ.api.trax.model.dto.institute.School;
import ca.bc.gov.educ.api.trax.model.dto.institute.SchoolDetail;
import ca.bc.gov.educ.api.trax.model.entity.institute.*;
import ca.bc.gov.educ.api.trax.model.transformer.institute.SchoolDetailTransformer;
import ca.bc.gov.educ.api.trax.model.transformer.institute.SchoolTransformer;
import ca.bc.gov.educ.api.trax.repository.redis.*;
import ca.bc.gov.educ.api.trax.service.RESTService;
import ca.bc.gov.educ.api.trax.util.EducGradTraxApiConstants;
import ca.bc.gov.educ.api.trax.util.RestUtils;
import org.h2.util.Cache;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
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
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import redis.clients.jedis.JedisCluster;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"unchecked","rawtypes"})
class CacheServiceTest {

    @Autowired
    private EducGradTraxApiConstants constants;
    @Autowired
    private CacheService cacheService;
    @MockBean
    private ServiceHelper serviceHelperMock;
    @MockBean
    private SchoolRedisRepository schoolRedisRepository;
    @MockBean
    private SchoolDetailRedisRepository schoolDetailRedisRepository;
    @MockBean
    private DistrictRedisRepository districtRedisRepository;
    @MockBean
    private SchoolCategoryCodeRedisRepository schoolCategoryCodeRedisRepository;
    @MockBean
    private SchoolFundingGroupCodeRedisRepository schoolFundingGroupCodeRedisRepository;
    @MockBean
    private JedisConnectionFactory jedisConnectionFactoryMock;
    @MockBean
    private JedisCluster jedisClusterMock;

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
    void whenLoadSchoolsIntoRedisCache_DoesNotThrow() {
        List<SchoolEntity> schoolEntities = new ArrayList<>();
        SchoolEntity schoolEntity = new SchoolEntity();

        schoolEntity.setSchoolId("ID");
        schoolEntity.setDistrictId("DistID");
        schoolEntity.setSchoolNumber("12345");
        schoolEntity.setSchoolCategoryCode("SCC");
        schoolEntity.setEmail("abc@xyz.ca");
        schoolEntity.setDisplayName("Tk̓emlúps te Secwépemc");
        schoolEntity.setDisplayNameNoSpecialChars("Tkkemlups te Secwepemc");
        schoolEntities.add(schoolEntity);
        when(this.schoolRedisRepository.saveAll(schoolEntities))
                .thenReturn(schoolEntities);
        assertDoesNotThrow(() -> cacheService.loadSchoolsIntoRedisCacheAsync(schoolEntities));
        assertDoesNotThrow(() -> cacheService.loadSchoolsIntoRedisCache(schoolEntities));
    }

    @Test
    void whenLoadSchoolDetailsIntoRedisCache_DoesNotThrow() {
        List<SchoolDetailEntity> schoolDetailEntities = new ArrayList<>();
        String districtId = "DistID";
        String mincode = "12345678";
        SchoolDetailEntity schoolDetailEntity = new SchoolDetailEntity();
        schoolDetailEntity.setSchoolId("ID");
        schoolDetailEntity.setDistrictId(districtId);
        schoolDetailEntity.setSchoolNumber("12345");
        schoolDetailEntity.setMincode(mincode);
        schoolDetailEntity.setSchoolCategoryCode("SCC");
        schoolDetailEntity.setEmail("abc@xyz.ca");
        schoolDetailEntities.add(schoolDetailEntity);
        when(this.schoolDetailRedisRepository.saveAll(schoolDetailEntities))
                .thenReturn(schoolDetailEntities);
        assertDoesNotThrow(() -> cacheService.loadSchoolDetailsIntoRedisCacheAsync(schoolDetailEntities));
        assertDoesNotThrow(() -> cacheService.loadSchoolDetailsIntoRedisCache(schoolDetailEntities));
    }

    @Test
    void whenLoadDistrictsIntoRedisCache_DoesNotThrow() {
        List<DistrictEntity> districtEntities = new ArrayList<>();
        DistrictEntity districtEntity = new DistrictEntity();
        districtEntity.setDistrictId("ID");
        districtEntity.setDistrictNumber("1234");
        districtEntity.setDistrictStatusCode("SC");
        districtEntity.setDistrictRegionCode("RC");
        districtEntity.setContacts(Arrays.asList(new DistrictContactEntity(), new DistrictContactEntity()));
        districtEntities.add(districtEntity);
        when(this.districtRedisRepository.saveAll(districtEntities))
                .thenReturn(districtEntities);
        assertDoesNotThrow(() -> cacheService.loadDistrictsIntoRedisCacheAsync(districtEntities));
        assertDoesNotThrow(() -> cacheService.loadDistrictsIntoRedisCache(districtEntities));
    }

    @Test
    void whenLoadSchoolCategoryCodesIntoRedisCache_DoesNotThrow() {
        List<SchoolCategoryCodeEntity> schoolCategoryCodeEntities = new ArrayList<>();
        SchoolCategoryCodeEntity scce = new SchoolCategoryCodeEntity();
        scce.setSchoolCategoryCode("11");
        scce.setDescription("Description");
        scce.setLegacyCode("LegacyCode");
        scce.setLabel("Label");
        scce.setEffectiveDate("01-01-2024");
        scce.setExpiryDate("01-01-2024");
        scce.setDisplayOrder("10");
        schoolCategoryCodeEntities.add(scce);
        when(this.schoolCategoryCodeRedisRepository.saveAll(schoolCategoryCodeEntities))
                .thenReturn(schoolCategoryCodeEntities);
        assertDoesNotThrow(() -> cacheService.loadSchoolCategoryCodesIntoRedisCacheAsync(schoolCategoryCodeEntities));
        assertDoesNotThrow(() -> cacheService.loadSchoolCategoryCodesIntoRedisCache(schoolCategoryCodeEntities));
    }

    @Test
    void whenLsoadSchoolFundingGroupCodesIntoRedisCache_DoesNotThrow() {
        List<SchoolFundingGroupCodeEntity> schoolFundingGroupCodeEntities = new ArrayList<>();
        SchoolFundingGroupCodeEntity sfgc = new SchoolFundingGroupCodeEntity();

        sfgc.setSchoolFundingGroupCode("CODE");
        sfgc.setDescription("Description");
        sfgc.setLabel("Label");
        sfgc.setEffectiveDate("01-01-2024");
        sfgc.setExpiryDate("01-01-2024");
        sfgc.setDisplayOrder("10");
        schoolFundingGroupCodeEntities.add(sfgc);
        when(this.schoolFundingGroupCodeRedisRepository.saveAll(schoolFundingGroupCodeEntities))
                .thenReturn(schoolFundingGroupCodeEntities);
        assertDoesNotThrow(() -> cacheService.loadSchoolFundingGroupCodesIntoRedisCacheAsync(schoolFundingGroupCodeEntities));
        assertDoesNotThrow(() -> cacheService.loadSchoolFundingGroupCodesIntoRedisCache(schoolFundingGroupCodeEntities));
    }

}
