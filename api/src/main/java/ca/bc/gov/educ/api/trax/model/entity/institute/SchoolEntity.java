package ca.bc.gov.educ.api.trax.model.entity.institute;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@RedisHash("School")
public class SchoolEntity {

    @Id
    private String schoolId;
    @Indexed
    private String districtId;
    @Indexed
    private String mincode;
    private String independentAuthorityId;
    private String schoolNumber;
    private String faxNumber;
    private String phoneNumber;
    private String email;
    private String website;
    private String displayName;
    private String displayNameNoSpecialChars;
    private String schoolReportingRequirementCode;
    private String schoolOrganizationCode;
    @Indexed
    private String schoolCategoryCode;
    private String facilityTypeCode;
    private String openedDate;
    private String closedDate;
    private boolean canIssueTranscripts;
    private boolean canIssueCertificates;
    private String createUser;
    private String updateUser;
    private String createDate;
    private String updateDate;
}
