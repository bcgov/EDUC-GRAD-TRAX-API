package ca.bc.gov.educ.api.trax.controller;

import ca.bc.gov.educ.api.trax.model.dto.SnapshotResponse;
import ca.bc.gov.educ.api.trax.model.dto.TranscriptStudentCourse;
import ca.bc.gov.educ.api.trax.model.dto.TranscriptStudentDemog;
import ca.bc.gov.educ.api.trax.service.EdwService;
import ca.bc.gov.educ.api.trax.util.GradValidation;
import ca.bc.gov.educ.api.trax.util.ResponseHelper;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;

@RunWith(MockitoJUnitRunner.class)
@ExtendWith(MockitoExtension.class)
public class EdwControllerTest {

    @Mock
    private EdwService edwService;

    @Mock
    GradValidation validation;

    @Mock
    ResponseHelper responseHelper;

    @InjectMocks
    private EdwController edwController;

    @Test
    public void testGetSchoolListFromSnapshotByGradYear() {
        Integer gradYear = 2023;

        Mockito.when(edwService.getUniqueSchoolList(gradYear)).thenReturn(Arrays.asList("12345678","87654321"));
        edwController.getSchoolListFromSnapshotByGradYear(gradYear);
        Mockito.verify(edwService).getUniqueSchoolList(gradYear);
    }


    @Test
    public void testGetStudentsFromSnapshotByGradYear() {
        Integer gradYear = 2023;

        // grad student
        SnapshotResponse snapshot1 = new SnapshotResponse();
        snapshot1.setPen("123456789");
        snapshot1.setGraduatedDate("202306");
        snapshot1.setGpa(BigDecimal.valueOf(3.60));
        // non-grad student
        SnapshotResponse snapshot2 = new SnapshotResponse();
        snapshot1.setPen("111222333");

        Mockito.when(edwService.getStudents(gradYear)).thenReturn(Arrays.asList(snapshot1, snapshot2));
        edwController.getStudentsFromSnapshotByGradYear(gradYear);
        Mockito.verify(edwService).getStudents(gradYear);
    }

    @Test
    public void testGetStudentsFromSnapshotByGradYearAndSchool() {
        Integer gradYear = 2023;
        String minCode = "12345678";

        // grad student
        SnapshotResponse snapshot1 = new SnapshotResponse();
        snapshot1.setPen("123456789");
        snapshot1.setGraduatedDate("202306");
        snapshot1.setGpa(BigDecimal.valueOf(3.60));
        // non-grad student
        SnapshotResponse snapshot2 = new SnapshotResponse();
        snapshot1.setPen("111222333");

        Mockito.when(edwService.getStudents(gradYear, minCode)).thenReturn(Arrays.asList(snapshot1, snapshot2));
        edwController.getStudentsFromSnapshotByGradYearAndSchool(gradYear, minCode);
        Mockito.verify(edwService).getStudents(gradYear, minCode);
    }

}
