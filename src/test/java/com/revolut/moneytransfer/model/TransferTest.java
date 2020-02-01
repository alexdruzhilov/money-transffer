package com.revolut.moneytransfer.model;

import org.junit.jupiter.api.Test;

import static com.revolut.moneytransfer.util.Assertions.validationAssert;

public class TransferTest {

    @Test
    void transferCurrency_validation_mustNotBeBlank() {
        final Transfer transfer = new Transfer();
        validationAssert(transfer, "currency", "must not be blank");

        transfer.setCurrency("");
        validationAssert(transfer, "currency", "must not be blank");
    }

    @Test
    void transferAmount_validation_mustBeGreaterThanZero() {
        final Transfer transfer = new Transfer();
        transfer.setAmount(-1);
        validationAssert(transfer, "amount", "must be greater than or equal to 0");
    }
}
