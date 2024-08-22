package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.exception.InvalidParameterException;
import ca.bc.gov.educ.api.trax.exception.ServiceException;
import ca.bc.gov.educ.api.trax.exception.TraxAPIRuntimeException;
import ca.bc.gov.educ.api.trax.filter.EventHistoryFilterSpecifics;
import ca.bc.gov.educ.api.trax.filter.FilterOperation;
import ca.bc.gov.educ.api.trax.model.dto.Condition;
import ca.bc.gov.educ.api.trax.model.dto.Search;
import ca.bc.gov.educ.api.trax.model.dto.SearchCriteria;
import ca.bc.gov.educ.api.trax.model.dto.ValueType;
import ca.bc.gov.educ.api.trax.model.entity.EventHistoryEntity;
import ca.bc.gov.educ.api.trax.repository.EventHistoryRepository;
import ca.bc.gov.educ.api.trax.repository.EventRepository;
import ca.bc.gov.educ.api.trax.util.RequestUtil;
import ca.bc.gov.educ.api.trax.util.TransformUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jboss.threads.EnhancedQueueExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

@Slf4j
@Service
public class EventHistoryService {

    private final EventHistoryFilterSpecifics eventHistoryFilterSpecs;
    private final EventRepository eventRepository;
    private final EventHistoryRepository eventHistoryRepository;
    private final Executor paginatedQueryExecutor = new EnhancedQueueExecutor.Builder()
            .setThreadFactory(new ThreadFactoryBuilder().setNameFormat("async-pagination-query-executor-%d").build())
            .setCorePoolSize(2).setMaximumPoolSize(10).setKeepAliveTime(Duration.ofSeconds(60)).build();

    @Autowired
    public EventHistoryService(EventHistoryFilterSpecifics eventHistoryFilterSpecs, EventRepository eventRepository, EventHistoryRepository eventHistoryRepository) {
        this.eventHistoryFilterSpecs = eventHistoryFilterSpecs;
        this.eventRepository = eventRepository;
        this.eventHistoryRepository = eventHistoryRepository;
    }

    /**
     * Deletes old records from event and event history
     * @param sinceBefore the LocalDateTime object. Removes records older than this.
     * @throws ServiceException if there is an issue
     */
    public void purgeOldEventAndEventHistoryRecords(LocalDateTime sinceBefore) throws ServiceException {
        try {
            this.eventRepository.deleteByCreateDateLessThan(sinceBefore);
        } catch (Exception e) {
            throw new ServiceException(String.format("Exception encountered when attempting old Event History Purge: %s", e.getMessage()));
        }
    }

    private Specification<EventHistoryEntity> getSpecifications(Specification<EventHistoryEntity> schoolSpecs, int i, Search search) {
        if (i == 0) {
            schoolSpecs = getEventHistorySpecification(search.getSearchCriteriaList());
        } else {
            if (search.getCondition() == Condition.AND) {
                schoolSpecs = schoolSpecs.and(getEventHistorySpecification(search.getSearchCriteriaList()));
            } else {
                schoolSpecs = schoolSpecs.or(getEventHistorySpecification(search.getSearchCriteriaList()));
            }
        }
        return schoolSpecs;
    }

    public Specification<EventHistoryEntity> setSpecificationAndSortCriteria(String sortCriteriaJson, String searchCriteriaListJson, ObjectMapper objectMapper, List<Sort.Order> sorts) {
        Specification<EventHistoryEntity> eventHistoryEntitySpecification = null;
        try {
            RequestUtil.getSortCriteria(sortCriteriaJson, objectMapper, sorts);
            if (StringUtils.isNotBlank(searchCriteriaListJson)) {
                List<Search> searches = objectMapper.readValue(searchCriteriaListJson, new TypeReference<>() {
                });
                int i = 0;
                for (var search : searches) {
                    eventHistoryEntitySpecification = getSpecifications(eventHistoryEntitySpecification, i, search);
                    i++;
                }
            }
        } catch (JsonProcessingException e) {
            throw new TraxAPIRuntimeException(e.getMessage());
        }
        return eventHistoryEntitySpecification;
    }

