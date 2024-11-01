package ca.bc.gov.educ.api.trax.model.dto.institute;

import ca.bc.gov.educ.api.trax.model.dto.SchoolContact;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Component("SchoolDetail")
public class SchoolDetail extends School {

    List<SchoolContact> contacts;
    List<SchoolAddress> addresses;
    List<Note> notes;
    List<Grade> grades;
    List<SchoolFundingGroup> schoolFundingGroups;
    List<NeighborhoodLearning> neighborhoodLearnings;
    List<SchoolMove> schoolMoves;

}
