package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.model.dto.TranscriptStudentCourse;
import ca.bc.gov.educ.api.trax.model.dto.TranscriptStudentDemog;
import ca.bc.gov.educ.api.trax.model.transformer.TranscriptStudentCourseTransformer;
import ca.bc.gov.educ.api.trax.model.transformer.TranscriptStudentDemogTransformer;
import ca.bc.gov.educ.api.trax.repository.TranscriptStudentCourseRepository;
import ca.bc.gov.educ.api.trax.repository.TranscriptStudentDemogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TswService {

    @Autowired
    private TranscriptStudentDemogRepository transcriptStudentDemogRepository;

    @Autowired
    private TranscriptStudentDemogTransformer transcriptStudentDemogTransformer;

    @Autowired
    private TranscriptStudentCourseRepository transcriptStudentCourseRepository;

    @Autowired
    private TranscriptStudentCourseTransformer transcriptStudentCourseTransformer;

    private static Logger logger = LoggerFactory.getLogger(TswService.class);

    @Transactional(readOnly = true)
    public TranscriptStudentDemog getTranscriptStudentDemog(String studNo) {
       return transcriptStudentDemogTransformer.transformToDTO(transcriptStudentDemogRepository.findById(studNo));
    }

    @Transactional(readOnly = true)
    public List<TranscriptStudentCourse> getTranscriptStudentCourses(String studNo) {
        return transcriptStudentCourseTransformer.transformToDTO(transcriptStudentCourseRepository.findByPen(studNo));
    }

    @Transactional(readOnly = true)
    public boolean existsTranscriptStudentDemog(String studNo) {
        return transcriptStudentDemogRepository.existsById(studNo);
    }
}
