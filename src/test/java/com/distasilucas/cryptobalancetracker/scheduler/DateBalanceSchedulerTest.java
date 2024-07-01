package com.distasilucas.cryptobalancetracker.scheduler;

import com.distasilucas.cryptobalancetracker.entity.DateBalance;
import com.distasilucas.cryptobalancetracker.model.response.insights.BalancesResponse;
import com.distasilucas.cryptobalancetracker.repository.DateBalanceRepository;
import com.distasilucas.cryptobalancetracker.service.InsightsService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

class DateBalanceSchedulerTest {

    private final UUID RANDOM_UUID = UUID.fromString("60560fe6-8be2-460f-89ba-ef2e1c2e405b");
    private final MockedStatic<UUID> UUID_MOCK = mockStatic(UUID.class);

    @Mock
    private DateBalanceRepository dateBalancesRepositoryMock;

    @Mock
    private InsightsService insightsServiceMock;

    @Mock
    private Clock clockMock;

    private DateBalanceScheduler dateBalanceScheduler;

    @BeforeEach
    void setUp() {
        openMocks(this);
        dateBalanceScheduler = new DateBalanceScheduler(dateBalancesRepositoryMock, insightsServiceMock, clockMock);
    }

    @AfterEach
    void tearDown() {
        UUID_MOCK.close();
    }

    @Test
    void shouldSaveDateBalance() {
        var localDate = LocalDate.of(2024, 2, 22);
        var zonedDateTime = ZonedDateTime.of(localDate, LocalTime.MAX, ZoneId.of("UTC"));
        var balancesResponse = new BalancesResponse("1000", "800", "0.001");
        var dateBalance = new DateBalance(
            "60560fe6-8be2-460f-89ba-ef2e1c2e405b",
            localDate,
            "1000"
        );

        when(clockMock.instant()).thenReturn(zonedDateTime.toInstant());
        when(clockMock.getZone()).thenReturn(zonedDateTime.getZone());
        when(insightsServiceMock.retrieveTotalBalancesInsights()).thenReturn(Optional.of(balancesResponse));
        when(dateBalancesRepositoryMock.findDateBalanceByDate(localDate)).thenReturn(Optional.empty());
        UUID_MOCK.when(UUID::randomUUID).thenReturn(RANDOM_UUID);

        dateBalanceScheduler.saveDateBalance();

        verify(dateBalancesRepositoryMock, times(1)).save(dateBalance);
    }

    @Test
    void shouldUpdateDateBalance() {
        var localDate = LocalDate.of(2024, 2, 22);
        var zonedDateTime = ZonedDateTime.of(localDate, LocalTime.MAX, ZoneId.of("UTC"));
        var balancesResponse = new BalancesResponse("1050", "850", "0.00105");
        var dateBalance = new DateBalance("48793270-ff28-4a0f-98a5-8b43ed3df0d4", localDate, "1050");

        when(clockMock.instant()).thenReturn(zonedDateTime.toInstant());
        when(clockMock.getZone()).thenReturn(zonedDateTime.getZone());
        when(dateBalancesRepositoryMock.findDateBalanceByDate(localDate)).thenReturn(Optional.of(dateBalance));
        when(insightsServiceMock.retrieveTotalBalancesInsights()).thenReturn(Optional.of(balancesResponse));

        dateBalanceScheduler.saveDateBalance();

        verify(dateBalancesRepositoryMock, times(1)).save(dateBalance);
    }

}
