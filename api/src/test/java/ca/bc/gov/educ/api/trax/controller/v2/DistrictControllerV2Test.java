package ca.bc.gov.educ.api.trax.controller.v2;

import ca.bc.gov.educ.api.trax.model.dto.institute.District;
import ca.bc.gov.educ.api.trax.service.institute.DistrictService;
import ca.bc.gov.educ.api.trax.util.ResponseHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DistrictControllerV2Test {

	@Mock
	private DistrictService districtService;
	
	@InjectMocks
	private DistrictController districtController;

	@Mock
	private ResponseHelper responseHelperMock;

	@Test
	void whenReloadSchoolCategoryCodesIntoCache_shouldReturnOK() {
		doNothing().when(districtService).initializeDistrictCache(true);
		districtController.reloadDistrictsIntoCache();
		Mockito.verify(districtService).initializeDistrictCache(true);
	}

	@Test
	void whenReloadSchoolCategoryCodesIntoCache_shouldThrowException() {
		doThrow(new RuntimeException()).when(districtService).initializeDistrictCache(true);
		districtController.reloadDistrictsIntoCache();
		assertThrows(RuntimeException.class, () -> districtService.initializeDistrictCache(true));
	}

	@Test
	void whenGetDistrictDetailsById_shouldReturnDistrict() {
		District district = new District();
		district.setDistrictId("1a-2b-3c");
		district.setDistrictNumber("01");
		district.setDistrictRegionCode("reg-code");
		ResponseEntity<District> resp = ResponseEntity.ok().body(district);
		when(districtService.getDistrictByIdFromRedisCache("1a-2b-3c")).thenReturn(district);
		when(responseHelperMock.GET(district)).thenReturn(resp);
		ResponseEntity<District> actual = districtController.getDistrictDetailsById("1a-2b-3c");
		Mockito.verify(districtService).getDistrictByIdFromRedisCache(district.getDistrictId());
		assertEquals(resp, actual);
	}

	@Test
	void whenGetDistrictDetailsById_shouldReturn_NOT_FOUND() {
		String districtId = "1a-2b-3c";
		ResponseEntity<District> resp = ResponseEntity.notFound().build();
		when(districtService.getDistrictByIdFromRedisCache(districtId)).thenReturn(null);
		ResponseEntity<District> actual = districtController.getDistrictDetailsById(districtId);
		Mockito.verify(districtService).getDistrictByIdFromRedisCache(districtId);
		assertEquals(resp, actual);
	}

}
