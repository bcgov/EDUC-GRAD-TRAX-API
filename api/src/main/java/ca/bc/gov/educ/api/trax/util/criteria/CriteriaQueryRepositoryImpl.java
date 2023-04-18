package ca.bc.gov.educ.api.trax.util.criteria;

import ca.bc.gov.educ.api.trax.util.criteria.GradCriteria.OperationEnum;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.metamodel.mapping.ordering.ast.PluralAttributePath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import jakarta.persistence.criteria.CriteriaBuilder.In;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Repository
public class CriteriaQueryRepositoryImpl<T> implements CriteriaQueryRepository<T> {

	private static final int MAX_RESULTS_DEFAULT = 1000;
	private static final int MAX_RESULTS_DEFAULT_EXPORT_TO_EXCEL = 100000;
	@Autowired
	protected EntityManager em;

	@Override
	public List<T> findByCriteria(CriteriaHelper criteriaHelper, Class<T> type) {
		CriteriaBuilder cb = em.getCriteriaBuilder();

		CriteriaQuery<T> cq = cb.createQuery(type);

		Root<T> root = cq.from(type);

		List<Predicate> predicates = buildPredicates(criteriaHelper, cb, root);
		cq.where(predicates.toArray(new Predicate[0]));

		buildOrderBy(criteriaHelper, cb, cq, root);
		
		TypedQuery<T> query = em.createQuery(cq);
		
		addResultLimit(criteriaHelper, query);
		
		return query.getResultList();
	}

	protected void addResultLimit(CriteriaHelper criteriaHelper, TypedQuery<T> query) {
		if(!criteriaHelper.isNoLimit()) {
			if (criteriaHelper.getMaxResults() != null && criteriaHelper.getMaxResults() > 0) {
				query.setMaxResults(criteriaHelper.getMaxResults());
			} else {
				query.setMaxResults(MAX_RESULTS_DEFAULT);
			}
		}else {
			query.setMaxResults(MAX_RESULTS_DEFAULT_EXPORT_TO_EXCEL);
		}
		
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected List<Predicate> buildPredicates(CriteriaHelper criteriaHelper, CriteriaBuilder cb, Root<T> root) {
		List<Predicate> predicates = new ArrayList<>();

		Collection<GradCriteria> criteriaList = criteriaHelper.getCriteria();
		OperationEnum operation = null;
		for (GradCriteria criteria : criteriaList) {
			operation = criteria.getOperation();
			switch (operation) {
			case EQUALS:
				predicates.add(cb.equal(buildExpression(root, criteria.getColumn()), criteria.getValue()));
				break;
			case NOT_EQUALS:
				predicates.add(cb.notEqual(buildExpression(root, criteria.getColumn()), criteria.getValue()));
				break;
			case GREATER_THAN:
				predicates.add(cb.greaterThan(root.get(criteria.getColumn()), (Comparable) criteria.getValue()));
				break;
			case GREATER_THAN_EQUAL_TO:
				predicates.add(cb.greaterThanOrEqualTo(root.get(criteria.getColumn()), (Comparable) criteria.getValue()));
				break;
			case IS_NULL:
				predicates.add(cb.isNull(root.get(criteria.getColumn())));
				break;
			case IS_NOT_NULL:
				predicates.add(cb.isNotNull(root.get(criteria.getColumn())));
				break;
			case LESS_THAN:
				predicates.add(cb.lessThan(root.get(criteria.getColumn()), (Comparable) criteria.getValue()));
				break;
			case LESS_THAN_EQUAL_TO:
				predicates.add(cb.lessThanOrEqualTo(root.get(criteria.getColumn()), (Comparable) criteria.getValue()));
				break;
			case LIKE:
				predicates.add(cb.like(root.get(criteria.getColumn()), "%"+criteria.getValue().toString()+"%"));
				break;
			case STARTS_WITH_IGNORE_CASE:
				predicates.add(cb.like((Expression<String>) buildExpression(root, criteria.getColumn()), criteria.getValue() + "%"));
				break;
			case IN:
				Object value = criteria.getValue();
				if (value instanceof Integer[]) {
					In<Integer> in = (In<Integer>) cb.in(buildExpression(root, criteria.getColumn()));
					Integer[] values = (Integer[]) value;
					for (int j = 0; j < values.length; j++) {
						in.value(values[j]);
					}
					predicates.add(in);
				} else {
					In<String> in = (In<String>) cb.in(buildExpression(root, criteria.getColumn()));
					String[] values = (String[]) value;
					for (int j = 0; j < values.length; j++) {
						in.value(values[j]);
					}
					predicates.add(in);
				}
				break;
			default:
				throw new RuntimeException("Unable to determine criteria for: " + operation);
			}
		}
		return predicates;
	}
	
	protected void buildOrderBy(CriteriaHelper criteriaHelper, CriteriaBuilder cb, CriteriaQuery<T> cq, Root<T> root) {
		
		criteriaHelper.getOrderBy().forEach((column, ascending) -> {
			Expression<?> expression = buildExpression(root, column);
			Order order = ascending ? cb.asc(expression) : cb.desc(expression);

			cq.orderBy(order);
		});
	}

	/**
	 * Build an expression which supports nested properties.
	 * 
	 * @param root
	 * @param column
	 * @return
	 */
	private Expression<?> buildExpression(Root<T> root, String column) {
		String[] attributes = StringUtils.split(column, ".");
		Path<?> path = root;

		for (String attribute : attributes) {
			Path<?> nextPath = path.get(attribute);
			if (nextPath instanceof PluralAttributePath) {
				From<?, ?> from = (From<?, ?>) path;
				path = from.join(attribute, JoinType.INNER);
			} else {
				path = nextPath;
			}
		}
		return path;
	}

}
