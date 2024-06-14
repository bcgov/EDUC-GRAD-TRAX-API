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
@RedisHash("SchoolAddress")
public class SchoolAddressEntity {

    @Id
    private String schoolAddressId;
    @Indexed
    private String schoolId;
    @Indexed
    private String addressTypeCode;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String postal;
    private String provinceCode;
    private String countryCode;
    private String createUser;
    private String updateUser;
    private String createDate;
    private String updateDate;
}