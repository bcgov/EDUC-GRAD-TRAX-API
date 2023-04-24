package ca.bc.gov.educ.api.trax.repository;

import ca.bc.gov.educ.api.trax.model.entity.DistrictEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DistrictRepository extends JpaRepository<DistrictEntity, String> {

    List<DistrictEntity> findAll();
    List<DistrictEntity> findByDistrictNameContaining(String districtName);
    List<DistrictEntity> findByActiveFlag(String activeFlag);
    Optional<DistrictEntity> findByDistrictNumberAndActiveFlag(String districtNumber, String activeFlag);

}
