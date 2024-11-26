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
@RedisHash("Note")
public class SchoolMoveEntity  implements Serializable {

    @Id
    private String schoolMoveId;
    @Indexed
    private String toSchoolId;
    @Indexed
    private String fromSchoolId;
    @Indexed
    private String moveDate;
    private String createUser;
    private String updateUser;
    private String createDate;
    private String updateDate;
}