package ca.bc.gov.educ.api.trax.model.entity;

import ca.bc.gov.educ.api.trax.model.entity.v2.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "EVENT_HISTORY")
@DynamicUpdate
public class EventHistoryEntity extends BaseEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator", parameters = {
            @org.hibernate.annotations.Parameter(name = "uuid_gen_strategy_class", value = "org.hibernate.id.uuid.CustomVersionOneStrategy")})
    @Column(name = "EVENT_HISTORY_ID", unique = true, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @NotNull
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "EVENT_ID", referencedColumnName = "REPLICATION_EVENT_ID", nullable = false)
    private EventEntity event;

    @Size(max = 1)
    @NotNull
    @Column(name = "ACKNOWLEDGE_FLAG", nullable = false, length = 1)
    private String acknowledgeFlag = "N";

}