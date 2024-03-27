package com.distasilucas.cryptobalancetracker.model.response.pricetarget;

import java.util.List;

public record PagePriceTargetResponse(
    int page,
    int totalPages,
    boolean hasNextPage,
    List<PriceTargetResponse> targets
) {

    public PagePriceTargetResponse(int page, int totalPages, List<PriceTargetResponse> targets) {
        this(page + 1, totalPages, totalPages - 1 > page, targets);
    }
}
