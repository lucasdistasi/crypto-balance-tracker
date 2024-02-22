package com.distasilucas.cryptobalancetracker.model;

import java.time.LocalDateTime;

public record DateBalancesInsightsRange(
    LocalDateTime from,
    LocalDateTime to
) {
}
