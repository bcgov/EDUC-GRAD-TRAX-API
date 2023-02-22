package ca.bc.gov.educ.api.trax.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * The type TRAX Update entity.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "UPDATE_IN_GRAD")
public class TraxUpdateInGradEntity {
    @Id
    @Column(name = "UPDATE_IN_GRAD_ID", nullable=false, updatable = false)
    private BigDecimal id;

    @Column(name = "STUD_NO", updatable = false)
    private String pen;

    @Column(name = "TRAX_UPDATE_DATE", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateDate;

    @Column(name = "UPDATE_STATUS")
    private String status;

    @Column(name = "UPDATE_TYPE")
    private String updateType;
}
