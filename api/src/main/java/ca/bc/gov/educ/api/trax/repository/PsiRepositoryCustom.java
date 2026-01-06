package ca.bc.gov.educ.api.trax.repository;

import ca.bc.gov.educ.api.trax.model.entity.PsiEntity;

import java.util.List;

public interface PsiRepositoryCustom {
    List<PsiEntity> searchForPSI(String psiName);
}

