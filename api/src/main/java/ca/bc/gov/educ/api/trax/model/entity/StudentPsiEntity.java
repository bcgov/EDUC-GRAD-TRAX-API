package ca.bc.gov.educ.api.trax.model.entity;

import lombok.Data;
import org.hibernate.annotations.Immutable;

import jakarta.persistence.*;

@Data
@Immutable
@Entity
@Table(name = "STUD_PSI")
public class StudentPsiEntity {

    @EmbeddedId
    private StudentPsiKey studentPsiKey;

    @Column(name = "PSI_YEAR")
    private String psiYear;

}
