package ca.bc.gov.educ.api.trax.controller;

import ca.bc.gov.educ.api.trax.EducGradTraxApiApplication;
import ca.bc.gov.educ.api.trax.filter.FilterOperation;
import ca.bc.gov.educ.api.trax.model.dto.Search;
import ca.bc.gov.educ.api.trax.model.dto.SearchCriteria;
import ca.bc.gov.educ.api.trax.model.dto.ValueType;
import ca.bc.gov.educ.api.trax.model.entity.EventEntity;
import ca.bc.gov.educ.api.trax.repository.EventHistoryRepository;
import ca.bc.gov.educ.api.trax.repository.EventRepository;
import ca.bc.gov.educ.api.trax.util.BaseEventHistoryTest;
import ca.bc.gov.educ.api.trax.util.EducGradTraxApiConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.*;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;

@SpringBootTest(classes = { EducGradTraxApiApplication.class })
@ActiveProfiles("test")
@AutoConfigureMockMvc
class EventHistoryControllerTest extends BaseEventHistoryTest {

    private static final String READ_SCOPE = "SCOPE_READ_EVENT_HISTORY";
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventHistoryRepository eventHistoryRepository;

    @Autowired
    private EventRepository eventRepository;
    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        EventEntity event = this.eventRepository.save(this.createEventData());
        this.eventHistoryRepository.save(this.createEventHistoryData(event));
    }

    @AfterEach
    void tearDown() throws Exception {
        this.eventRepository.deleteAll();
        closeable.close();
    }

    @Test
    void testReadEventHistoryPaginated_givenValueNull_ShouldReturnStatusOk() throws Exception {
        final GrantedAuthority grantedAuthority = () -> READ_SCOPE;
        final var mockAuthority = oidcLogin().authorities(grantedAuthority);
        final SearchCriteria criteria = SearchCriteria.builder().key("website").operation(FilterOperation.EQUAL).value(null).valueType(ValueType.STRING).build();
        final List<SearchCriteria> criteriaList = new ArrayList<>();
        criteriaList.add(criteria);
        final List<Search> searches = new LinkedList<>();
        searches.add(Search.builder().searchCriteriaList(criteriaList).build());
        final ObjectMapper objectMapper = new ObjectMapper();
        final String criteriaJSON = objectMapper.writeValueAsString(searches);
        this.mockMvc.perform(get(EducGradTraxApiConstants.EVENT_HISTORY_MAPPING_V1 + "/paginated").with(mockAuthority).param("searchCriteriaList", criteriaJSON)
                .contentType(APPLICATION_JSON)).andDo(print()).andExpect(status().isOk());
    }

    @Test
    void testReadEventHistoryPaginated_givenUserNameFilter_ShouldReturnStatusOk() throws Exception {
        final GrantedAuthority grantedAuthority = () -> READ_SCOPE;
        final var mockAuthority = oidcLogin().authorities(grantedAuthority);
        final ObjectMapper objectMapper = new ObjectMapper();
        final SearchCriteria criteria = SearchCriteria.builder().key("createUser").operation(FilterOperation.EQUAL).value(USER).valueType(ValueType.STRING).build();
        final List<SearchCriteria> criteriaList = new ArrayList<>();
        criteriaList.add(criteria);
        final List<Search> searches = new LinkedList<>();
        searches.add(Search.builder().searchCriteriaList(criteriaList).build());
        final String criteriaJSON = objectMapper.writeValueAsString(searches);
        final MvcResult result = this.mockMvc
                .perform(get(EducGradTraxApiConstants.EVENT_HISTORY_MAPPING_V1 + "/paginated").with(mockAuthority).param("searchCriteriaList", criteriaJSON)
                        .contentType(APPLICATION_JSON))
                .andReturn();
        this.mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(1)));
    }


}