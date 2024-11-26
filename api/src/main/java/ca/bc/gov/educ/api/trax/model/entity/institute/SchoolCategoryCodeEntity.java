package ca.bc.gov.educ.api.trax.model.entity.institute;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("SchoolCategoryCode")
public class SchoolCategoryCodeEntity {

    @org.springframework.data.annotation.Id
    @Id
    private String schoolCategoryCode;
    private String label;
    private String description;
    private String legacyCode;
    private String displayOrder;
    private String effectiveDate;
    private String expiryDate;
}