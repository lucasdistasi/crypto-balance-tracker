package com.distasilucas.cryptobalancetracker.exception;

import static com.distasilucas.cryptobalancetracker.constants.ExceptionConstants.NOT_ENOUGH_BALANCE;

public class InsufficientBalanceException extends RuntimeException {

    public InsufficientBalanceException() {
        super(NOT_ENOUGH_BALANCE);
    }
}
