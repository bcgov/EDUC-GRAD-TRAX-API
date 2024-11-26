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
@RedisHash("Grade")
public class GradeEntity  implements Serializable {

    @Id
    private String schoolGradeId;
    @Indexed
    private String schoolId;
    @Indexed
    private String schoolGradeCode;
    private String createUser;
    private String updateUser;
    private String createDate;
    private String updateDate;
}