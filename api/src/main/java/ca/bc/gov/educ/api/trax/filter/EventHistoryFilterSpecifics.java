package ca.bc.gov.educ.api.trax.filter;

import ca.bc.gov.educ.api.trax.model.entity.EventHistoryEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class EventHistoryFilterSpecifics extends BaseFilterSpecs<EventHistoryEntity> {
    /**
     * Instantiates a new Base filter specs.
     *
     * @param eventHistoryEntityChronoLocalDateFilterSpecifications     the date filter specifications
     * @param eventHistoryEntityChronoLocalDateTimeFilterSpecifications the date time filter specifications
     * @param eventHistoryEntityIntegerFilterSpecifications             the integer filter specifications
     * @param eventHistoryEntityStringFilterSpecifications              the string filter specifications
     * @param eventHistoryEntityLongFilterSpecifications                the long filter specifications
     * @param uuidFilterSpecifications                                  the uuid filter specifications
     * @param converters                                                the converters
     */
    public EventHistoryFilterSpecifics(FilterSpecifications<EventHistoryEntity, ChronoLocalDate> eventHistoryEntityChronoLocalDateFilterSpecifications, FilterSpecifications<EventHistoryEntity, ChronoLocalDateTime<?>> eventHistoryEntityChronoLocalDateTimeFilterSpecifications, FilterSpecifications<EventHistoryEntity, Integer> eventHistoryEntityIntegerFilterSpecifications, FilterSpecifications<EventHistoryEntity, String> eventHistoryEntityStringFilterSpecifications, FilterSpecifications<EventHistoryEntity, Long> eventHistoryEntityLongFilterSpecifications, FilterSpecifications<EventHistoryEntity, UUID> uuidFilterSpecifications, Converters converters) {
        super(eventHistoryEntityChronoLocalDateFilterSpecifications, eventHistoryEntityChronoLocalDateTimeFilterSpecifications, eventHistoryEntityIntegerFilterSpecifications, eventHistoryEntityStringFilterSpecifications, eventHistoryEntityLongFilterSpecifications, uuidFilterSpecifications, converters);
    }
}
