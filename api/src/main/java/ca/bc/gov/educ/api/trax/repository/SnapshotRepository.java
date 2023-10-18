package ca.bc.gov.educ.api.trax.repository;

import ca.bc.gov.educ.api.trax.model.dto.SnapshotResponse;
import ca.bc.gov.educ.api.trax.model.entity.SnapshotEntity;
import ca.bc.gov.educ.api.trax.model.entity.SnapshotID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SnapshotRepository extends JpaRepository<SnapshotEntity, SnapshotID> {

    @Query(value="select s.mincode as schoolOfRecord from (\n" +
            " select mincode, count(*) from SNAPSHOT\n" +
            " where grad_year = :gradYear\n" +
            " group by mincode\n" +
            " order by 2 desc ) s", nativeQuery=true)
    List<String> getSchools(@Param("gradYear") Integer gradYear);

    @Query(value="select new ca.bc.gov.educ.api.trax.model.dto.SnapshotResponse(trim(s.pen), trim(s.graduatedDate), s.gpa, trim(s.honourFlag), trim(s.schoolOfRecord))\n" +
            "from SnapshotEntity s\n" +
            "where s.gradYear = :gradYear")
    List<SnapshotResponse> getStudentsByGradYear(@Param("gradYear") Integer gradYear);

    @Query(value="select new ca.bc.gov.educ.api.trax.model.dto.SnapshotResponse(trim(s.pen), trim(s.graduatedDate), s.gpa, trim(s.honourFlag), trim(s.schoolOfRecord))\n" +
            "from SnapshotEntity s\n" +
            "where s.gradYear = :gradYear\n" +
            "and s.schoolOfRecord = :schoolOfRecord")
    List<SnapshotResponse> getStudentsByGradYearAndSchoolOfRecord(@Param("gradYear") Integer gradYear, @Param("schoolOfRecord") String schoolOfRecord);

    // Paginated support by gradYear
    Page<SnapshotEntity> findByGradYear(Integer gradYear, Pageable pageable);

    Integer countAllByGradYear(Integer gradYear);

    // Paginated support by gradYear & schoolOfRecord
    Page<SnapshotEntity> findByGradYearAndSchoolOfRecord(Integer gradYear, String schoolOfRecord, Pageable pageable);

    Integer countAllByGradYearAndSchoolOfRecord(Integer gradYear, String schoolOfRecord);

}
