package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.constant.EventOutcome;
import ca.bc.gov.educ.api.trax.exception.TraxAPIRuntimeException;
import ca.bc.gov.educ.api.trax.messaging.jetstream.Publisher;
import ca.bc.gov.educ.api.trax.model.dto.*;
import ca.bc.gov.educ.api.trax.model.entity.TraxUpdatedPubEvent;
import ca.bc.gov.educ.api.trax.model.entity.TraxUpdateInGradEntity;
import ca.bc.gov.educ.api.trax.repository.TraxUpdatedPubEventRepository;
import ca.bc.gov.educ.api.trax.repository.TraxUpdateInGradRepository;
import ca.bc.gov.educ.api.trax.service.institute.CommonService;
import ca.bc.gov.educ.api.trax.util.JsonUtil;
import ca.bc.gov.educ.api.trax.util.RestUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

import static ca.bc.gov.educ.api.trax.constant.EventStatus.DB_COMMITTED;
import static ca.bc.gov.educ.api.trax.util.EducGradTraxApiConstants.DEFAULT_CREATED_BY;
import static ca.bc.gov.educ.api.trax.util.EducGradTraxApiConstants.DEFAULT_UPDATED_BY;

@Service
public class TraxUpdateService {

    private static Logger logger = LoggerFactory.getLogger(TraxUpdateService.class);

    @Autowired
    private TraxCommonService traxCommonService;

    @Autowired
    private CommonService commonService;

    @Autowired
    private TraxUpdateInGradRepository traxUpdateInGradRepository;

    @Autowired
    private TraxUpdatedPubEventRepository traxUpdatedPubEventRepository;

    @Autowired
    private RestUtils restUtils;

    @Autowired
    Publisher publisher;

    @Transactional(readOnly = true)
    public List<TraxUpdateInGradEntity> getOutstandingList(int numberOfRecordsToPull) {
        return traxUpdateInGradRepository.findOutstandingUpdates(new Date(System.currentTimeMillis()), numberOfRecordsToPull);
    }
    
