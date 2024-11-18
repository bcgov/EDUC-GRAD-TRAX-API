package ca.bc.gov.educ.api.trax.service.institute;

import ca.bc.gov.educ.api.trax.model.dto.School;
import ca.bc.gov.educ.api.trax.model.dto.institute.District;
import ca.bc.gov.educ.api.trax.model.dto.institute.SchoolAddress;
import ca.bc.gov.educ.api.trax.model.dto.institute.SchoolCategoryCode;
import ca.bc.gov.educ.api.trax.model.dto.institute.SchoolDetail;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class CommonService {
    private CodeService codeService;
    private DistrictService districtService;
    private SchoolService schoolService;

    @Autowired
    public CommonService(@Qualifier("InstituteCodeService") CodeService codeService,
                         @Qualifier("InstituteDistrictService") DistrictService districtService,
                         @Qualifier("InstituteSchoolService") SchoolService schoolService) {
        this.codeService = codeService;
        this.districtService = districtService;
        this.schoolService = schoolService;
    }

    // ================ Graduation Algorithm Data supports (School Clob) ====================
    // All of Schools Clob data for Algorithm Data from RedisCache
    public List<ca.bc.gov.educ.api.trax.model.dto.School> getSchoolsForClobDataFromRedisCache() {
        log.debug("Get All Schools Clob data from Redis Cache.");
        List<SchoolDetail> schoolDetails = schoolService.getSchoolDetailsFromRedisCache();
        return schoolDetails.stream().map(this::convertSchoolDetailIntoSchoolClob).toList();
    }

    // School Clob data for Algorithm Data by minCode from RedisCache
    public ca.bc.gov.educ.api.trax.model.dto.School getSchoolForClobDataByMinCodeFromRedisCache(String minCode) {
        log.debug("Get a School Clob data by MinCode from Redis Cache: {}", minCode);
        SchoolDetail schoolDetail = schoolService.getSchoolDetailByMincodeFromRedisCache(minCode);
        return schoolDetail != null? convertSchoolDetailIntoSchoolClob((schoolDetail)) : null;
    }

    // School Clob data for Algorithm Data by schoolId from RedisCache
    public ca.bc.gov.educ.api.trax.model.dto.School getSchoolForClobDataBySchoolIdFromRedisCache(UUID schoolId) {
        log.debug("Get a School Clob data by SchoolId from Redis Cache: {}", schoolId);
        SchoolDetail schoolDetail = schoolService.getSchoolDetailBySchoolId(schoolId);
        return schoolDetail != null? convertSchoolDetailIntoSchoolClob((schoolDetail)) : null;
    }

    public UUID getSchoolIdFromRedisCache(String minCode) {
        ca.bc.gov.educ.api.trax.model.dto.institute.School school = schoolService.getSchoolByMinCodeFromRedisCache(minCode);
        return school != null && StringUtils.isNotBlank(school.getSchoolId())? UUID.fromString(school.getSchoolId()) : null;
    }

    public String getSchoolIdStrFromRedisCache(String minCode) {
        ca.bc.gov.educ.api.trax.model.dto.institute.School school = schoolService.getSchoolByMinCodeFromRedisCache(minCode);
        return school != null && StringUtils.isNotBlank(school.getSchoolId())? school.getSchoolId() : null;
    }

    public List<School> getSchoolsByDistrictNumberFromRedisCache(String districtNumber) {
        District district = districtService.getDistrictByDistNoFromRedisCache(districtNumber);
        if (district != null) {
            List<SchoolDetail> schoolDetails = schoolService.getSchoolDetailsByDistrictFromRedisCache(district.getDistrictId());
            return schoolDetails.stream().map(this::convertSchoolDetailIntoSchoolClob).toList();
        }
        return new ArrayList<>();
    }

    private School convertSchoolDetailIntoSchoolClob(ca.bc.gov.educ.api.trax.model.dto.institute.SchoolDetail schoolDetail) {
        School school = new School();

        school.setMinCode(schoolDetail.getMincode());
        school.setSchoolId(schoolDetail.getSchoolId());
        school.setSchoolName(schoolDetail.getDisplayName());
        school.setTranscriptEligibility(schoolDetail.isCanIssueTranscripts()? "Y" : "N");
        school.setCertificateEligibility(schoolDetail.isCanIssueCertificates()? "Y" : "N");
        school.setOpenFlag(schoolDetail.getClosedDate() == null? "Y" : "N");

        // District
        ca.bc.gov.educ.api.trax.model.dto.institute.District district = districtService.getDistrictByIdFromRedisCache(schoolDetail.getDistrictId());
        if (district != null) {
            school.setDistrictName(district.getDisplayName());
        }

        // School Category
        school.setSchoolCategoryCode(schoolDetail.getSchoolCategoryCode());
        populateSchoolCategoryLegacyCode(school, schoolDetail.getSchoolCategoryCode());

        // Address
        populateSchoolAddress(school, schoolDetail.getAddresses());

        return school;
    }

    private void populateSchoolAddress(School school, List<SchoolAddress> addresses) {
        // Address
        if (addresses != null && !addresses.isEmpty()) {
            for (SchoolAddress address : addresses) {
                if ("MAILING".equalsIgnoreCase(address.getAddressTypeCode())) {
                    school.setAddress1(address.getAddressLine1());
                    school.setAddress2(address.getAddressLine2());

                    school.setCity(address.getCity());
                    school.setPostal(address.getPostal());
                    school.setProvCode(address.getProvinceCode());
                    school.setCountryCode(address.getCountryCode());
                }
            }
        }
    }

    private void populateSchoolCategoryLegacyCode(School school, String schoolCategoryCode) {
        if (StringUtils.isNotBlank(schoolCategoryCode)) {
            SchoolCategoryCode schoolCategoryCodeObj = codeService.getSchoolCategoryCodeFromRedisCache(schoolCategoryCode);
            if (schoolCategoryCodeObj != null) {
                school.setSchoolCategoryLegacyCode(schoolCategoryCodeObj.getLegacyCode());
            }
        }
    }

}
