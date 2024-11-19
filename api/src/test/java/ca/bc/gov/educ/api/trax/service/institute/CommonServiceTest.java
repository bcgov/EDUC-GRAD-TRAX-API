package ca.bc.gov.educ.api.trax.service.institute;

import ca.bc.gov.educ.api.trax.messaging.NatsConnection;
import ca.bc.gov.educ.api.trax.messaging.jetstream.Publisher;
import ca.bc.gov.educ.api.trax.messaging.jetstream.Subscriber;
import ca.bc.gov.educ.api.trax.model.dto.institute.*;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import redis.clients.jedis.JedisCluster;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class CommonServiceTest {
    @Autowired
    private CommonService commonService;
    @MockBean
    private CodeService codeService;
    @MockBean
    private DistrictService districtService;
    @MockBean
    private SchoolService schoolService;

    @MockBean
    private JedisConnectionFactory jedisConnectionFactoryMock;
    @MockBean
    private JedisCluster jedisClusterMock;
    // NATS
    @MockBean
    private NatsConnection natsConnection;
    @MockBean
    private Publisher publisher;
    @MockBean
    private Subscriber subscriber;

    @Test
    public void testGetSchoolIdFromRedisCache() {
        UUID schoolId = UUID.randomUUID();
        String minCode = "12345678";
        String distNo = "123";
        SchoolDetail schoolDetail = mockInstituteData(schoolId, minCode, distNo, "PUBLIC", "01");

        var result = commonService.getSchoolIdFromRedisCache(minCode);
        assertThat(result).isEqualTo(UUID.fromString(schoolDetail.getSchoolId()));
    }

    @Test
    public void testGetSchoolIdStrFromRedisCache() {
        UUID schoolId = UUID.randomUUID();
        String minCode = "12345678";
        String distNo = "123";
        SchoolDetail schoolDetail = mockInstituteData(schoolId, minCode, distNo, "PUBLIC", "01");

        var result = commonService.getSchoolIdStrFromRedisCache(minCode);
        assertThat(result).isEqualTo(schoolDetail.getSchoolId());
    }

    @Test
    public void testGetAllSchoolClobs() {
        UUID schoolId = UUID.randomUUID();
        String minCode = "12345678";
        String distNo = "123";
        SchoolDetail schoolDetail = mockInstituteData(schoolId, minCode, distNo, "PUBLIC", "01");

        var result = commonService.getSchoolsForClobDataFromRedisCache();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSchoolId()).isEqualTo(schoolDetail.getSchoolId());
    }

    @Test
    public void testGetSchoolsByDistrictNumber() {
        UUID schoolId = UUID.randomUUID();
        String minCode = "12345678";
        String distNo = "123";
        SchoolDetail schoolDetail = mockInstituteData(schoolId, minCode, distNo, "PUBLIC", "01");

        when(schoolService.getSchoolDetailsByDistrictFromRedisCache(schoolDetail.getDistrictId())).thenReturn(List.of(schoolDetail));

        var result = commonService.getSchoolsByDistrictNumberFromRedisCache(distNo);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSchoolId()).isEqualTo(schoolDetail.getSchoolId());
    }

    @Test
    public void testGetSchoolsByDistrictNumber_returnEmptyList() {
        var result = commonService.getSchoolsByDistrictNumberFromRedisCache("123");
        assertThat(result).isEmpty();
    }

    @Test
    public void testGetSchoolClob() {
        UUID schoolId = UUID.randomUUID();
        String minCode = "12345678";
        String distNo = "123";
        String schoolCategoryLegacyCode = "01";
        SchoolDetail schoolDetail = mockInstituteData(schoolId, minCode, distNo, "PUBLIC", schoolCategoryLegacyCode);

        var result = commonService.getSchoolForClobDataBySchoolIdFromRedisCache(schoolId);
        assertThat(result).isNotNull();
        assertThat(result.getSchoolId()).isEqualTo(schoolDetail.getSchoolId());
        assertThat(result.getMinCode()).isEqualTo(schoolDetail.getMincode());
        assertThat(result.getSchoolCategoryLegacyCode()).isEqualTo(schoolCategoryLegacyCode);
        assertThat(result.getDistrictName()).isNotNull();
        assertThat(result.getAddress1()).isNotNull();
    }

    public SchoolDetail mockInstituteData(UUID schoolId, String minCode, String distNo, String schoolCategoryCode, String schoolCategoryLegacyCode) {
        District district = new District();
        district.setDistrictId(UUID.randomUUID().toString());
        district.setDistrictNumber(distNo);
        district.setDisplayName("Test District");

        SchoolCategoryCode schoolCategory = new SchoolCategoryCode();
        schoolCategory.setLegacyCode(schoolCategoryLegacyCode);
        schoolCategory.setSchoolCategoryCode(schoolCategoryCode);

        SchoolDetail schoolDetail = new SchoolDetail();
        schoolDetail.setSchoolId(schoolId.toString());
        schoolDetail.setMincode(minCode);
        schoolDetail.setDisplayName("Test School");
        schoolDetail.setDistrictId(district.getDistrictId());
        schoolDetail.setSchoolCategoryCode(schoolCategoryCode);

        SchoolAddress address = new SchoolAddress();
        address.setSchoolId(schoolDetail.getSchoolId());
        address.setAddressLine1("123 Test");
        address.setCity("Vancouver");
        address.setProvinceCode("BC");
        address.setPostal("V4N3Y2");
        address.setAddressTypeCode("MAILING");
        address.setCountryCode("CA");

        schoolDetail.setAddresses(List.of(address));

        School school = new School();
        school.setMincode(minCode);
        school.setDistrictId(district.getDistrictId());
        school.setSchoolId(schoolDetail.getSchoolId());
        school.setSchoolCategoryCode(schoolCategoryLegacyCode);

        when(this.codeService.getSchoolCategoryCodeFromRedisCache(schoolCategoryCode)).thenReturn(schoolCategory);
        when(this.districtService.getDistrictByIdFromRedisCache(district.getDistrictId())).thenReturn(district);
        when(this.districtService.getDistrictByDistNoFromRedisCache(district.getDistrictNumber())).thenReturn(district);
        when(this.schoolService.getSchoolDetailBySchoolId(schoolId)).thenReturn(schoolDetail);
        when(this.schoolService.getSchoolDetailByMincodeFromRedisCache(minCode)).thenReturn(schoolDetail);
        when(this.schoolService.getSchoolDetailsFromRedisCache()).thenReturn(List.of(schoolDetail));
        when(this.schoolService.getSchoolByMinCodeFromRedisCache(minCode)).thenReturn(school);
        when(this.schoolService.getSchoolBySchoolId(schoolId)).thenReturn(Optional.of(school));

        return schoolDetail;
    }

    @Test
    public void testGetSchoolForClobDataBySchoolIdFromRedisCache_shouldReturnSchool() {
        UUID schoolId = UUID.randomUUID();
        String schoolIdString = schoolId.toString();
        SchoolDetail schoolDetail = new SchoolDetail();
        schoolDetail.setSchoolId(schoolIdString);
        schoolDetail.setMincode("12345");
        schoolDetail.setDisplayName("My School");
        ca.bc.gov.educ.api.trax.model.dto.School expected = new ca.bc.gov.educ.api.trax.model.dto.School();
        expected.setSchoolId(schoolIdString);
        expected.setMinCode("12345");
        expected.setSchoolName("My School");
        when(schoolService.getSchoolDetailBySchoolId(schoolId)).thenReturn(schoolDetail);
        ca.bc.gov.educ.api.trax.model.dto.School actual = commonService.getSchoolForClobDataBySchoolIdFromRedisCache(schoolId);
        assertEquals(expected, actual);
    }

    @Test
    public void testGetSchoolForClobDataBySchoolIdFromRedisCache_shouldReturnNull() {
        UUID schoolId = UUID.randomUUID();
        when(schoolService.getSchoolDetailBySchoolId(schoolId)).thenReturn(null);
        ca.bc.gov.educ.api.trax.model.dto.School actual = commonService.getSchoolForClobDataBySchoolIdFromRedisCache(schoolId);
        assertNull(actual);
    }

}
