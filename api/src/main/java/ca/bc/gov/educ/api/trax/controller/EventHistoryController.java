package ca.bc.gov.educ.api.trax.controller;
import ca.bc.gov.educ.api.trax.mapper.EventHistoryMapper;
import ca.bc.gov.educ.api.trax.model.dto.EventHistory;
import ca.bc.gov.educ.api.trax.model.entity.EventHistoryEntity;
import ca.bc.gov.educ.api.trax.service.EventHistoryService;
import ca.bc.gov.educ.api.trax.util.EducGradTraxApiConstants;
import ca.bc.gov.educ.api.trax.util.JsonUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@CrossOrigin
@RestController
@RequestMapping(EducGradTraxApiConstants.EVENT_HISTORY_MAPPING_V1)
@Tag(name = "Event History", description = "Endpoints for Event History.")
public class EventHistoryController {

    private final EventHistoryService eventHistoryService;
    private final EventHistoryMapper mapper;

    @Autowired
    public EventHistoryController(EventHistoryService eventHistoryService, EventHistoryMapper mapper) {
        this.eventHistoryService = eventHistoryService;
        this.mapper = mapper;
    }

    @GetMapping("/paginated")
    @Async
    @PreAuthorize("hasAuthority('SCOPE_READ_EVENT_HISTORY')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR.")})
    @Transactional(readOnly = true)
    @Operation(summary = "Find All Event History paginated", description = "Find all Event History using pagination", tags = { "Event History" })
    public CompletableFuture<Page<EventHistory>> findAll(@RequestParam(name = "pageNumber", defaultValue = "0") Integer pageNumber,
                                                         @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                         @RequestParam(name = "sort", defaultValue = "") String sort,
                                                         @RequestParam(name = "searchParams", required = false) String searchParams) {
        final List<Sort.Order> sorts = new ArrayList<>();
        Specification<EventHistoryEntity> eventHistorySpecs = eventHistoryService.setSpecificationAndSortCriteria(sort, searchParams, JsonUtil.mapper, sorts);
        return this.eventHistoryService.findAll(eventHistorySpecs, pageNumber, pageSize, sorts).thenApplyAsync(schoolEntities -> schoolEntities.map(mapper::toStructure));
    }

    @PutMapping
    @PreAuthorize("hasAuthority('SCOPE_WRITE_EVENT_HISTORY')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR.")})
    @Transactional
    @Operation(summary = "Update an event history entity", description = "Update a valid event history", tags = { "Event History" })
    public EventHistory updateEventHistory(@Valid @RequestBody EventHistory eventHistory) {
       return this.eventHistoryService.updateEventHistory(eventHistory);
    }
}
