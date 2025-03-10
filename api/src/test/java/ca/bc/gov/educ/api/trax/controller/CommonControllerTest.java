package ca.bc.gov.educ.api.trax.controller;

import ca.bc.gov.educ.api.trax.controller.v2.CommonController;
import ca.bc.gov.educ.api.trax.model.dto.School;
import ca.bc.gov.educ.api.trax.service.institute.CommonService;
import ca.bc.gov.educ.api.trax.util.EducGradTraxApiConstants;
import ca.bc.gov.educ.api.trax.util.GradValidation;
import ca.bc.gov.educ.api.trax.util.MessageHelper;
import ca.bc.gov.educ.api.trax.util.ResponseHelper;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("ALL")
public class CommonControllerTest {

    @InjectMocks
    private CommonController commonController;

    @Mock
    CommonService commonService;

    @Mock
    GradValidation validation;

    @Mock
    MessageHelper messagesHelper;

    @Mock
    ResponseHelper response;

    @Autowired
    private EducGradTraxApiConstants constants;

    @Test
    public void testGetAllSchoolsClobData() {
        School school = new School();
        school.setSchoolId(UUID.randomUUID().toString());
        school.setSchoolName("Test School");
        school.setMinCode("12345678");

        Mockito.when(commonService.getSchoolsForClobDataFromRedisCache()).thenReturn(List.of(school));
        commonController.getAllSchoolsForClobData();
        Mockito.verify(commonService).getSchoolsForClobDataFromRedisCache();
    }

    @Test
    public void testGetSchoolsForClobDataByDistrictNumber() {
        String distNo = "003";
        School school = new School();
        school.setSchoolId(UUID.randomUUID().toString());
        school.setSchoolName("Test School");
        school.setMinCode("12345678");

        Mockito.when(commonService.getSchoolsByDistrictNumberFromRedisCache(distNo)).thenReturn(List.of(school));
        commonController.getSchoolsForClobDataByDistrictNumber(distNo);
        Mockito.verify(commonService).getSchoolsByDistrictNumberFromRedisCache(distNo);
    }

    @Test
    public void testGetSchoolClobData() {
        School school = new School();
        UUID schoolId = UUID.randomUUID();
        school.setSchoolId(schoolId.toString());
        school.setSchoolName("Test School");
        school.setMinCode("12345678");

        Mockito.when(commonService.getSchoolForClobDataBySchoolIdFromRedisCache(schoolId)).thenReturn(school);
        commonController.getSchoolForClobDataBySchoolId(schoolId);
        Mockito.verify(commonService).getSchoolForClobDataBySchoolIdFromRedisCache(schoolId);
    }

    @Test
    public void testGetSchoolClobData_whenMinCode_isNot_Provided() {
        Mockito.when(validation.hasErrors()).thenReturn(true);
        commonController.getSchoolForClobDataBySchoolId(null);
        Mockito.verify(validation).stopOnErrors();
    }

    @Test
    public void testGetSchoolClobData_whenMinCode_isNot_Found() {
        UUID schoolId = UUID.randomUUID();
        Mockito.when(commonService.getSchoolForClobDataBySchoolIdFromRedisCache(schoolId)).thenReturn(null);
        commonController.getSchoolForClobDataBySchoolId(schoolId);
        Mockito.verify(commonService).getSchoolForClobDataBySchoolIdFromRedisCache(schoolId);
    }

    @Test
    public void testGetSchoolClobDataByParams() {
        School school = new School();
        UUID schoolId = UUID.randomUUID();
        school.setSchoolId(schoolId.toString());
        school.setSchoolName("Test School");
        school.setMinCode("12345678");

        Mockito.when(commonService.getSchoolForClobDataByMinCodeFromRedisCache(school.getMinCode())).thenReturn(school);
        commonController.getSchoolForClobDataByMinCode(school.getMinCode());
        Mockito.verify(commonService).getSchoolForClobDataByMinCodeFromRedisCache(school.getMinCode());
    }


}
