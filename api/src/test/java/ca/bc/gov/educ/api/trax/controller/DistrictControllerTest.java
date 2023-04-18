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

import java.util.List;

@RunWith(MockitoJUnitRunner.class)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("rawtypes")
public class DistrictControllerTest {

    @Mock
    private DistrictService districtService;

    @Mock
    ResponseHelper responseHelper;

    @InjectMocks
    private DistrictController districtController;

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

        Mockito.when(districtService.getDistrictBySchoolCategory("123", "accessToken")).thenReturn(List.of(district));
        districtController.getDistrictBySchoolCategory("123", "accessToken");
        Mockito.verify(districtService).getDistrictBySchoolCategory("123", "accessToken");

    }
}
