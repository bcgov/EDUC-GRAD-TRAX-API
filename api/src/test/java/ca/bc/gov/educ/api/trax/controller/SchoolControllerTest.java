package ca.bc.gov.educ.api.trax.controller;

import ca.bc.gov.educ.api.trax.model.dto.School;
import ca.bc.gov.educ.api.trax.service.SchoolService;
import ca.bc.gov.educ.api.trax.util.ResponseHelper;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("rawtypes")
public class SchoolControllerTest {

    @Mock
    private SchoolService schoolService;

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

        Mockito.when(schoolService.getSchoolDetails("1234567")).thenReturn(school);
        schoolController.getSchoolDetails("1234567");
        Mockito.verify(schoolService).getSchoolDetails("1234567");

    }

    @Test
    public void testGetSchoolsByParams() {
        final School school = new School();
        school.setMinCode("1234567");
        school.setSchoolName("Test School");
        Mockito.when(schoolService.getSchoolsByParams("1234567", "123")).thenReturn(Arrays.asList(school));
        schoolController.getSchoolsByParams("1234567", "123");
        Mockito.verify(schoolService).getSchoolsByParams("1234567", "123");
    }

    @Test
    public void testCheckSchoolExists() {
        Mockito.when(schoolService.existsSchool("1234567")).thenReturn(true);
        schoolController.checkSchoolExists("1234567");
        Mockito.verify(schoolService).existsSchool("1234567");
    }
}
