package ca.bc.gov.educ.api.trax.model.entity.institute;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("Note")
public class SchoolMoveEntity {

    @Id
    private String schoolMoveId;
    @Indexed
    private String toSchoolId;
    @Indexed
    private String fromSchoolId;
    private String moveDate;
    private String createUser;
    private String updateUser;
    private String createDate;
    private String updateDate;
}