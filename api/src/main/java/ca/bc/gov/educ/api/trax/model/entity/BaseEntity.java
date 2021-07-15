package ca.bc.gov.educ.api.trax.model.entity;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.util.Date;

@Data
@MappedSuperclass
public class BaseEntity {
	@Column(name = "CREATE_USER", nullable = false)
    private String createUser;
	
	@Column(name = "CREATE_DATE", nullable = false)
    private Date createDate;
	
	@Column(name = "UPDATE_USER", nullable = false)
    private String updateUser;
	
	@Column(name = "UPDATE_DATE", nullable = false)
    private Date updateDate;
	
	@PrePersist
	protected void onCreate() {
		this.updateUser = "API_COURSE";
		this.createUser = "API_COURSE";
		this.createDate = new Date(System.currentTimeMillis());
		this.updateDate = new Date(System.currentTimeMillis());
	}

	@PreUpdate
	protected void onPersist() {
		this.updateDate = new Date(System.currentTimeMillis());
		this.updateUser = "API_COURSE";
		if (StringUtils.isBlank(createUser)) {
			createUser = "API_COURSE";
		}
		if (this.createDate == null) {
			this.createDate = new Date(System.currentTimeMillis());
		}
	}
}
