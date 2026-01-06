package ca.bc.gov.educ.api.trax.repository;

import java.util.List;

public interface StudentPsiRepositoryCustom {
    List<Object[]> findStudentsUsingPSI(String transmissionMode, String psiYear, String psiCodeProvided, List<String> psiCode);
}
