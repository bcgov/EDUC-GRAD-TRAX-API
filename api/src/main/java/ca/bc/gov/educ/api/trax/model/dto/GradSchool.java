package ca.bc.gov.educ.api.trax.model.dto;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class GradSchool {

    private String schoolID;
    private String canIssueTranscripts;
    private String canIssueCertificates;

}
