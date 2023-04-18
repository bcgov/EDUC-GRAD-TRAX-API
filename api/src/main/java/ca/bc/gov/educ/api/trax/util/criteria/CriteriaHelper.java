package ca.bc.gov.educ.api.trax.util.criteria;

import ca.bc.gov.educ.api.trax.util.criteria.GradCriteria.OperationEnum;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;

import java.util.*;

public class CriteriaHelper {

	private static final Logger LOGGER = Logger.getLogger(CriteriaHelper.class);
	
	List<GradCriteria> criteriaList = new ArrayList<>();

	private Map<String, Boolean> orderBy = new HashMap<>();
	
	private Integer maxResults = null;
	
	private boolean noLimit= true;
	
	/**
	 * Creates the Criterias needed for Criteria Query
	 * This version should only be used where no value is required (e.g. IS_NOT_NULL, IS_NULL)
	 * @param column
	 * @param operation
	 */
	public CriteriaHelper add(String column, OperationEnum operation) {
		return add(column, operation, null);
		
	}
	
	/**
	 * Creates the Criterias needed for Criteria Query
	 * Will check for nulls and blanks before adding.
	 * @param column
	 * @param operation
	 * @param value
	 */
	public CriteriaHelper add(String column, OperationEnum operation, Object value) {
		if (operation == OperationEnum.IS_NOT_NULL || operation == OperationEnum.IS_NULL) {
			//IS_NULL and IS_NOT_NULL do not need a value
			LOGGER.debug(String.format("Adding Operation: [%s] for column: [%s]", operation.name(), column));
			criteriaList.add(new GradCriteria(column, operation, value));
		} else {
			if (value != null) {
				if (value instanceof String) {
					if (StringUtils.isNotBlank((String)value)) {
						LOGGER.debug(String.format("Adding value: [%s] with Operation: [%s] for column: [%s]", value, operation.name(), column));
						criteriaList.add(new GradCriteria(column, operation, value));
					}
				} else {
					LOGGER.debug(String.format("Adding value: [%s] with Operation: [%s] for column: [%s]", value, operation.name(), column));
					criteriaList.add(new GradCriteria(column, operation, value));
				}
			}
		}
		return this;
		
	}
	
	/**
	 * Order by the specified column (can be nested).
	 * 
	 * @param column: column
	 * @param asc True for ASC sort, false for DESC sort
	 * @return
	 */
	public CriteriaHelper orderBy(String column, Boolean asc) {
		orderBy.put(column, asc);
		return this;
	}
	
	Collection<GradCriteria> getCriteria() {
		return this.criteriaList;
	}

	public Map<String, Boolean> getOrderBy() {
		return orderBy;
	}

	public Integer getMaxResults() {
		return maxResults;
	}

	public void setMaxResults(Integer maxResults) {
		this.maxResults = maxResults;
	}

	public boolean isNoLimit() {
		return noLimit;
	}

	public void setNoLimit(boolean noLimit) {
		this.noLimit = noLimit;
	}
	
	

}
