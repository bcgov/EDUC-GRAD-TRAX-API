package ca.bc.gov.educ.api.trax.model.entity.institute;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.util.List;

@Entity(name = "districtRedisEntity")
@Data
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("District")
public class DistrictEntity {

    @org.springframework.data.annotation.Id
    @Id
    private String districtId;
    @Indexed
    private String districtNumber;
    private String faxNumber;
    private String phoneNumber;
    private String email;
    private String website;
    private String displayName;
    private String districtRegionCode;
    private String districtStatusCode;
    private List<DistrictContactEntity> contacts;
    private List<DistrictAddressEntity> addresses;
    private List<NoteEntity> notes;
    private String createUser;
    private String updateUser;
    private String createDate;
    private String updateDate;
}
