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
public class NoteEntity {

    @Id
    private String noteId;
    @Indexed
    private String schoolId;
    @Indexed
    private String districtId;
    private String independentAuthorityId;
    private String content;
    private String createUser;
    private String updateUser;
    private String createDate;
    private String updateDate;
}