package ca.bc.gov.educ.api.trax.controller;

import ca.bc.gov.educ.api.trax.model.dto.District;
import ca.bc.gov.educ.api.trax.service.DistrictService;
import ca.bc.gov.educ.api.trax.util.ResponseHelper;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("rawtypes")
public class DistrictControllerTest {

    @Mock
    private DistrictService districtService;
    @Mock
    private ca.bc.gov.educ.api.trax.service.institute.DistrictService districtServiceV2;

    @Mock
    ResponseHelper responseHelper;

    @InjectMocks
    private DistrictController districtController;

    @InjectMocks
    private ca.bc.gov.educ.api.trax.controller.v2.DistrictController districtControllerV2;

    @Test
    public void testGetSchoolDetails() {
        final District district = new District();
        district.setDistrictNumber("123");
        district.setDistrictName("Test School");

        Mockito.when(districtService.getDistrictDetails("123")).thenReturn(district);
        districtController.getDistrictDetails("123");
        Mockito.verify(districtService).getDistrictDetails("123");

    }

    @Test
    public void testGetDistrictBySchoolCategoryCode() {
        final District district = new District();
        district.setDistrictNumber("123");
        district.setDistrictName("Test School");

        Mockito.when(districtService.getDistrictBySchoolCategory("123")).thenReturn(List.of(district));
        districtController.getDistrictBySchoolCategory("123");
        Mockito.verify(districtService).getDistrictBySchoolCategory("123");

    }

    @Test
    public void whenGetDistrictDetailsByDistNo_ReturnsDistrict() {
        String distNo = "123";
        ca.bc.gov.educ.api.trax.model.dto.institute.District district = new ca.bc.gov.educ.api.trax.model.dto.institute.District();
        district.setDistrictId("123456");
        district.setDistrictNumber("123");
        district.setDistrictRegionCode("BC");

        Mockito.when(districtServiceV2.getDistrictByDistNoFromRedisCache(distNo)).thenReturn(district);
        districtControllerV2.getDistrictDetailsByDistNo(distNo);
        Mockito.verify(districtServiceV2).getDistrictByDistNoFromRedisCache(distNo);
    }

    @Test
    public void whenGetDistrictsBySchoolCategoryCode_ReturnListOfDistricts() {
        String schoolCategoryCode = "123";
        final List<ca.bc.gov.educ.api.trax.model.dto.institute.District> districts = new ArrayList<>();
        ca.bc.gov.educ.api.trax.model.dto.institute.District district = new ca.bc.gov.educ.api.trax.model.dto.institute.District();
        district.setDistrictId("123456");
        district.setDistrictNumber("123");
        district.setDistrictRegionCode("BC");
        districts.add(district);
        district = new ca.bc.gov.educ.api.trax.model.dto.institute.District();
        district.setDistrictId("789012");
        district.setDistrictNumber("456");
        district.setDistrictRegionCode("BC");
        districts.add(district);

        Mockito.when(districtServiceV2.getDistrictsBySchoolCategoryCode(schoolCategoryCode)).thenReturn(districts);
        districtControllerV2.getDistrictsBySchoolCategoryCode(schoolCategoryCode);
        Mockito.verify(districtServiceV2).getDistrictsBySchoolCategoryCode(schoolCategoryCode);
    }
}
