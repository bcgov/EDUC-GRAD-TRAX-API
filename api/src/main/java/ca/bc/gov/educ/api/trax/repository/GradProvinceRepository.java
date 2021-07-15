package ca.bc.gov.educ.api.trax.repository;

import ca.bc.gov.educ.api.trax.model.entity.GradProvinceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GradProvinceRepository extends JpaRepository<GradProvinceEntity, String> {

    List<GradProvinceEntity> findAll();

}
