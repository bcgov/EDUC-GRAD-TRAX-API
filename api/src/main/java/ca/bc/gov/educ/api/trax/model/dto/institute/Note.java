package ca.bc.gov.educ.api.trax.model.dto.institute;

import ca.bc.gov.educ.api.trax.model.dto.BaseModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

@Data
@EqualsAndHashCode(callSuper = true)
@Component("note")
public class Note extends BaseModel {

    private String noteId;
    private String schoolId;
    private String districtId;
    private String independentAuthorityId;
    private String content;

}
