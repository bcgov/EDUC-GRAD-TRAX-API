package ca.bc.gov.educ.api.trax.util.criteria;

public class GradCriteria {

	String column;
	Object value;
	OperationEnum operation;
	
	
	public GradCriteria(String column, OperationEnum operation, Object value) {
		super();
		this.column = column;
		this.value = value;
		this.operation = operation;
	}

	public enum OperationEnum {
		IS_NULL, IS_NOT_NULL, EQUALS,NOT_EQUALS, LIKE, LESS_THAN, GREATER_THAN, LESS_THAN_EQUAL_TO, GREATER_THAN_EQUAL_TO,IN,STARTS_WITH_IGNORE_CASE;
	}

	public String getColumn() {
		return column;
	}

	public Object getValue() {
		return value;
	}
	
	public OperationEnum getOperation() {
		return operation;
	}
	
	
}
