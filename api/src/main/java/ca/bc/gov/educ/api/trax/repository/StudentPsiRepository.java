package ca.bc.gov.educ.api.trax.repository;

import ca.bc.gov.educ.api.trax.model.entity.StudentPsiEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentPsiRepository extends JpaRepository<StudentPsiEntity, String>, StudentPsiRepositoryCustom {
}
