package ca.bc.gov.educ.api.trax.repository.redis;

import ca.bc.gov.educ.api.trax.model.entity.institute.SchoolDetailEntity;
import ca.bc.gov.educ.api.trax.service.institute.SchoolSearchCriteria;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SchoolSearchSpecification implements Specification<SchoolDetailEntity> {

    private static final Logger logger = LoggerFactory.getLogger(SchoolSearchSpecification.class);

    private final SchoolSearchCriteria searchCriteria;

    public SchoolSearchSpecification(SchoolSearchCriteria searchCriteria) {
        this.searchCriteria = searchCriteria;
    }

    @Override
    @Nullable
    public Predicate toPredicate(Root<SchoolDetailEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
        logger.debug("toPredicate()");
        List<Predicate> predicates = new ArrayList<>();
        if (StringUtils.isNotBlank(searchCriteria.getDisplayName())) {
            Predicate schoolNamePredicate;
                schoolNamePredicate = criteriaBuilder.like(criteriaBuilder.upper(root.get("displayName")), "%" + searchCriteria.getDisplayName().toUpperCase(Locale.ROOT) + "%");
            predicates.add(schoolNamePredicate);
        }
        /*if (StringUtils.isNotBlank(searchCriteria.getMincode())) {
            Predicate minCodePredicate;
            if(searchCriteria.isMincodeWildCard())
                minCodePredicate = criteriaBuilder.like(root.get("mincode"), searchCriteria.getMincode() + "%");
            else
                minCodePredicate = criteriaBuilder.equal(root.get("mincode"), searchCriteria.getMincode());
            predicates.add(minCodePredicate);
        }
        if (StringUtils.isNotBlank(searchCriteria.getDistrictId())) {
            Predicate districtIdPredicate;
            if(searchCriteria.isDistrictIdWildCard())
                districtIdPredicate = criteriaBuilder.like(criteriaBuilder.upper(root.get("districtId")), "%" + searchCriteria.getDistrictId().toUpperCase(Locale.ROOT) + "%");
            else
                districtIdPredicate = criteriaBuilder.equal(criteriaBuilder.upper(root.get("districtId")), searchCriteria.getDistrictId().toUpperCase(Locale.ROOT));
            predicates.add(districtIdPredicate);
        }*/
        return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
    }
}