    private Specification<EventHistoryEntity> getEventHistorySpecification(List<SearchCriteria> criteriaList) {
        Specification<EventHistoryEntity> eventHistoryEntitySpecification = null;
        if (!criteriaList.isEmpty()) {
            int i = 0;
            for (SearchCriteria criteria : criteriaList) {
                if (criteria.getKey() != null && criteria.getOperation() != null && criteria.getValueType() != null) {
                    var criteriaValue = criteria.getValue();
                    if(StringUtils.isNotBlank(criteria.getValue()) && TransformUtil.isUppercaseField(EventHistoryEntity.class, criteria.getKey())) {
                        criteriaValue = criteriaValue.toUpperCase();
                    }
                    Specification<EventHistoryEntity> typeSpecification = getTypeSpecification(criteria.getKey(), criteria.getOperation(), criteriaValue, criteria.getValueType());
                    eventHistoryEntitySpecification = getSpecificationPerGroup(eventHistoryEntitySpecification, i, criteria, typeSpecification);
                    i++;
                } else {
                    throw new InvalidParameterException("Search Criteria can not contain null values for key, value and operation type");
                }
            }
        }
        return eventHistoryEntitySpecification;
    }

    private Specification<EventHistoryEntity> getSpecificationPerGroup(Specification<EventHistoryEntity> eventHistoryEntitySpecification, int i, SearchCriteria criteria, Specification<EventHistoryEntity> typeSpecification) {
        if (i == 0) {
            eventHistoryEntitySpecification = Specification.where(typeSpecification);
        } else {
            if (criteria.getCondition() == Condition.AND) {
                eventHistoryEntitySpecification = eventHistoryEntitySpecification.and(typeSpecification);
            } else {
                eventHistoryEntitySpecification = eventHistoryEntitySpecification.or(typeSpecification);
            }
        }
        return eventHistoryEntitySpecification;
    }

    private Specification<EventHistoryEntity> getTypeSpecification(String key, FilterOperation filterOperation, String value, ValueType valueType) {
        Specification<EventHistoryEntity> schoolEntitySpecification = null;
        switch (valueType) {
            case STRING:
                schoolEntitySpecification = eventHistoryFilterSpecs.getStringTypeSpecification(key, value, filterOperation);
                break;
            case DATE_TIME:
                schoolEntitySpecification = eventHistoryFilterSpecs.getDateTimeTypeSpecification(key, value, filterOperation);
                break;
            case LONG:
                schoolEntitySpecification = eventHistoryFilterSpecs.getLongTypeSpecification(key, value, filterOperation);
                break;
            case INTEGER:
                schoolEntitySpecification = eventHistoryFilterSpecs.getIntegerTypeSpecification(key, value, filterOperation);
                break;
            case DATE:
                schoolEntitySpecification = eventHistoryFilterSpecs.getDateTypeSpecification(key, value, filterOperation);
                break;
            case UUID:
                schoolEntitySpecification = eventHistoryFilterSpecs.getUUIDTypeSpecification(key, value, filterOperation);
                break;
            default:
                break;
        }
        return schoolEntitySpecification;
    }


    @Transactional(propagation = Propagation.SUPPORTS)
    public CompletableFuture<Page<EventHistoryEntity>> findAll(Specification<EventHistoryEntity> eventHistorySpecs, final Integer pageNumber, final Integer pageSize, final List<Sort.Order> sorts) {
        log.trace("In find all query: {}", eventHistorySpecs);
        return CompletableFuture.supplyAsync(() -> {
            Pageable paging = PageRequest.of(pageNumber, pageSize, Sort.by(sorts));
            try {
                log.trace("Running paginated query: {}", eventHistorySpecs);
                var results = this.eventHistoryRepository.findAll(eventHistorySpecs, paging);
                log.trace("Paginated query returned with results: {}", results);
                return results;
            } catch (final Throwable ex) {
                log.error("Failure querying for paginated schools: {}", ex.getMessage());
                throw new CompletionException(ex);
            }
        }, paginatedQueryExecutor);

    }
}
