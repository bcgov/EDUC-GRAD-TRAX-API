package ca.bc.gov.educ.api.trax.controller;

import ca.bc.gov.educ.api.trax.EducGradTraxApiApplication;
import ca.bc.gov.educ.api.trax.filter.FilterOperation;
import ca.bc.gov.educ.api.trax.mapper.EventHistoryMapper;
import ca.bc.gov.educ.api.trax.model.dto.EventHistory;
import ca.bc.gov.educ.api.trax.model.dto.Search;
import ca.bc.gov.educ.api.trax.model.dto.SearchCriteria;
import ca.bc.gov.educ.api.trax.model.dto.ValueType;
import ca.bc.gov.educ.api.trax.model.entity.EventEntity;
import ca.bc.gov.educ.api.trax.repository.EventHistoryRepository;
import ca.bc.gov.educ.api.trax.repository.EventRepository;
import ca.bc.gov.educ.api.trax.support.TestUtils;
import ca.bc.gov.educ.api.trax.util.BaseEventHistoryTest;
import ca.bc.gov.educ.api.trax.util.EducGradTraxApiConstants;
import ca.bc.gov.educ.api.trax.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDateTime;
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
    private static final String WRITE_SCOPE = "SCOPE_WRITE_EVENT_HISTORY";
    private AutoCloseable closeable;

    @Autowired
    private EventHistoryMapper mapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventHistoryRepository eventHistoryRepository;

    @Autowired
    private EventRepository eventRepository;


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
        final SearchCriteria criteria = SearchCriteria.builder().key("website").operation(FilterOperation.EQUAL).value(null).valueType(ValueType.STRING).build();
        final List<SearchCriteria> criteriaList = new ArrayList<>();
        criteriaList.add(criteria);
        final List<Search> searches = new LinkedList<>();
        searches.add(Search.builder().searchCriteriaList(criteriaList).build());
        final ObjectMapper objectMapper = new ObjectMapper();
        final String criteriaJSON = objectMapper.writeValueAsString(searches);
        this.mockMvc.perform(get(EducGradTraxApiConstants.EVENT_HISTORY_MAPPING_V1 + "/paginated").with(getMockAuthority(READ_SCOPE)).param("searchCriteriaList", criteriaJSON)
                .contentType(APPLICATION_JSON)).andDo(print()).andExpect(status().isOk());
    }

    @Test
    void testReadEventHistoryPaginated_givenUserNameFilter_ShouldReturnStatusOk() throws Exception {
        final ObjectMapper objectMapper = new ObjectMapper();
        final SearchCriteria criteria = SearchCriteria.builder().key("createUser").operation(FilterOperation.EQUAL).value(USER).valueType(ValueType.STRING).build();
        final List<SearchCriteria> criteriaList = new ArrayList<>();
        criteriaList.add(criteria);
        final List<Search> searches = new LinkedList<>();
        searches.add(Search.builder().searchCriteriaList(criteriaList).build());
        final String criteriaJSON = objectMapper.writeValueAsString(searches);
        final MvcResult result = this.mockMvc
                .perform(get(EducGradTraxApiConstants.EVENT_HISTORY_MAPPING_V1 + "/paginated").with(getMockAuthority(READ_SCOPE)).param("searchCriteriaList", criteriaJSON)
                        .contentType(APPLICATION_JSON))
                .andReturn();
        this.mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(1)));
    }

    // test update goes ok
    @Test
    void testUpdateEventHistory_givenValidObject_shouldReturnStatusOk() throws Exception {
        final String acknowledgeFlag = "Y";
        final String updateUser = "Wattie";
        var eventHistory = createEventHistoryData();
        eventHistory.setAcknowledgeFlag(acknowledgeFlag);
        eventHistory.setUpdateUser(updateUser);
        this.mockMvc.perform(MockMvcRequestBuilders
                .put(EducGradTraxApiConstants.EVENT_HISTORY_MAPPING_V1)
                        .with(getMockAuthority(WRITE_SCOPE))
                .content(JsonUtil.getJsonStringFromObject(eventHistory))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.acknowledgeFlag").value(acknowledgeFlag))
                .andExpect(MockMvcResultMatchers.jsonPath("$.updateUser").value(updateUser));
    }

    // test update returns bad request
    @Test
    void testUpdateEventHistory_givenBadDate_shouldReturnStatusBadRequest() throws Exception {
        var eventHistory = createEventHistoryData();
        eventHistory.setAcknowledgeFlag("Q");
        eventHistory.setUpdateUser(null);
        this.mockMvc.perform(MockMvcRequestBuilders
                        .put(EducGradTraxApiConstants.EVENT_HISTORY_MAPPING_V1)
                        .with(getMockAuthority(WRITE_SCOPE))
                        .content(JsonUtil.getJsonStringFromObject(eventHistory))
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // test update not found
    @Test
    void testUpdateEventHistory_givenInvalidId_shouldReturnStatusNotFound() throws Exception {
        var eventHistory = createEventHistoryData();
        eventHistory.setId(UUID.randomUUID());
        this.mockMvc.perform(MockMvcRequestBuilders
                        .put(EducGradTraxApiConstants.EVENT_HISTORY_MAPPING_V1)
                        .with(getMockAuthority(WRITE_SCOPE))
                        .content(JsonUtil.getJsonStringFromObject(eventHistory))
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    private EventHistory createEventHistoryData() throws JsonProcessingException {
        var event = TestUtils.createEvent("UPDATE_AUTHORITY_CONTACT", TestUtils.createAuthorityContact(), LocalDateTime.now(), eventRepository);
        var eventHistory = TestUtils.createEventHistory(event, LocalDateTime.now(), eventHistoryRepository);
        return mapper.toStructure(eventHistory);
    }

    private SecurityMockMvcRequestPostProcessors.OidcLoginRequestPostProcessor getMockAuthority(String scope) {
        final GrantedAuthority grantedAuthority = () -> scope;
        return oidcLogin().authorities(grantedAuthority);
    }
}