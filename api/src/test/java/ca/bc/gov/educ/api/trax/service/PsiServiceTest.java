package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.model.dto.GradCountry;
import ca.bc.gov.educ.api.trax.model.dto.GradProvince;
import ca.bc.gov.educ.api.trax.model.dto.Psi;
import ca.bc.gov.educ.api.trax.model.entity.PsiEntity;
import ca.bc.gov.educ.api.trax.repository.PsiCriteriaQueryRepository;
import ca.bc.gov.educ.api.trax.repository.PsiRepository;
import ca.bc.gov.educ.api.trax.util.criteria.CriteriaHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;


@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class PsiServiceTest {

    @Autowired
    private PsiService psiService;

    @MockBean
    private PsiRepository psiRepository;

    @MockBean
    private PsiCriteriaQueryRepository psiCriteriaQueryRepository;

    @MockBean
    private CodeService codeService;

    @Before
    public void setUp() {
        openMocks(this);
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testGetPSIList() {
        // psi data
    	List<PsiEntity> gradPSIList = new ArrayList<>();
		PsiEntity obj = new PsiEntity();
		obj.setPsiCode("AB");
		obj.setPsiName("Autobody");
		obj.setAddress1("ABC");
		obj.setAddress2("DEF");
		obj.setAddress3("FGF");
		obj.setAttentionName("XZA");
		obj.setCity("Abbotsford");
		obj.setCountryCode("CDD");
		obj.setCslCode("SW@");
		obj.setFax("23432234234");
		obj.setOpenFlag("Y");
		obj.setPhone1("123213");
		obj.setPostal("V3T1C4");
		gradPSIList.add(obj);
		obj = new PsiEntity();
		obj.setPsiCode("AB");
		obj.setPsiName("Autobody");
		obj.setAddress1("ABC");
		obj.setAddress2("DEF");
		obj.setAddress3("FGF");
		obj.setAttentionName("XZA");
		obj.setCity("Abbotsford");
		obj.setCountryCode("CDD");
		obj.setCslCode("SW@");
		obj.setFax("23432234234");
		obj.setOpenFlag("Y");
		obj.setPhone1("123213");
		obj.setPostal("V3T1C4");
		gradPSIList.add(obj);
		

        when(psiRepository.findAll()).thenReturn(gradPSIList);
        List<Psi> results = psiService.getPSIList();

        assertThat(results).isNotNull();
        assertThat(results.size()).isEqualTo(2);
    }

    @Test
    public void testGetPsiDetails() {
        // School
    	final PsiEntity obj = new PsiEntity();
		obj.setPsiCode("AB");
		obj.setPsiName("Autobody");
		obj.setAddress1("ABC");
		obj.setAddress2("DEF");
		obj.setAddress3("FGF");
		obj.setAttentionName("XZA");
		obj.setCity("Abbotsford");
		obj.setCountryCode("CDD");
		obj.setCslCode("SW@");
		obj.setFax("23432234234");
		obj.setOpenFlag("Y");
		obj.setPhone1("123213");
		obj.setPostal("V3T1C4");

        // Country
        final GradCountry country = new GradCountry();
        country.setCountryCode("CA");
        country.setCountryName("Canada");

        // Provice
        final GradProvince province = new GradProvince();
        province.setCountryCode("CA");
        province.setProvCode("BC");
        province.setProvName("British Columbia");

        when(psiRepository.findById("AB")).thenReturn(Optional.of(obj));

        when(codeService.getSpecificCountryCode(obj.getCountryCode())).thenReturn(country);
        when(codeService.getSpecificProvinceCode(obj.getProvinceCode())).thenReturn(province);

        var result = psiService.getPSIDetails("AB");

        assertThat(result).isNotNull();
        assertThat(result.getPsiCode()).isEqualTo("AB");
        assertThat(result.getPsiName()).isEqualTo("Autobody");
    }

    @Test
    public void testGetPSIsByParams() {
        // School
    	PsiEntity obj = new PsiEntity();
		obj.setPsiCode("AB");
		obj.setPsiName("Autobody");
		obj.setAddress1("ABC ");
		obj.setAddress2("DEF ");
		obj.setAddress3("FGF ");
		obj.setAttentionName("XZA");
		obj.setCity("Abbotsford");
		obj.setCountryCode("CDD");
		obj.setProvinceCode("BC");
		obj.setCslCode("SW@");
		obj.setFax("23432234234");
		obj.setOpenFlag("Y");
		obj.setPhone1("123213 ");
		obj.setPostal("V3T1C4 ");
		List<PsiEntity> list = new ArrayList<PsiEntity>();
		list.add(obj);
        // Country
        GradCountry country = new GradCountry();
        country.setCountryCode("CA");
        country.setCountryName("Canada");

        // Provice
        GradProvince province = new GradProvince();
        province.setCountryCode("CA");
        province.setProvCode("BC");
        province.setProvName("British Columbia");

        when(psiCriteriaQueryRepository.findByCriteria(any(CriteriaHelper.class), eq(PsiEntity.class))).thenReturn(list);

        when(codeService.getSpecificCountryCode(obj.getCountryCode())).thenReturn(country);
        when(codeService.getSpecificProvinceCode(obj.getProvinceCode())).thenReturn(province);

        var result = psiService.getPSIByParams("Autobody", "AB", null,null);
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getPsiCode()).isEqualTo("AB");
        assertThat(result.get(0).getPsiName()).isEqualTo("Autobody");
    }
    
    @Test
    public void testGetPSIsByParamsStar() {
        // School
    	PsiEntity obj = new PsiEntity();
		obj.setPsiCode("AB");
		obj.setPsiName("Autobody");
		obj.setAddress1("ABC ");
		obj.setAddress2("DEF ");
		obj.setAddress3("FGF ");
		obj.setAttentionName("XZA");
		obj.setCity("Abbotsford");
		obj.setCountryCode("CDD");
		obj.setProvinceCode("BC");
		obj.setCslCode("SW@");
		obj.setFax("23432234234");
		obj.setOpenFlag("Y");
		obj.setPhone1("123213 ");
		obj.setPostal("V3T1C4 ");
		List<PsiEntity> list = new ArrayList<PsiEntity>();
		list.add(obj);
        // Country
        GradCountry country = new GradCountry();
        country.setCountryCode("CA");
        country.setCountryName("Canada");

        // Provice
        GradProvince province = new GradProvince();
        province.setCountryCode("CA");
        province.setProvCode("BC");
        province.setProvName("British Columbia");

        when(psiCriteriaQueryRepository.findByCriteria(any(CriteriaHelper.class), eq(PsiEntity.class))).thenReturn(list);

        when(codeService.getSpecificCountryCode(obj.getCountryCode())).thenReturn(country);
        when(codeService.getSpecificProvinceCode(obj.getProvinceCode())).thenReturn(province);

        var result = psiService.getPSIByParams("Autobody", "AB*", null,null);
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getPsiCode()).isEqualTo("AB");
        assertThat(result.get(0).getPsiName()).isEqualTo("Autobody");
    }
}
