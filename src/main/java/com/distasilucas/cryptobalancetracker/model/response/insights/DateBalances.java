package com.distasilucas.cryptobalancetracker.model.response.insights;

import java.io.Serializable;

public record DateBalances(
    String date,
    BalancesResponse balances
) implements Serializable {
}
