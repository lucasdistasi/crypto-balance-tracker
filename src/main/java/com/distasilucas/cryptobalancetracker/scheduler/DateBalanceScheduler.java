package com.distasilucas.cryptobalancetracker.scheduler;

import com.distasilucas.cryptobalancetracker.entity.DateBalance;
import com.distasilucas.cryptobalancetracker.repository.DateBalanceRepository;
import com.distasilucas.cryptobalancetracker.service.InsightsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;

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

        var now = LocalDate.now(clock);
        var totalBalances = insightsService.retrieveTotalBalancesInsights();
        var optionalDateBalance = dateBalancesRepository.findDateBalanceByDate(now);

        optionalDateBalance.ifPresentOrElse(dateBalance -> {
            var updatedDateBalances = new DateBalance(dateBalance.getId(), now, totalBalances);
            log.info("Updating balances for date {}. Old Balance: {}. New balances {}", now, dateBalance, updatedDateBalances);
            dateBalancesRepository.save(updatedDateBalances);
        }, () -> {
            log.info("Saving balances {} for date {}", totalBalances, now);
            dateBalancesRepository.save(new DateBalance(now, totalBalances));
        });
    }
}
