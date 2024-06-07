package ca.bc.gov.educ.api.trax.model.dto.institute;

import ca.bc.gov.educ.api.trax.model.dto.BaseModel;
import ca.bc.gov.educ.api.trax.model.entity.institute.DistrictAddressEntity;
import ca.bc.gov.educ.api.trax.model.entity.institute.DistrictContactEntity;
import ca.bc.gov.educ.api.trax.model.entity.institute.NoteEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Component("InstituteDistrict")
public class District extends BaseModel {

    private String districtId;
    private String districtNumber;
    private String faxNumber;
    private String phoneNumber;
    private String email;
    private String website;
    private String districtRegionCode;
    private String districtStatusCode;
    private List<DistrictContactEntity> contacts;
    private List<DistrictAddressEntity> addresses;
    private List<NoteEntity> notes;

}
