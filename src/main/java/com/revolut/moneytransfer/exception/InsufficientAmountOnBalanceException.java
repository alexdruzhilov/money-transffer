package com.revolut.moneytransfer.exception;

public class InsufficientAmountOnBalanceException extends RuntimeException {
    public InsufficientAmountOnBalanceException(String message) {
        super(message);
    }
}
