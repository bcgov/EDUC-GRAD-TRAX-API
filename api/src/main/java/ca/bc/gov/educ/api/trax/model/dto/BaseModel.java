package ca.bc.gov.educ.api.trax.model.dto;

import lombok.Data;

import java.sql.Date;

@Data
public class BaseModel {
	private String createUser;
	private Date createDate;
	private String updateUser;
	private Date updateDate;
}
