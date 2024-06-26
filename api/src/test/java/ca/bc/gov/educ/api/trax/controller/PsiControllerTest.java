package ca.bc.gov.educ.api.trax.controller;

import ca.bc.gov.educ.api.trax.model.dto.Psi;
import ca.bc.gov.educ.api.trax.model.dto.StudentPsi;
import ca.bc.gov.educ.api.trax.service.PsiService;
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
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
@ExtendWith(MockitoExtension.class)
public class PsiControllerTest {

    @Mock
    private PsiService psiService;

    @Mock
    ResponseHelper responseHelper;

    @InjectMocks
    private PsiController psiController;

    @Test
    public void testGetAllPsis() {
        List<Psi> gradPsiList = new ArrayList<>();
        Psi obj = new Psi();
        obj.setPsiCode("1234567");
        obj.setPsiName("Test1 Psi");
        gradPsiList.add(obj);
        obj = new Psi();
        obj.setPsiCode("7654321");
        obj.setPsiName("Test2 Psi");
        gradPsiList.add(obj);

        Mockito.when(psiService.getPSIList()).thenReturn(gradPsiList);
        psiController.getAllPSIs();
        Mockito.verify(psiService).getPSIList();
    }

    @Test
    public void testGetPsiDetails() {
        Psi school = new Psi();
        school.setPsiCode("1234567");
        school.setPsiName("Test Psi");

        Mockito.when(psiService.getPSIDetails("1234567")).thenReturn(school);
        psiController.getPSIDetails("1234567");
        Mockito.verify(psiService).getPSIDetails("1234567");

    }

    @Test
    public void testGetPsisByParams() {
        Psi school = new Psi();
        school.setPsiCode("1234567");
        school.setPsiName("Test Psi");

        Mockito.when(psiService.getPSIByParams("Test Psi", "1234567", null,null,null)).thenReturn(List.of(school));
        psiController.getPSIByParams("Test Psi","1234567", null, null,null);
        Mockito.verify(psiService).getPSIByParams("Test Psi", "1234567", null,null,null);
    }

    @Test
    public void testGetStudentPSIDetails() {
        StudentPsi studentPsi = new StudentPsi();
        studentPsi.setPsiYear("2021");
        studentPsi.setPsiCode("001");
        studentPsi.setPen("123123131");
        Mockito.when(psiService.getStudentPSIDetails("paper","2021","001")).thenReturn(List.of(studentPsi));
        psiController.getStudentPSIDetails("paper","001","2021");
        Mockito.verify(psiService).getStudentPSIDetails("paper","2021","001");

    }
}
