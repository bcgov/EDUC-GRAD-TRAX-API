package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.model.dto.TranscriptStudentDemog;
import ca.bc.gov.educ.api.trax.model.transformer.TranscriptStudentDemogTransformer;
import ca.bc.gov.educ.api.trax.repository.TranscriptStudentDemogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TswService {

    @Autowired
    private TranscriptStudentDemogRepository transcriptStudentDemogRepository;

    @Autowired
    private TranscriptStudentDemogTransformer transcriptStudentDemogTransformer;

    private static Logger logger = LoggerFactory.getLogger(TswService.class);

    public TranscriptStudentDemog getTranscriptStudentDemog(String studNo) {
       return transcriptStudentDemogTransformer.transformToDTO(transcriptStudentDemogRepository.findById(studNo));
    }

    public boolean isGraduated(String studNo) {
        Integer count = transcriptStudentDemogRepository.countGradDateByPen(studNo);
        return count != null && count.intValue() > 0;
    }
}
