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

    List<SchoolEntity> findByMinCodeStartsWith(String districtNumber);
	@Query(value="SELECT si.* FROM TAB_SCHOOL si where "
			+ "(:city is null or si.CITY like %:city%)  and "
			+ "(:schoolName is null or si.SCHL_NAME like %:schoolName%)  and "
			+ "(:isMincodeListIncluded is null or si.MINCODE in :minCodeList) and ROWNUM <= 50",nativeQuery = true)			
	List<SchoolEntity> searchForSchool(String schoolName,String isMincodeListIncluded, Set<String> minCodeList, String city);

	@Query(value="select count(*) from TAB_SCHOOL sc \n" +
			"where sc.mincode = :minCode \n", nativeQuery=true)
	long countTabSchools(@Param("minCode") String minCode);

	@Query(value="SELECT si.* FROM TAB_SCHOOL si where "
			+ "(:schoolName is null or UPPER(si.SCHL_NAME) like %:schoolName%) and "
			+ "(:mincode is null or si.MINCODE like :mincode%)  and "
			+ "(:district is null or si.MINCODE like :district%) and ROWNUM <= 1000",nativeQuery = true)
	List<SchoolEntity> findSchools(String schoolName, String mincode, String district);
}
