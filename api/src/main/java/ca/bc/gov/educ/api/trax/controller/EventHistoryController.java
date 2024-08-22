package ca.bc.gov.educ.api.trax.controller;
import ca.bc.gov.educ.api.trax.model.dto.EventHistory;
import ca.bc.gov.educ.api.trax.model.entity.EventHistoryEntity;
import ca.bc.gov.educ.api.trax.model.transformer.EventHistoryTransformer;
import ca.bc.gov.educ.api.trax.service.EventHistoryService;
import ca.bc.gov.educ.api.trax.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@CrossOrigin
@RestController
//@RequestMapping(EducGradTraxApiConstants.GRAD_DISTRICT_URL_MAPPING)
public class EventHistoryController {

    private final EventHistoryService eventHistoryService;
    private final EventHistoryTransformer transformer;

    @Autowired
    public EventHistoryController(EventHistoryService eventHistoryService, EventHistoryTransformer transformer) {
        this.eventHistoryService = eventHistoryService;
        this.transformer = transformer;
    }

    public CompletableFuture<Page<EventHistory>> findAll(Integer pageNumber, Integer pageSize, String sortCriteriaJson, String searchCriteriaListJson) {
        final List<Sort.Order> sorts = new ArrayList<>();
        Specification<EventHistoryEntity> eventHistorySpecs = eventHistoryService.setSpecificationAndSortCriteria(sortCriteriaJson, searchCriteriaListJson, JsonUtil.mapper, sorts);
        return this.eventHistoryService.findAll(eventHistorySpecs, pageNumber, pageSize, sorts).thenApplyAsync(schoolEntities -> schoolEntities.map(transformer::toDTO));
    }

}
