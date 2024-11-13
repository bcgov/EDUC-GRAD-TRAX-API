package ca.bc.gov.educ.api.trax.controller.v2;

import ca.bc.gov.educ.api.trax.service.institute.DistrictService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class DistrictControllerV2Test {

	@Mock
	private DistrictService districtService;
	
	@InjectMocks
	private DistrictController districtController;

	@Test
	void testReloadSchoolCategoryCodesIntoCache_shouldReturnOK() {
		doNothing().when(districtService).initializeDistrictCache(true);
		districtController.reloadDistrictsIntoCache();
		Mockito.verify(districtService).initializeDistrictCache(true);
	}

	@Test
	void testReloadSchoolCategoryCodesIntoCache_shouldThrowException() {
		doThrow(new RuntimeException()).when(districtService).initializeDistrictCache(true);
		districtController.reloadDistrictsIntoCache();
		assertThrows(RuntimeException.class, () -> districtService.initializeDistrictCache(true));
	}

}
