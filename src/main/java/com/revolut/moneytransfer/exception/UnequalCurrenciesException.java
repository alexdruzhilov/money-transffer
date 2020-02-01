package com.revolut.moneytransfer.exception;

public class UnequalCurrenciesException extends RuntimeException {
    public UnequalCurrenciesException(String message) {
        super(message);
    }
}
