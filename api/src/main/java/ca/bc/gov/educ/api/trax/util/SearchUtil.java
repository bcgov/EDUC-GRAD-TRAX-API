package ca.bc.gov.educ.api.trax.util;

import ca.bc.gov.educ.api.trax.filter.FilterOperation;
import ca.bc.gov.educ.api.trax.model.dto.Search;
import ca.bc.gov.educ.api.trax.model.dto.SearchCriteria;
import ca.bc.gov.educ.api.trax.model.dto.ValueType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

/**
 * Util class for searches
 */
public class SearchUtil {

    public static final ObjectMapper mapper = new ObjectMapper();

    private SearchUtil() {
    }

    /**
     * convert list of search strings to HTTP params
     *
     * @param searchStringMap the HashMap of search strings
     * @return list of http params
     * @throws JsonProcessingException the json process exception
     *
     */
    public static Map<String, String> searchStringsToHTTPParams(Map<String, String> searchStringMap) throws JsonProcessingException {

        List<SearchCriteria> criteriaList = new ArrayList<>();
        searchStringMap.forEach((key, value) ->{
            SearchCriteria criteria = SearchCriteria.builder().key(key).operation(FilterOperation.EQUAL).value(value).valueType(ValueType.STRING).build();
            criteriaList.add(criteria);
        });
        final List<Search> searches = new LinkedList<>();
        searches.add(Search.builder().searchCriteriaList(criteriaList).build());
        final String criteriaJSON;

        criteriaJSON = mapper.writeValueAsString(searches);

        HashMap<String, String> params = new HashMap<>();
        params.put("searchCriteriaList", criteriaJSON);
        return params;

    }

}
