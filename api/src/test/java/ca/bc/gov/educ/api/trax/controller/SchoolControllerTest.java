package ca.bc.gov.educ.api.trax.controller;

import ca.bc.gov.educ.api.trax.model.dto.CommonSchool;
import ca.bc.gov.educ.api.trax.model.dto.School;
import ca.bc.gov.educ.api.trax.service.SchoolService;
import ca.bc.gov.educ.api.trax.util.EducGradTraxApiConstants;
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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("ALL")
public class SchoolControllerTest {

    @Mock
    private SchoolService schoolService;

    @Autowired
    private EducGradTraxApiConstants constants;

    @MockBean
    private WebClient webClient;

    @Mock WebClient.RequestHeadersSpec requestHeadersMock;
    @Mock WebClient.RequestHeadersUriSpec requestHeadersUriMock;
    @Mock WebClient.RequestBodySpec requestBodyMock;
    @Mock WebClient.RequestBodyUriSpec requestBodyUriMock;
    @Mock WebClient.ResponseSpec responseMock;

    @Mock
    ResponseHelper responseHelper;

    @InjectMocks
    private SchoolController schoolController;

    @Test
    public void testGetAllSchools() {
        final List<School> gradSchoolList = new ArrayList<>();
        School obj = new School();
        obj.setMinCode("1234567");
        obj.setSchoolName("Test1 School");
        gradSchoolList.add(obj);
        obj = new School();
        obj.setMinCode("7654321");
        obj.setSchoolName("Test2 School");
        gradSchoolList.add(obj);

        Mockito.when(schoolService.getSchoolList()).thenReturn(gradSchoolList);
        schoolController.getAllSchools();
        Mockito.verify(schoolService).getSchoolList();
    }

    @Test
    public void testGetSchoolDetails() {
        final School school = new School();
        school.setMinCode("1234567");
        school.setSchoolName("Test School");
        Mockito.when(schoolService.getSchoolDetails("1234567", "accessToken")).thenReturn(school);
        schoolController.getSchoolDetails("1234567", "accessToken");
        Mockito.verify(schoolService).getSchoolDetails("1234567", "accessToken");

    }

    @Test
    public void testGetSchoolsByParams() {
        final School school = new School();
        school.setMinCode("1234567");
        school.setSchoolName("Test School");
        Mockito.when(schoolService.getSchoolsByParams("1234567", "123", null, "accessToken")).thenReturn(Arrays.asList(school));
        schoolController.getSchoolsByParams("1234567", "123", null,"accessToken");
        Mockito.verify(schoolService).getSchoolsByParams("1234567", "123", null, "accessToken");
    }

    @Test
    public void testGetSchoolsBySchoolCategoryCode() {
        final School school = new School();
        school.setMinCode("1234567");
        school.setSchoolName("Test School");
        Mockito.when(schoolService.getSchoolsBySchoolCategory("01", "accessToken")).thenReturn(Arrays.asList(school));
        schoolController.getSchoolsBySchoolCategory("01", "accessToken");
        Mockito.verify(schoolService).getSchoolsBySchoolCategory("01", "accessToken");
    }

    @Test
    public void testCheckSchoolExists() {
        Mockito.when(schoolService.existsSchool("1234567")).thenReturn(true);
        schoolController.checkSchoolExists("1234567");
        Mockito.verify(schoolService).existsSchool("1234567");
    }

    private void mockCommonSchool(String minCode, String schoolName) {
        CommonSchool commonSchool = new CommonSchool();
        commonSchool.setSchlNo(minCode);
        commonSchool.setSchoolName(schoolName);
        commonSchool.setSchoolCategoryCode("02");

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getSchoolByMincodeSchoolApiUrl(), minCode))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(CommonSchool.class)).thenReturn(Mono.just(commonSchool));

    }
}
