package ca.bc.gov.educ.api.trax.model.entity.institute;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("SchoolFundingGroupCode")
public class SchoolFundingGroupCodeEntity {

    @Id
    private String schoolFundingGroupCode;
    private String label;
    private String description;
    private String displayOrder;
    private String effectiveDate;
    private String expiryDate;
}