    private void updateStatus(TraxUpdateInGradEntity traxUpdateInGradEntity) {
        // update status to PUBLISHED
        traxUpdateInGradEntity.setStatus("PUBLISHED");
        traxUpdateInGradRepository.save(traxUpdateInGradEntity);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<TraxUpdatedPubEvent> writeTraxUpdatedEvent(List<TraxUpdateInGradEntity> traxUpdateInGradEntity) {
        // Save TraxUpdatedPubEvent
        List<TraxUpdatedPubEvent> traxUpdatedPubEvents = new ArrayList<>();
        try {
            for(TraxUpdateInGradEntity ts : traxUpdateInGradEntity) {
                traxUpdatedPubEvents.add(persistTraxUpdatedEvent(ts));
                updateStatus(ts);
            }
            
            return traxUpdatedPubEvents;
        } catch (JsonProcessingException ex) {
            logger.error("JSON Processing exception : {}", ex.getMessage());
            throw new TraxAPIRuntimeException("JSON Processing exception : " + ex.getMessage());
        }
    }

    private TraxUpdatedPubEvent persistTraxUpdatedEvent(TraxUpdateInGradEntity traxStudentEntity) throws JsonProcessingException {
        String jsonString = null;
        String updateType = traxStudentEntity.getUpdateType() != null? traxStudentEntity.getUpdateType().trim() : null;
        if (StringUtils.equalsIgnoreCase(updateType, "NEWSTUDENT")) {
            ConvGradStudent newStudent = populateNewStudent(traxStudentEntity.getPen().trim());
            if (newStudent != null) {
                jsonString = JsonUtil.getJsonStringFromObject(newStudent);
            }
        } else {
            TraxStudentUpdateDTO studentUpdate = populateEventPayload(updateType, traxStudentEntity.getPen().trim());
            if (studentUpdate != null) {
                jsonString = JsonUtil.getJsonStringFromObject(studentUpdate);
            }
        }

        final TraxUpdatedPubEvent traxUpdatedPubEvent = TraxUpdatedPubEvent.builder()
                .eventType(updateType)
                .eventId(UUID.randomUUID())
                .eventOutcome(EventOutcome.TRAX_STUDENT_MASTER_UPDATED.toString())
                .eventPayload(jsonString)
                .eventStatus(DB_COMMITTED.toString())
                .createUser(DEFAULT_CREATED_BY)
                .updateUser(DEFAULT_UPDATED_BY)
                .createDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();
        return traxUpdatedPubEventRepository.save(traxUpdatedPubEvent);
    }

    private ConvGradStudent populateNewStudent(String pen) {
        ConvGradStudent payload = null;
        List<ConvGradStudent> results = traxCommonService.getStudentMasterDataFromTrax(pen);
        if (results != null && !results.isEmpty()) {
            payload = results.get(0);
        }
        return payload;
    }

    private TraxDemographicsUpdateDTO populateDemographicsInfo(String pen) {
        TraxDemographicsUpdateDTO dto = null;
        Student student = null;
        List<Student> students = traxCommonService.getStudentDemographicsDataFromTrax(pen);
        if (students != null && !students.isEmpty()) {
            student = students.get(0);
        }
        if (student != null) {
            dto = new TraxDemographicsUpdateDTO();
            dto.setPen(pen);
            dto.setLastName(student.getLegalLastName());
            dto.setFirstName(student.getLegalFirstName());
            dto.setMiddleNames(student.getLegalMiddleNames());
            dto.setBirthday(student.getDob());
        }
        return dto;
    }

    private TraxStudentUpdateDTO populateEventPayload(String updateType, String pen) {
        TraxStudentUpdateDTO result = null;
        ConvGradStudent traxStudent;
        List<ConvGradStudent> results = traxCommonService.getStudentMasterDataFromTrax(pen);
        if (results != null && !results.isEmpty()) {
            traxStudent = results.get(0);
            switch(updateType) {
                case "UPD_DEMOG":
                    result = populateDemographicsInfo(pen);
                    break;
                case "UPD_GRAD":
                    TraxGraduationUpdateDTO gradUpdate = new TraxGraduationUpdateDTO();
                    gradUpdate.setPen(pen);
                    gradUpdate.setGraduationRequirementYear(traxStudent.getGraduationRequirementYear());
                    gradUpdate.setStudentGrade(traxStudent.getStudentGrade());
                    gradUpdate.setSchoolOfRecordId(traxStudent.getSchoolOfRecordId());
                    gradUpdate.setSlpDate(traxStudent.getSlpDate());
                    gradUpdate.setCitizenship(traxStudent.getStudentCitizenship());
                    gradUpdate.setStudentStatus(traxStudent.getStudentStatus());
                    gradUpdate.setArchiveFlag(traxStudent.getArchiveFlag());
                    result = gradUpdate;
                    break;
                case "XPROGRAM":
                    TraxXProgramDTO xprogram = new TraxXProgramDTO();
                    xprogram.setPen(pen);
                    xprogram.setProgramList(traxStudent.getProgramCodes());
                    result = xprogram;
                    break;
                case "ASSESSMENT":
                case "COURSE":
                    TraxStudentUpdateDTO studentUpdate = new TraxStudentUpdateDTO();
                    studentUpdate.setPen(pen);
                    result = studentUpdate;
                    break;
                case "FI10ADD":
                    TraxFrenchImmersionUpdateDTO fi10Add = new TraxFrenchImmersionUpdateDTO();
                    fi10Add.setPen(pen);
                    fi10Add.setGraduationRequirementYear(traxStudent.getGraduationRequirementYear());
                    fi10Add.setCourseCode("FRAL");
                    fi10Add.setCourseLevel("10");
                    result = fi10Add;
                    break;
                default:
                    break;
            }
        }
        return result;
    }

    public void publishToJetStream(final TraxUpdatedPubEvent traxUpdatedPubEvent) {
        if (traxUpdatedPubEvent != null) {
            publisher.dispatchChoreographyEvent(traxUpdatedPubEvent);
        }
    }

}
