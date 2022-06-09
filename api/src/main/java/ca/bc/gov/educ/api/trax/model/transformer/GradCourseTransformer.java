package ca.bc.gov.educ.api.trax.model.transformer;

import ca.bc.gov.educ.api.trax.model.dto.GradCourse;
import ca.bc.gov.educ.api.trax.model.entity.GradCourseEntity;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class GradCourseTransformer {

    @Autowired
    ModelMapper modelMapper;

    public GradCourse transformToDTO(GradCourseEntity gradCourseEntity) {
        return modelMapper.map(gradCourseEntity, GradCourse.class);
    }

    public GradCourse transformToDTO(Optional<GradCourseEntity> gradCourseEntityOptional) {
        GradCourseEntity entity;
        if (gradCourseEntityOptional.isPresent()) {
            entity = gradCourseEntityOptional.get();
        } else {
            entity = new GradCourseEntity();
        }

        return modelMapper.map(entity, GradCourse.class);
    }

    public List<GradCourse> transformToDTO(Iterable<GradCourseEntity> gradCourseEntities) {
        List<GradCourse> gradCourseList = new ArrayList<>();

        for (GradCourseEntity gradCourseEntity : gradCourseEntities) {
            GradCourse gradCourse = modelMapper.map(gradCourseEntity, GradCourse.class);

            gradCourse.setCourseCode(gradCourseEntity.getGradCourseKey().getCourseCode());
            gradCourse.setCourseLevel(gradCourseEntity.getGradCourseKey().getCourseLevel());
            gradCourse.setGradReqtYear(gradCourseEntity.getGradCourseKey().getGradReqtYear());

            gradCourseList.add(gradCourse);
        }

        return gradCourseList;
    }

    public GradCourseEntity transformToEntity(GradCourse gradCourse) {
        return modelMapper.map(gradCourse, GradCourseEntity.class);
    }
}
