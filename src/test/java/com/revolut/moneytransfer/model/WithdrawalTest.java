package com.revolut.moneytransfer.model;

import org.junit.jupiter.api.Test;

import static com.revolut.moneytransfer.util.Assertions.validationAssert;

public class WithdrawalTest {
    @Test
    void withdrawalCurrency_validation_mustNotBeBlank() {
        final Withdrawal withdrawal = new Withdrawal();
        validationAssert(withdrawal, "currency", "must not be blank");

        withdrawal.setCurrency("");
        validationAssert(withdrawal, "currency", "must not be blank");
    }

    @Test
    void withdrawalAmount_validation_mustBeGreaterThanZero() {
        final Withdrawal withdrawal = new Withdrawal();
        withdrawal.setAmount(-1);
        validationAssert(withdrawal, "amount", "must be greater than or equal to 0");
    }
}
