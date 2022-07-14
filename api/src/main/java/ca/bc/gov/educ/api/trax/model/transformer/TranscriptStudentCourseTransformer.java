package ca.bc.gov.educ.api.trax.model.transformer;

import ca.bc.gov.educ.api.trax.model.dto.TranscriptStudentCourse;
import ca.bc.gov.educ.api.trax.model.entity.TranscriptStudentCourseEntity;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class TranscriptStudentCourseTransformer {

    @Autowired
    ModelMapper modelMapper;

    public TranscriptStudentCourse transformToDTO(TranscriptStudentCourseEntity transcriptStudentCourseEntity) {
        return modelMapper.map(transcriptStudentCourseEntity, TranscriptStudentCourse.class);
    }

    public TranscriptStudentCourse transformToDTO(Optional<TranscriptStudentCourseEntity> transcriptStudentCourseEntityOptional) {
        TranscriptStudentCourseEntity entity;
        if (transcriptStudentCourseEntityOptional.isPresent()) {
            entity = transcriptStudentCourseEntityOptional.get();
        } else {
            entity = new TranscriptStudentCourseEntity();
        }

        return modelMapper.map(entity, TranscriptStudentCourse.class);
    }

    public List<TranscriptStudentCourse> transformToDTO(Iterable<TranscriptStudentCourseEntity> transcriptStudentCourseEntities) {
        List<TranscriptStudentCourse> transcriptStudentCourseList = new ArrayList<>();

        for (TranscriptStudentCourseEntity transcriptStudentCourseEntity : transcriptStudentCourseEntities) {
            TranscriptStudentCourse transcriptStudentCourse = modelMapper.map(transcriptStudentCourseEntity, TranscriptStudentCourse.class);

            transcriptStudentCourse.setCourseCode(transcriptStudentCourseEntity.getStudentCourseKey().getCourseCode());
            transcriptStudentCourse.setCourseLevel(transcriptStudentCourseEntity.getStudentCourseKey().getCourseLevel());
            transcriptStudentCourse.setStudNo(transcriptStudentCourseEntity.getStudentCourseKey().getStudNo());

            transcriptStudentCourseList.add(transcriptStudentCourse);
        }

        return transcriptStudentCourseList;
    }

    public TranscriptStudentCourseEntity transformToEntity(TranscriptStudentCourse transcriptStudentCourse) {
        return modelMapper.map(transcriptStudentCourse, TranscriptStudentCourseEntity.class);
    }
}
