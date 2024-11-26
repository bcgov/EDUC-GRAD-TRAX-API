package ca.bc.gov.educ.api.trax.model.entity.institute;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.io.Serializable;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("NeighborhoodLearning")
public class NeighborhoodLearningEntity  implements Serializable {

    @Id
    private String neighborhoodLearningId;
    @Indexed
    private String schoolId;
    @Indexed
    private String neighborhoodLearningTypeCode;
    private String createUser;
    private String updateUser;
    private String createDate;
    private String updateDate;
}