package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.model.dto.GradCountry;
import ca.bc.gov.educ.api.trax.model.dto.GradProvince;
import ca.bc.gov.educ.api.trax.model.entity.GradCountryEntity;
import ca.bc.gov.educ.api.trax.model.entity.GradProvinceEntity;
import ca.bc.gov.educ.api.trax.repository.GradCountryRepository;
import ca.bc.gov.educ.api.trax.repository.GradProvinceRepository;
import ca.bc.gov.educ.api.trax.util.GradValidation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@SuppressWarnings({"rawtypes"})
public class CodeServiceTest {

	@Autowired
	private CodeService codeService;

	@MockBean
	private GradCountryRepository gradCountryRepository;

	@MockBean
	private GradProvinceRepository gradProvinceRepository;

	@Autowired
	GradValidation validation;
	
	@Test
	public void testGetAllProvinceList() {
		List<GradProvinceEntity> gradProvinceList = new ArrayList<>();
		GradProvinceEntity obj = new GradProvinceEntity();
		obj.setProvCode("BC");
		obj.setProvName("British Columbia");
		gradProvinceList.add(obj);
		obj = new GradProvinceEntity();
		obj.setProvCode("AB");
		obj.setProvName("Alberta");
		gradProvinceList.add(obj);
		Mockito.when(gradProvinceRepository.findAll()).thenReturn(gradProvinceList);
		codeService.getAllProvinceCodeList();
		
	}
	
	@Test
	public void testGetSpecificProvinceCode() {
		String provCode = "BC";
		GradProvince obj = new GradProvince();
		obj.setProvCode("BC");
		obj.setProvName("British Columbia");
		obj.toString();
		GradProvinceEntity objEntity = new GradProvinceEntity();
		objEntity.setProvCode("BC");
		objEntity.setProvName("British Columbia");
		Optional<GradProvinceEntity> ent = Optional.of(objEntity);
		Mockito.when(gradProvinceRepository.findById(provCode)).thenReturn(ent);
		codeService.getSpecificProvinceCode(provCode);
	}
	
	@Test
	public void testGetSpecificProvinceCodeReturnsNull() {
		String provCode = "BC";
		Mockito.when(gradProvinceRepository.findById(provCode)).thenReturn(Optional.empty());
		codeService.getSpecificProvinceCode(provCode);
	}
	
	@Test
	public void testGetAllCountryList() {
		List<GradCountryEntity> gradCountryList = new ArrayList<>();
		GradCountryEntity obj = new GradCountryEntity();
		obj.setCountryCode("CA");
		obj.setCountryName("Canada");
		gradCountryList.add(obj);
		obj = new GradCountryEntity();
		obj.setCountryCode("USA");
		obj.setCountryName("America");
		gradCountryList.add(obj);
		Mockito.when(gradCountryRepository.findAll()).thenReturn(gradCountryList);
		codeService.getAllCountryCodeList();
	}
	
	@Test
	public void testGetSpecificCountryCode() {
		String countryCode = "AB";
		GradCountry obj = new GradCountry();
		obj.setCountryCode("CA");
		obj.setCountryName("Canada");
		obj.toString();
		GradCountryEntity objEntity = new GradCountryEntity();
		objEntity.setCountryCode("CA");
		objEntity.setCountryName("Canada");
		Optional<GradCountryEntity> ent = Optional.of(objEntity);
		Mockito.when(gradCountryRepository.findById(countryCode)).thenReturn(ent);
		codeService.getSpecificCountryCode(countryCode);
	}
	
	@Test
	public void testGetSpecificCountryCodeReturnsNull() {
		String countryCode = "CA";
		Mockito.when(gradCountryRepository.findById(countryCode)).thenReturn(Optional.empty());
		codeService.getSpecificCountryCode(countryCode);
	}
}
