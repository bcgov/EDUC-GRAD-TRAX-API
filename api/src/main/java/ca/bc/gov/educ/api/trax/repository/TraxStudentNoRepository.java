package ca.bc.gov.educ.api.trax.repository;

import ca.bc.gov.educ.api.trax.model.entity.TraxStudentNoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TraxStudentNoRepository extends PagingAndSortingRepository<TraxStudentNoEntity, String> {
  Page<TraxStudentNoEntity> findAllByStatus(String status, Pageable pageable);
  Integer countAllByStatus(String status);
}
