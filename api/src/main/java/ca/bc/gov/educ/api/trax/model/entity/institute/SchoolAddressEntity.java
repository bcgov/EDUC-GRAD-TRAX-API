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
@RedisHash("SchoolAddress")
public class SchoolAddressEntity implements Serializable {

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