package ca.bc.gov.educ.api.trax.mapper;

import ca.bc.gov.educ.api.trax.EducGradTraxApiApplication;
import ca.bc.gov.educ.api.trax.model.dto.GradSchool;
import ca.bc.gov.educ.api.trax.model.dto.institute.School;
import ca.bc.gov.educ.api.trax.model.entity.institute.SchoolEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@SpringBootTest(classes = { EducGradTraxApiApplication.class })
@ActiveProfiles("test")
@ExtendWith(OutputCaptureExtension.class)
public class GradSchoolMapperTest {

    private static final GradSchoolMapper gradSchoolMapper = GradSchoolMapper.mapper;

    @Test
    void testGetMapper_shouldNotBeNull() {
        Assertions.assertNotNull(gradSchoolMapper);
    }

    @Test
    void test_toStructure() {
        List<GradSchool> gradSchools = new ArrayList<>();
        GradSchool gradSchool = new GradSchool();
        gradSchool.setSchoolID("ID");
        gradSchool.setCanIssueCertificates("Y");
        gradSchool.setCanIssueTranscripts("N");
        gradSchools.add(gradSchool);

        SchoolEntity schoolEntity = new SchoolEntity();
        String mincode = "12345678";
        schoolEntity.setSchoolId("ID");
        schoolEntity.setDistrictId("DistID");
        schoolEntity.setSchoolNumber("12345");
        schoolEntity.setSchoolCategoryCode("SCC");
        schoolEntity.setEmail("abc@xyz.ca");
        schoolEntity.setDisplayName("Tk̓emlúps te Secwépemc");
        schoolEntity.setDisplayNameNoSpecialChars("Tkkemlups te Secwepemc");
        schoolEntity.setMincode(mincode);

        Map<String, GradSchool> gradSchoolMap = gradSchools.stream()
                .collect(Collectors.toMap(GradSchool::getSchoolID, Function.identity(), (existing, replacement) -> replacement));
        School result = gradSchoolMapper.toStructure(schoolEntity, gradSchoolMap);
        assertNotNull(result);
        assertEquals(result.getSchoolId(), schoolEntity.getSchoolId());
    }

    @Test
    void test_toSchools() {
        List<GradSchool> gradSchools = new ArrayList<>();
        GradSchool gradSchool = new GradSchool();
        gradSchool.setSchoolID("ID");
        gradSchool.setCanIssueCertificates("Y");
        gradSchool.setCanIssueTranscripts("N");
        gradSchools.add(gradSchool);

        List<SchoolEntity> schoolEntities = new ArrayList<>();
        SchoolEntity schoolEntity = new SchoolEntity();
        String mincode = "12345678";
        schoolEntity.setSchoolId("ID");
        schoolEntity.setDistrictId("DistID");
        schoolEntity.setSchoolNumber("12345");
        schoolEntity.setSchoolCategoryCode("SCC");
        schoolEntity.setEmail("abc@xyz.ca");
        schoolEntity.setDisplayName("Tk̓emlúps te Secwépemc");
        schoolEntity.setDisplayNameNoSpecialChars("Tkkemlups te Secwepemc");
        schoolEntity.setMincode(mincode);
        schoolEntities.add(schoolEntity);

        Map<String, GradSchool> gradSchoolMap = gradSchools.stream()
                .collect(Collectors.toMap(GradSchool::getSchoolID, Function.identity(), (existing, replacement) -> replacement));
        List<School> result = gradSchoolMapper.toSchools(schoolEntities, gradSchoolMap);
        assertNotNull(result);
        assertEquals(result.size(), schoolEntities.size());
    }


}
