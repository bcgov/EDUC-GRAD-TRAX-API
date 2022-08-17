package ca.bc.gov.educ.api.trax.repository;

import ca.bc.gov.educ.api.trax.model.entity.PsiEntity;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.LinkedList;

public class PsiSearchSpecification implements Specification<PsiEntity> {

    private static final Logger logger = LoggerFactory.getLogger(PsiSearchSpecification.class);

    private final PsiSearchCriteria searchCriteria;

    public PsiSearchSpecification(PsiSearchCriteria searchCriteria) {
        this.searchCriteria = searchCriteria;
    }

    @Override
    @Nullable
    public Predicate toPredicate(Root<PsiEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
        logger.debug("toPredicate()");
        Predicate whereClause = criteriaBuilder.and();
        LinkedList<Predicate> conditions = new LinkedList<Predicate>();
        if (StringUtils.isNotBlank(searchCriteria.getPsiName())) {
            Predicate psiNameCondition = criteriaBuilder.like(root.get("psiName"), "%" + searchCriteria.getPsiName() + "%");
            conditions.add(psiNameCondition);
        }
        if (StringUtils.isNotBlank(searchCriteria.getPsiCode())) {
            Predicate psiCodeCondition = criteriaBuilder.like(root.get("psiCode"), searchCriteria.getPsiCode() + "%");
            conditions.add(psiCodeCondition);
        }
        if (StringUtils.isNotBlank(searchCriteria.getCslCode())) {
            Predicate cslCodeCondition = criteriaBuilder.like(root.get("cslCode"), searchCriteria.getCslCode() + "%");
            conditions.add(cslCodeCondition);
        }
        if (StringUtils.isNotBlank(searchCriteria.getTransmissionMode())) {
            Predicate transmissionModeCondition = criteriaBuilder.like(root.get("transmissionMode"), searchCriteria.getTransmissionMode() + "%");
            conditions.add(transmissionModeCondition);
        }
        if (StringUtils.isNotBlank(searchCriteria.getOpenFlag())) {
            Predicate openFlagCondition = criteriaBuilder.equal(root.get("openFlag"), searchCriteria.getOpenFlag());
            conditions.add(openFlagCondition);
        }
        if(!conditions.isEmpty()) {
            whereClause = criteriaBuilder.and(conditions.toArray(new Predicate[conditions.size()]));
        }
        return whereClause;
    }
}
