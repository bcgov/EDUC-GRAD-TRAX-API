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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
        List<Predicate> predicates = new ArrayList<>();
        if (StringUtils.isNotBlank(searchCriteria.getSchoolName())) {
            Predicate schoolNamePredicate;
            if(searchCriteria.isSchoolNameWildCard()) {
                schoolNamePredicate = criteriaBuilder.like(criteriaBuilder.upper(root.get("schoolName")), "%" + searchCriteria.getSchoolName().toUpperCase(Locale.ROOT) + "%");
            } else {
                schoolNamePredicate = criteriaBuilder.equal(criteriaBuilder.upper(root.get("schoolName")), searchCriteria.getSchoolName().toUpperCase(Locale.ROOT));
            }
            predicates.add(schoolNamePredicate);
        }
        if (StringUtils.isNotBlank(searchCriteria.getMinCode())) {
            Predicate minCodePredicate;
            if(searchCriteria.isMinCodeWildCard()) {
                minCodePredicate = criteriaBuilder.like(root.get("minCode"), searchCriteria.getMinCode() + "%");
            } else {
                minCodePredicate = criteriaBuilder.equal(root.get("minCode"), searchCriteria.getMinCode());
            }
            predicates.add(minCodePredicate);
        } else if (StringUtils.isNotBlank(searchCriteria.getDistrict())) {
            Predicate districtPredicate = criteriaBuilder.like(root.get("minCode"), searchCriteria.getDistrict() + "%");
            predicates.add(districtPredicate);
        }
        return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
    }
}
