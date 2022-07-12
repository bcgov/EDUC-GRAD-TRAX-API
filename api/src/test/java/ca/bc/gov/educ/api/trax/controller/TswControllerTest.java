package ca.bc.gov.educ.api.trax.controller;

import ca.bc.gov.educ.api.trax.model.dto.TranscriptStudentDemog;
import ca.bc.gov.educ.api.trax.service.TswService;
import ca.bc.gov.educ.api.trax.util.ResponseHelper;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;

@RunWith(MockitoJUnitRunner.class)
@ExtendWith(MockitoExtension.class)
public class TswControllerTest {

    @Mock
    private TswService tswService;

    @Mock
    ResponseHelper responseHelper;

    @InjectMocks
    private TswController tswController;

    @Test
    public void testGetPsiDetails() {
        TranscriptStudentDemog transcriptStudentDemog = new TranscriptStudentDemog();
        transcriptStudentDemog.setStudNo("123456789");
        transcriptStudentDemog.setFirstName("Test");
        transcriptStudentDemog.setLastName("QA");
        transcriptStudentDemog.setMincode("7654321");
        transcriptStudentDemog.setSchoolName("Test2 School");

        Mockito.when(tswService.getTranscriptStudentDemog("123456789")).thenReturn(transcriptStudentDemog);
        tswController.getTranscriptStudentDemogByPen("123456789");
        Mockito.verify(tswService).getTranscriptStudentDemog("123456789");

    }

}
