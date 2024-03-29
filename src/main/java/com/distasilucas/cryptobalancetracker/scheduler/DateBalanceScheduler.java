package com.distasilucas.cryptobalancetracker.scheduler;

import com.distasilucas.cryptobalancetracker.entity.DateBalance;
import com.distasilucas.cryptobalancetracker.model.response.insights.BalancesResponse;
import com.distasilucas.cryptobalancetracker.repository.DateBalanceRepository;
import com.distasilucas.cryptobalancetracker.service.InsightsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class DateBalanceScheduler {

    private final DateBalanceRepository dateBalancesRepository;
    private final InsightsService insightsService;
    private final Clock clock;

    @Scheduled(cron = "${save-day-balance-cron}")
    public void saveDateBalance() {
        log.info("Running cron to save daily balance");

        var now = LocalDateTime.now(clock).toLocalDate().atTime(LocalTime.of(23, 59, 59, 0));
        var totalUSDBalance = insightsService.retrieveTotalBalancesInsights()
            .map(BalancesResponse::totalUSDBalance);
        var optionalDateBalance = dateBalancesRepository.findDateBalanceByDate(now);

        totalUSDBalance.ifPresent(balance -> optionalDateBalance.ifPresentOrElse(
            dateBalance -> {
                log.info("Updating balance for date {}. Old Balance: {}. New balance {}", now, dateBalance.balance(), balance);
                dateBalancesRepository.save(new DateBalance(dateBalance.id(), now, balance));
            },
            () -> {
                log.info("Saving balance {} for date {}", balance, now);
                dateBalancesRepository.save(new DateBalance(now, balance));
            }
        ));
    }
}
