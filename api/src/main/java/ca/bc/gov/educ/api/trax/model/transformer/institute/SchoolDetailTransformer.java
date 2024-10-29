package ca.bc.gov.educ.api.trax.model.transformer.institute;

import ca.bc.gov.educ.api.trax.model.dto.institute.SchoolDetail;
import ca.bc.gov.educ.api.trax.model.entity.institute.SchoolDetailEntity;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component("SchoolDetailTransformer")
public class SchoolDetailTransformer {

    ModelMapper modelMapper;

    @Autowired
    public SchoolDetailTransformer(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public SchoolEntity transformToSchoolEntity(final SchoolDetail schoolDetail) {
        return SchoolEntity.builder()
                .schoolId(schoolDetail.getSchoolId())
                .districtId(schoolDetail.getDistrictId())
                .mincode(schoolDetail.getMincode())
                .independentAuthorityId(schoolDetail.getIndependentAuthorityId())
                .schoolNumber(schoolDetail.getSchoolNumber())
                .faxNumber(schoolDetail.getFaxNumber())
                .phoneNumber(schoolDetail.getPhoneNumber())
                .email(schoolDetail.getEmail())
                .website(schoolDetail.getWebsite())
                .displayName(schoolDetail.getDisplayName())
                .displayNameNoSpecialChars(schoolDetail.getDisplayNameNoSpecialChars())
                .schoolReportingRequirementCode(schoolDetail.getSchoolReportingRequirementCode())
                .schoolOrganizationCode(schoolDetail.getSchoolOrganizationCode())
                .schoolCategoryCode(schoolDetail.getSchoolCategoryCode())
                .facilityTypeCode(schoolDetail.getFacilityTypeCode())
                .openedDate(schoolDetail.getOpenedDate())
                .closedDate(schoolDetail.getClosedDate())
                .canIssueTranscripts(schoolDetail.isCanIssueTranscripts())
                .canIssueCertificates(schoolDetail.isCanIssueCertificates())
                .build();
    }

    public SchoolDetail transformToDTO (SchoolDetailEntity schoolDetailEntity) {
        return modelMapper.map(schoolDetailEntity, SchoolDetail.class);
    }

    public SchoolDetail transformToDTO (Optional<SchoolDetailEntity> schoolDetailEntity) {
        if (schoolDetailEntity.isPresent()) {
            SchoolDetailEntity sde = schoolDetailEntity.get();
	       return modelMapper.map(sde, SchoolDetail.class);
        }
        return null;
    }

	public List<SchoolDetail> transformToDTO (Iterable<SchoolDetailEntity> schoolDetailEntities ) {
        return modelMapper.map(schoolDetailEntities, new TypeToken<List<SchoolDetail>>(){}.getType());
    }

    public SchoolDetailEntity transformToEntity(SchoolDetail schoolDetail) {
        return modelMapper.map(schoolDetail, SchoolDetailEntity.class);
    }

    public List<SchoolDetailEntity> transformToEntity (List<SchoolDetail> schoolDetails ) {
        if (schoolDetails == null) return null;
        return schoolDetails
                .stream()
                .map(sd -> modelMapper.map(sd, SchoolDetailEntity.class))
                .collect(Collectors.toList());
    }
}
