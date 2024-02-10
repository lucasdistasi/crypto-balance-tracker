package com.distasilucas.cryptobalancetracker.model.response.usercrypto;

import java.util.List;

public record PageUserCryptoResponse(
    int page,
    int totalPages,
    boolean hasNextPage,
    List<UserCryptoResponse> cryptos
) {

    public PageUserCryptoResponse(int page, int totalPages, List<UserCryptoResponse> cryptos) {
        this(page + 1, totalPages, totalPages - 1 > page, cryptos);
    }
}
