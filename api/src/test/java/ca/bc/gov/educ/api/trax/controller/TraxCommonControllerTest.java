package ca.bc.gov.educ.api.trax.controller;

import ca.bc.gov.educ.api.trax.model.dto.*;
import ca.bc.gov.educ.api.trax.service.TraxCommonService;
import ca.bc.gov.educ.api.trax.util.GradValidation;
import ca.bc.gov.educ.api.trax.util.MessageHelper;
import ca.bc.gov.educ.api.trax.util.ResponseHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class TraxCommonControllerTest {

	@Mock
	private TraxCommonService traxCommonService;
	
	@Mock
	ResponseHelper response;
	
	@InjectMocks
	private TraxCommonController traxCommonController;
	
	@Mock
	GradValidation validation;
	
	@Mock
	MessageHelper messagesHelper;

	@Test
	void testGetStudentDemographicsDataFromTrax() {
		final String pen = "123456789";
		List<Student> studentList = new ArrayList<>();
		Student obj = Student.builder()
						.pen(pen)
						.dob("198801")
						.genderCode("M")
						.legalFirstName("Test")
						.legalLastName("QA")
						.gradeCode("12")
						.mincode("12345678")
						.statusCode("A")
					.build();
		studentList.add(obj);
		Mockito.when(traxCommonService.getStudentDemographicsDataFromTrax(pen)).thenReturn(studentList);
		traxCommonController.getStudentDemographicsDataFromTrax(pen);
		Mockito.verify(traxCommonService).getStudentDemographicsDataFromTrax(pen);
	}
	
	@Test
	void testGetStudentMasterDataFromTrax() {
		final String pen = "123456789";
		List<ConvGradStudent> studentList = new ArrayList<>();
		ConvGradStudent obj = ConvGradStudent.builder()
								.pen(pen)
								.graduationRequirementYear("2020")
								.program("2018-EN")
								.studentGrade("12")
								.schoolOfRecordId(UUID.randomUUID())
							.build();
		studentList.add(obj);
		Mockito.when(traxCommonService.getStudentMasterDataFromTrax(pen)).thenReturn(studentList);
		traxCommonController.getStudentMasterDataFromTrax(pen);
		Mockito.verify(traxCommonService).getStudentMasterDataFromTrax(pen);
	}

	@Test
	void testGetTraxStudentNoListByPaging() {
		final String pen = "123456789";
		List<TraxStudentNo> studentList = new ArrayList<>();
		TraxStudentNo obj = new TraxStudentNo();
		obj.setStudNo(pen);

		studentList.add(obj);

		final int pageNumber = 0;
		final int pageSize = 10;
		Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("studNo").ascending());

		Mockito.when(traxCommonService.loadTraxStudentNoByPage(pageable)).thenReturn(studentList);
		traxCommonController.getTraxStudentNoListByPaging(pageNumber, pageSize);
		Mockito.verify(traxCommonService).loadTraxStudentNoByPage(pageable);
	}

	@Test
	void testGetToralNumberOfTraxStudentNoList() {
		Mockito.when(traxCommonService.getTotalNumberOfTraxStudentNo()).thenReturn(1);
		traxCommonController.getTotalNumberOfTraxStudentNoList();
		Mockito.verify(traxCommonService).getTotalNumberOfTraxStudentNo();
	}

	@Test
	void testGetCourseRestrictions() {
		List<CourseRestriction> courseRestrictionList = new ArrayList<>();
		CourseRestriction obj = new CourseRestriction();
		obj.setMainCourse("main");
		obj.setMainCourseLevel("12");
		obj.setRestrictedCourse("test");
		obj.setRestrictedCourseLevel("12");
		obj.setCourseRestrictionId(UUID.randomUUID());

		courseRestrictionList.add(obj);

		Mockito.when(traxCommonService.loadGradCourseRestrictionsDataFromTrax()).thenReturn(courseRestrictionList);
		traxCommonController.getCourseRestrictions();
		Mockito.verify(traxCommonService).loadGradCourseRestrictionsDataFromTrax();
	}

	@Test
	void testGetCourseRequirements() {
		List<GradCourse> courseRequirementList = new ArrayList<>();
		GradCourse obj = new GradCourse();
		obj.setCourseCode("main");
		obj.setCourseLevel("12");
		obj.setGradReqtYear("2020");
		obj.setEnglish12("Y");

		courseRequirementList.add(obj);

		Mockito.when(traxCommonService.loadGradCourseRequirementsDataFromTrax()).thenReturn(courseRequirementList);
		traxCommonController.getCourseRequirements();
		Mockito.verify(traxCommonService).loadGradCourseRequirementsDataFromTrax();
	}

	@Test
	void testSaveTraxStudentNo() {
		final String pen = "123456789";
		TraxStudentNo obj = new TraxStudentNo();
		obj.setStudNo(pen);

		Mockito.when(traxCommonService.saveTraxStudentNo(obj)).thenReturn(obj);
		traxCommonController.saveTraxStudentNo(obj);
		Mockito.verify(traxCommonService).saveTraxStudentNo(obj);
	}

}
