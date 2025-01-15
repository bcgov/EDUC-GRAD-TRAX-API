package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.model.dto.SnapshotResponse;
import ca.bc.gov.educ.api.trax.model.dto.institute.School;
import ca.bc.gov.educ.api.trax.repository.SnapshotRepository;
import ca.bc.gov.educ.api.trax.service.institute.SchoolService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EdwService {
    final private SnapshotRepository snapshotRepository;
    final private SchoolService schoolService;

    public EdwService(SnapshotRepository snapshotRepository, SchoolService schoolService) {
        this.snapshotRepository = snapshotRepository;
        this.schoolService = schoolService;
    }

    public List<String> getUniqueSchoolList(Integer gradYear) {
        return snapshotRepository.getSchools(gradYear);
    }

    public List<SnapshotResponse> getStudents(Integer gradYear) {
        List<SnapshotResponse> results = snapshotRepository.getStudentsByGradYear(gradYear);
        populateSchoolId(results);
        return results;
    }

    public List<SnapshotResponse> getStudents(Integer gradYear, String schoolOfRecord) {
        List<SnapshotResponse> results = snapshotRepository.getStudentsByGradYearAndSchoolOfRecord(gradYear, schoolOfRecord);
        populateSchoolId(results);
        return results;
    }

    private void populateSchoolId(List<SnapshotResponse> list) {
        list.forEach(r -> {
            School sch = schoolService.getSchoolByMinCodeFromRedisCache(r.getSchoolOfRecord());
            if (sch != null) {
                r.setSchoolOfRecordId(sch.getSchoolId());
            }
        });
    }
}
