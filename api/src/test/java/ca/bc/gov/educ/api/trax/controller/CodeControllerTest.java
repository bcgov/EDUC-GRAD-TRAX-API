package ca.bc.gov.educ.api.trax.controller;

import ca.bc.gov.educ.api.trax.model.dto.GradCountry;
import ca.bc.gov.educ.api.trax.model.dto.GradProvince;
import ca.bc.gov.educ.api.trax.service.CodeService;
import ca.bc.gov.educ.api.trax.util.GradValidation;
import ca.bc.gov.educ.api.trax.util.MessageHelper;
import ca.bc.gov.educ.api.trax.util.ResponseHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(MockitoExtension.class)
public class CodeControllerTest {

	@Autowired
	private MockMvc mockMvc;
	@Mock
	private CodeService codeService;
	
	@Mock
	ResponseHelper response;
	
	@InjectMocks
	private CodeController codeController;
	
	@Mock
	GradValidation validation;
	
	@Mock
	MessageHelper messagesHelper;

	@Test
	public void testGetAllCountryList() {
		List<GradCountry> gradCountryList = new ArrayList<>();
		GradCountry obj = new GradCountry();
		obj.setCountryCode("CA");
		obj.setCountryName("Canada");
		gradCountryList.add(obj);
		obj = new GradCountry();
		obj.setCountryCode("USA");
		obj.setCountryName("America");
		gradCountryList.add(obj);
		Mockito.when(codeService.getAllCountryCodeList()).thenReturn(gradCountryList);
		codeController.getAllCountryCodeList();
		Mockito.verify(codeService).getAllCountryCodeList();
	}
	
	@Test
	public void testGetSpecificCountryCode() {
		String countryCode = "CA";
		GradCountry obj = new GradCountry();
		obj.setCountryCode("CA");
		obj.setCountryName("Canada");
		Mockito.when(codeService.getSpecificCountryCode(countryCode)).thenReturn(obj);
		codeController.getSpecificCountryCode(countryCode);
		Mockito.verify(codeService).getSpecificCountryCode(countryCode);
	}
	
	@Test
	public void testGetSpecificCountryCode_noContent() {
		String countryCode = "AB";	
		Mockito.when(codeService.getSpecificCountryCode(countryCode)).thenReturn(null);
		codeController.getSpecificCountryCode(countryCode);
		Mockito.verify(codeService).getSpecificCountryCode(countryCode);
	}
	
	@Test
	public void testGetAllProvinceList() {
		List<GradProvince> gradProvinceList = new ArrayList<>();
		GradProvince obj = new GradProvince();
		obj.setProvCode("CA");
		obj.setProvName("Canada");
		gradProvinceList.add(obj);
		obj = new GradProvince();
		obj.setProvCode("USA");
		obj.setProvName("America");
		gradProvinceList.add(obj);
		Mockito.when(codeService.getAllProvinceCodeList()).thenReturn(gradProvinceList);
		codeController.getAllProvinceCodeList();
		Mockito.verify(codeService).getAllProvinceCodeList();
	}
	
	@Test
	public void testGetSpecificProvinceCode() {
		String countryCode = "CA";
		GradProvince obj = new GradProvince();
		obj.setProvCode("CA");
		obj.setProvName("Canada");
		Mockito.when(codeService.getSpecificProvinceCode(countryCode)).thenReturn(obj);
		codeController.getSpecificProvinceCode(countryCode);
		Mockito.verify(codeService).getSpecificProvinceCode(countryCode);
	}
	
	@Test
	public void testGetSpecificProvinceCode_noContent() {
		String countryCode = "AB";	
		Mockito.when(codeService.getSpecificProvinceCode(countryCode)).thenReturn(null);
		codeController.getSpecificProvinceCode(countryCode);
		Mockito.verify(codeService).getSpecificProvinceCode(countryCode);
	}

	@Test
	void testReloadSchoolCategoryCodesIntoCache_shouldReturnOK() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.put("/api/v2/trax/school/cache/school-category-codes")
						.with(jwt().jwt(jwt -> jwt.claim("scope", "UPDATE_GRAD_TRAX_CACHE")))
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	void testReloadSchoolFundingGroupCodesIntoCache_shouldReturnOK() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.put("/api/v2/trax/school/cache/school-funding-group-codes")
						.with(jwt().jwt(jwt -> jwt.claim("scope", "UPDATE_GRAD_TRAX_CACHE")))
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

}
