package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.model.dto.SnapshotResponse;
import ca.bc.gov.educ.api.trax.repository.SnapshotRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EdwService {
    final SnapshotRepository snapshotRepository;

    public EdwService(
            SnapshotRepository snapshotRepository
    ) {
        this.snapshotRepository = snapshotRepository;
    }

    public List<String> getUniqueSchoolList(Integer gradYear) {
        return snapshotRepository.getSchools(gradYear);
    }

    public List<SnapshotResponse> getStudents(Integer gradYear) {
        return snapshotRepository.getStudentsByGradYear(gradYear);
    }

    public List<SnapshotResponse> getStudents(Integer gradYear, String schoolOfRecord) {
        return snapshotRepository.getStudentsByGradYearAndSchoolOfRecord(gradYear, schoolOfRecord);
    }
}
