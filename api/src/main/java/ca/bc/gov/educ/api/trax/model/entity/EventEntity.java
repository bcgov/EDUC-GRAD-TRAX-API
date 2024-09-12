package ca.bc.gov.educ.api.trax.model.entity;

import lombok.*;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * The type Grad Status event.
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "REPLICATION_EVENT")
@Data
@DynamicUpdate
public class EventEntity {
  /**
   * The Create user.
   */
  @Column(name = "CREATE_USER", updatable = false)
  String createUser;
  /**
   * The Create date.
   */
  @Column(name = "CREATE_DATE", updatable = false)
  @PastOrPresent
  LocalDateTime createDate;
  /**
   * The Update user.
   */
  @Column(name = "UPDATE_USER")
  String updateUser;
  /**
   * The Update date.
   */
  @Column(name = "UPDATE_DATE")
  @PastOrPresent
  LocalDateTime updateDate;
  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator", parameters = {
      @org.hibernate.annotations.Parameter(name = "uuid_gen_strategy_class", value = "org.hibernate.id.uuid.CustomVersionOneStrategy")})
  @Column(name = "REPLICATION_EVENT_ID", unique = true, updatable = false, columnDefinition = "BINARY(16)")
  private UUID replicationEventId;
  /**
   * The EventEntity id.
   */
  @Column(name = "EVENT_ID", unique = true, updatable = false, columnDefinition = "BINARY(16)")
  private UUID eventId;
  /**
   * The EventEntity payload.
   */
  @NotNull(message = "eventPayload cannot be null")
  @Lob
  @Column(name = "EVENT_PAYLOAD")
  @ToString.Exclude
  private byte[] eventPayloadBytes;
  /**
   * The EventEntity status.
   */
  @NotNull(message = "eventStatus cannot be null")
  @Column(name = "EVENT_STATUS")
  private String eventStatus;
  /**
   * The EventEntity type.
   */
  @NotNull(message = "eventType cannot be null")
  @Column(name = "EVENT_TYPE")
  private String eventType;
  /**
   * The EventEntity outcome.
   */
  @NotNull(message = "eventOutcome cannot be null.")
  @Column(name = "EVENT_OUTCOME")
  private String eventOutcome;

  /**
   * The Activity code.
   */
  @Column(name = "ACTIVITY_CODE")
  private String activityCode;

  @OneToOne(cascade = CascadeType.ALL, mappedBy = "event")
  private EventHistoryEntity eventHistoryEntity;

  /**
   * Gets event payload.
   *
   * @return the event payload
   */
  public String getEventPayload() {
    return new String(this.getEventPayloadBytes(), StandardCharsets.UTF_8);
  }

  /**
   * Sets event payload.
   *
   * @param eventPayload the event payload
   */
  public void setEventPayload(final String eventPayload) {
    this.setEventPayloadBytes(eventPayload.getBytes(StandardCharsets.UTF_8));
  }

  /**
   * The type Student event builder.
   */
  public static class EventEntityBuilder {
    /**
     * The EventEntity payload bytes.
     */
    byte[] eventPayloadBytes;

    /**
     * EventEntity payload student event . student event builder.
     *
     * @param eventPayload the event payload
     * @return the student event . student event builder
     */
    public EventEntityBuilder eventPayload(final String eventPayload) {
      this.eventPayloadBytes = eventPayload.getBytes(StandardCharsets.UTF_8);
      return this;
    }
  }
}
