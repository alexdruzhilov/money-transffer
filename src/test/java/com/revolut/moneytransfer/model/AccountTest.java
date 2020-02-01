package com.revolut.moneytransfer.model;

import com.revolut.moneytransfer.util.RandomUtils;
import org.junit.jupiter.api.Test;

import static com.revolut.moneytransfer.util.Assertions.validationAssert;

public class AccountTest {

    @Test
    void accountName_validation_mustNotBeBlank() {
        final Account account = new Account();
        validationAssert(account, "name", "must not be blank");

        account.setName("");
        validationAssert(account, "name", "must not be blank");
    }

    @Test
    void accountName_validation_mustNotExceedSizeBoundaries() {
        final Account account = new Account();
        account.setName(RandomUtils.randomString(256));
        validationAssert(account, "name", "size must be between 0 and 255");
    }

    @Test
    void accountBalance_validation_mustBeGreaterThanZero() {
        final Account account = new Account();
        account.setBalance(-1);
        validationAssert(account, "balance", "must be greater than or equal to 0");
    }
}
