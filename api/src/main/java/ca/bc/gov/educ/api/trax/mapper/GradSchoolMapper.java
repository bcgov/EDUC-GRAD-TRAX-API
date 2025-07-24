package ca.bc.gov.educ.api.trax.mapper;

import ca.bc.gov.educ.api.trax.model.dto.GradSchool;
import ca.bc.gov.educ.api.trax.model.dto.institute.School;
import ca.bc.gov.educ.api.trax.model.entity.institute.SchoolEntity;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;

@Mapper
public interface GradSchoolMapper {
    GradSchoolMapper mapper = Mappers.getMapper(GradSchoolMapper.class);

    @Mapping(target = "canIssueTranscripts", expression = "java(getCanIssueTranscriptFromMap(schoolEntity.getSchoolId(), gradSchoolMap))")
    @Mapping(target = "canIssueCertificates", expression = "java(getCanIssueCertificateFromMap(schoolEntity.getSchoolId(), gradSchoolMap))")
    School toStructure(SchoolEntity schoolEntity, @Context Map<String, GradSchool> gradSchoolMap);


    List<School> toSchools(List<SchoolEntity> schoolEntities, @Context Map<String, GradSchool> gradSchoolMap);

    default Boolean getCanIssueTranscriptFromMap(String id, Map<String, GradSchool> map) {
        return map.containsKey(id) && map.get(id).getCanIssueTranscripts().equals("Y");
    }

    default Boolean getCanIssueCertificateFromMap(String id, Map<String, GradSchool> map) {
        return map.containsKey(id) && map.get(id).getCanIssueCertificates().equals("Y");
    }
}
