package ca.bc.gov.educ.api.trax.repository;

import ca.bc.gov.educ.api.trax.model.entity.TraxUpdateInGradEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * The interface TRAX Student repository.
 */
@Repository
public interface TraxUpdateInGradRepository extends CrudRepository<TraxUpdateInGradEntity, BigDecimal> {

    @Query(value="SELECT tuge FROM TraxUpdateInGradEntity tuge \n"
    + "WHERE tuge.updateDate <= :currentDate \n"
    + "AND tuge.status = 'OUTSTANDING' \n"
    + "AND ROWNUM < :numberOfRecordsToPull \n"
    + "ORDER BY tuge.id")
    List<TraxUpdateInGradEntity> findOutstandingUpdates(@Param("currentDate") Date currentDate, int numberOfRecordsToPull);

    @Query(value="SELECT tuge FROM TraxUpdateInGradEntity tuge \n"
            + "WHERE tuge.pen = :pen \n"
            + "AND tuge.status = :status \n"
            + "ORDER BY tuge.pen, tuge.updateDate")
    List<TraxUpdateInGradEntity> findByStatusAndPen(@Param("status")String status, @Param("pen")String pen);

}
