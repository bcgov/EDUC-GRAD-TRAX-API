package ca.bc.gov.educ.api.trax.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "TRAX_STUDENT_NO")
public class TraxStudentNoEntity {
  @Id
  @Column(name = "STUD_NO", unique = true, updatable = false)
  private String studNo;

  @Column(name = "STATUS", nullable = false)
  private String status;
}
