package ca.bc.gov.educ.api.trax.repository;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Data
@Getter
@Setter
@Builder
public class PsiSearchCriteria implements Serializable {

    private static final long serialVersionUID = 1L;

    String psiName, psiCode, cslCode, transmissionMode, openFlag;

}
