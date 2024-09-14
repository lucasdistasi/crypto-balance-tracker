package com.distasilucas.cryptobalancetracker.model.response.insights;

import java.io.Serializable;

public record DifferencesChanges(
    String usdDifference,
    String eurDifference,
    String btcDifference
) implements Serializable {
}
