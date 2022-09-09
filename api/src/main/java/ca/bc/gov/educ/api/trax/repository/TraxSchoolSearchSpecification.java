package ca.bc.gov.educ.api.trax.repository;

import ca.bc.gov.educ.api.trax.model.entity.SchoolEntity;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public class TraxSchoolSearchSpecification implements Specification<SchoolEntity> {

    private static final Logger logger = LoggerFactory.getLogger(TraxSchoolSearchSpecification.class);

    private final TraxSchoolSearchCriteria searchCriteria;

    public TraxSchoolSearchSpecification(TraxSchoolSearchCriteria searchCriteria) {
        this.searchCriteria = searchCriteria;
    }

    @Override
    @Nullable
    public Predicate toPredicate(Root<SchoolEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
        logger.debug("toPredicate()");
        if (StringUtils.isNotBlank(searchCriteria.getSchoolName())) {
            return criteriaBuilder.like(root.get("schoolName"), "%" + searchCriteria.getSchoolName() + "%");
        } else if (StringUtils.isNotBlank(searchCriteria.getMinCode())) {
            return criteriaBuilder.like(root.get("minCode"), searchCriteria.getMinCode() + "%");
        } else if (StringUtils.isNotBlank(searchCriteria.getDistrict())) {
            return criteriaBuilder.like(root.get("minCode"), searchCriteria.getDistrict() + "%");
        }
        return criteriaBuilder.and();
    }
}
