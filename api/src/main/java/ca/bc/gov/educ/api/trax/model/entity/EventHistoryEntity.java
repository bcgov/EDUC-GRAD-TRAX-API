package ca.bc.gov.educ.api.trax.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "EVENT_HISTORY")
public class EventHistoryEntity {
    @Id
    @ColumnDefault("SYS_GUID()")
    @Column(name = "EVENT_HISTORY_ID", nullable = false)
    private UUID id;

    @NotNull
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "EVENT_ID", nullable = false)
    private EventEntity event;

    @Size(max = 1)
    @NotNull
    @Column(name = "ACKNOWLEDGE_FLAG", nullable = false, length = 1)
    private String acknowledgeFlag;

    @Size(max = 32)
    @NotNull
    @ColumnDefault("USER")
    @Column(name = "CREATE_USER", nullable = false, length = 32)
    private String createUser;

    @NotNull
    @ColumnDefault("SYSTIMESTAMP")
    @Column(name = "CREATE_DATE", nullable = false)
    private LocalDate createDate;

    @Size(max = 32)
    @NotNull
    @ColumnDefault("USER")
    @Column(name = "UPDATE_USER", nullable = false, length = 32)
    private String updateUser;

    @NotNull
    @ColumnDefault("SYSTIMESTAMP")
    @Column(name = "UPDATE_DATE", nullable = false)
    private LocalDate updateDate;

}