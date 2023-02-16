package ca.bc.gov.educ.api.trax.model.entity;

import lombok.Data;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Data
public class StudentPsiKey implements Serializable {
    private static final long serialVersionUID = 1L;

    @Column(name = "PSI_CODE", insertable = false, updatable = false, length = 5)
    private String psiCode;

    @Column(name = "STUD_NO", insertable = false, updatable = false, length = 3)
    private String pen;


    public StudentPsiKey() {
    }

    public StudentPsiKey(String psiCode, String pen) {
        this.psiCode = psiCode;
        this.pen = pen;
    }
}
