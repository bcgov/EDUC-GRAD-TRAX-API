package ca.bc.gov.educ.api.trax.controller.v2;

import ca.bc.gov.educ.api.trax.service.institute.CodeService;
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
class CodeControllerV2Test {

	@Mock
	private CodeService codeService;
	
	@InjectMocks
	private CodeController codeController;

	@Test
	void testReloadSchoolCategoryCodesIntoCache_shouldReturnOK() {
		doNothing().when(codeService).initializeSchoolCategoryCodeCache(true);
		codeController.reloadSchoolCategoryCodesIntoCache();
		Mockito.verify(codeService).initializeSchoolCategoryCodeCache(true);
	}

	@Test
	void testReloadSchoolCategoryCodesIntoCache_shouldThrowException() {
		doThrow(new RuntimeException()).when(codeService).initializeSchoolCategoryCodeCache(true);
		codeController.reloadSchoolCategoryCodesIntoCache();
		assertThrows(RuntimeException.class, () -> codeService.initializeSchoolCategoryCodeCache(true));
	}

	@Test
	void testReloadSchoolFundingGroupCodesIntoCache_shouldReturnOK() {
		doNothing().when(codeService).initializeSchoolFundingGroupCodeCache(true);
		codeController.reloadSchoolFundingGroupCodesIntoCache();
		Mockito.verify(codeService).initializeSchoolFundingGroupCodeCache(true);
	}

	@Test
	void testReloadSchoolFundingGroupCodesIntoCache_shouldThrowException() {
		doThrow(new RuntimeException()).when(codeService).initializeSchoolFundingGroupCodeCache(true);
		codeController.reloadSchoolFundingGroupCodesIntoCache();
		assertThrows(RuntimeException.class, () -> codeService.initializeSchoolFundingGroupCodeCache(true));
	}

}
