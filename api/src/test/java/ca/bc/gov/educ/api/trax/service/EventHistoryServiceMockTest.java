package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.exception.ServiceException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;


class EventHistoryServiceMockTest extends BaseReplicationServiceTest {

    @MockBean
    EventHistoryService eventHistoryService;

    @Test
    void purgeOldEventAndEventHistoryRecords_givenExceptionThrown_shouldThrowException() {
        final String ERROR_MSG = "Exception encountered";
        final LocalDateTime localDateTime = LocalDateTime.now();
        doThrow(new ServiceException(ERROR_MSG)).when(eventHistoryService).purgeOldEventAndEventHistoryRecords(localDateTime);
        Exception exception = assertThrows(ServiceException.class, () -> {
            eventHistoryService.purgeOldEventAndEventHistoryRecords(localDateTime);
        });
        assertTrue(exception.getMessage().contains(ERROR_MSG));
    }
}