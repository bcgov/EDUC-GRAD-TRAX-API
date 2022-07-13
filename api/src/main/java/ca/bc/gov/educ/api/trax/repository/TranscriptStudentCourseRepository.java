package ca.bc.gov.educ.api.trax.repository;

import ca.bc.gov.educ.api.trax.model.entity.TranscriptStudentCourseEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * The interface TSW Student Course repository.
 */
@Repository
public interface TranscriptStudentCourseRepository extends CrudRepository<TranscriptStudentCourseEntity, String> {
    @Query(value = "select e from TranscriptStudentCourseEntity e where e.studentCourseKey.studNo = :pen")
    Iterable<TranscriptStudentCourseEntity> findByPen(@Param("pen")String pen);
}
