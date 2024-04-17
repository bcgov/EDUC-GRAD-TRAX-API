package ca.bc.gov.educ.api.trax.util;

import ca.bc.gov.educ.api.trax.model.dto.CommonSchool;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

@Component
public class SchoolCache {

    private List<CommonSchool> schools;

    public CommonSchool getSchoolByMincode(String mincode){
        return null;
    }

    private CommonSchool searchSchools(Predicate<CommonSchool> search) {
        if(schools == null || cacheHasExpired()){
            populateCache();
        }
        return schools.stream().filter(search).findFirst().orElse(null);
    }

    private boolean cacheHasExpired() {
        return false;
    }

    private void populateCache() {
        // get schools
    }

}
