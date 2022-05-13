package ca.bc.gov.educ.api.trax.repository;

import ca.bc.gov.educ.api.trax.model.entity.SchoolEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface SchoolRepository extends JpaRepository<SchoolEntity, String> {

    List<SchoolEntity> findAll();
    List<SchoolEntity> findByMinCodeStartsWith(String districtNumber);
	@Query(value="SELECT si.* FROM tab_school si where "
			+ "(:city is null or si.CITY like %:city%)  and "
			+ "(:schoolName is null or si.SCHL_NAME like %:schoolName%)  and "
			+ "(:isMincodeListIncluded is null or si.MINCODE in :minCodeList) and ROWNUM <= 50",nativeQuery = true)			
	public List<SchoolEntity> searchForSchool(String schoolName,String isMincodeListIncluded, Set<String> minCodeList, String city);

	@Query(value="select count(*) from TAB_SCHOOL sc \n" +
			"where sc.mincode = :minCode \n", nativeQuery=true)
	long countTabSchools(@Param("minCode") String minCode);
}
