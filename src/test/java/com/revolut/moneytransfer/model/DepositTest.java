package com.revolut.moneytransfer.model;

import org.junit.jupiter.api.Test;

import static com.revolut.moneytransfer.util.Assertions.validationAssert;

public class DepositTest {
    @Test
    void depositCurrency_validation_mustNotBeBlank() {
        final Deposit deposit = new Deposit();
        validationAssert(deposit, "currency", "must not be blank");

        deposit.setCurrency("");
        validationAssert(deposit, "currency", "must not be blank");
    }

    @Test
    void depositAmount_validation_mustBeGreaterThanZero() {
        final Deposit deposit = new Deposit();
        deposit.setAmount(-1);
        validationAssert(deposit, "amount", "must be greater than or equal to 0");
    }
}
