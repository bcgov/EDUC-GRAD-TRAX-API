package ca.bc.gov.educ.api.trax.model.entity.v2;

import ca.bc.gov.educ.api.trax.util.EducGradTraxApiConstants;
import ca.bc.gov.educ.api.trax.util.ThreadLocalStateUtil;
import jakarta.persistence.*;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;

@Data
@MappedSuperclass
public class BaseEntity {

	@Column(name = "CREATE_USER", updatable = false)
	String createUser;

	@Column(name = "CREATE_DATE", updatable = false)
	@PastOrPresent
	LocalDateTime createDate;

	@Column(name = "UPDATE_USER")
	String updateUser;

	@Column(name = "UPDATE_DATE")
	@PastOrPresent
	LocalDateTime updateDate;
	
	@PrePersist
	protected void onCreate() {
		initUserInfo();
		this.createDate = LocalDateTime.now();
		this.updateDate = LocalDateTime.now();
	}

	@PreUpdate
	protected void onPersist() {
		initUserInfo();
		this.createDate = (this.createDate == null) ? LocalDateTime.now() : this.createDate;
		this.updateDate = LocalDateTime.now();
	}

	private void initUserInfo() {
		String user = ThreadLocalStateUtil.getCurrentUser();
		this.updateUser = (StringUtils.isBlank(user)) ? EducGradTraxApiConstants.DEFAULT_UPDATED_BY : user;
		this.createUser = (StringUtils.isBlank(createUser) && StringUtils.isBlank(user)) ? EducGradTraxApiConstants.DEFAULT_CREATED_BY : user;
	}
}
