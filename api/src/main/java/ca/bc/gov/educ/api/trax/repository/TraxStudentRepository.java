package ca.bc.gov.educ.api.trax.repository;

import ca.bc.gov.educ.api.trax.model.entity.TraxStudentEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * The interface TRAX Student repository.
 */
@Repository
public interface TraxStudentRepository extends CrudRepository<TraxStudentEntity, String> {


}
