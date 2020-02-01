package com.revolut.moneytransfer.service;

import com.revolut.moneytransfer.exception.InsufficientAmountOnBalanceException;
import com.revolut.moneytransfer.model.Account;
import com.revolut.moneytransfer.model.Deposit;
import com.revolut.moneytransfer.model.Transfer;
import com.revolut.moneytransfer.model.Withdrawal;
import io.micronaut.test.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
public class AccountServiceIntegrationTest {

    @Inject
    private AccountService accountService;

    @Test
    void createAccount_withValidData_success() {
        final Account account = accountService.createAccount(Account.builder()
                .name("Alice")
                .build());
        assertNotNull(account.getId());
    }

    @Test
    void getAccountById_notFound_shouldReturnEmptyResult() {
        final Optional<Account> account = accountService.getAccountById("aliceId");
        assertFalse(account.isPresent());
    }

    @Test
    void getAccountById_found_shouldReturnAccountData() {
        final Account account = accountService.createAccount(Account.builder()
                .name("Alice")
                .build());

        final Optional<Account> result = accountService.getAccountById(account.getId());
        assertTrue(result.isPresent());
    }

    @Test
    void createDepositOperation_withValidData_success() {
        final Account account = accountService.createAccount(Account.builder()
                .name("Alice")
                .build());

        final Deposit deposit = new Deposit();
        deposit.setAmount(10);
        deposit.setCurrency("USD");
        assertDoesNotThrow(() -> {
            accountService.createDepositOperation(account.getId(), deposit);
        });

        final Account result = accountService.getAccountById(account.getId()).get();
        assertEquals(10, result.getBalance());
    }

    @Test
    void createWithdrawOperation_withValidData_success() {
        final Account account = accountService.createAccount(Account.builder()
                .name("Alice")
                .build());

        final Deposit deposit = new Deposit();
        deposit.setAmount(10);
        deposit.setCurrency("USD");
        assertDoesNotThrow(() -> {
            accountService.createDepositOperation(account.getId(), deposit);
        });

        final Withdrawal withdrawal = new Withdrawal();
        withdrawal.setAmount(10);
        withdrawal.setCurrency("USD");
        assertDoesNotThrow(() -> {
            accountService.createWithdrawalOperation(account.getId(), withdrawal);
        });

        final Account result = accountService.getAccountById(account.getId()).get();
        assertEquals(0, result.getBalance());
    }

    @Test
    void createWithdrawOperation_insufficientBalance_shouldThrowException() {
        final Account account = accountService.createAccount(Account.builder()
                .name("Alice")
                .build());

        final Deposit deposit = new Deposit();
        deposit.setAmount(10);
        deposit.setCurrency("USD");
        assertDoesNotThrow(() -> {
            accountService.createDepositOperation(account.getId(), deposit);
        });

        final Withdrawal withdrawal = new Withdrawal();
        withdrawal.setAmount(11);
        withdrawal.setCurrency("USD");

        assertThrows(InsufficientAmountOnBalanceException.class, () -> {
            accountService.createWithdrawalOperation(account.getId(), withdrawal);
        });
    }

    @Test
    void createTransferOperation_withValidData_success() {
        final Account alice = accountService.createAccount(Account.builder()
                .name("Alice")
                .build());

        final Deposit aliceDeposit = new Deposit();
        aliceDeposit.setAmount(10);
        aliceDeposit.setCurrency("USD");
        assertDoesNotThrow(() -> {
            accountService.createDepositOperation(alice.getId(), aliceDeposit);
        });

        final Account bob = accountService.createAccount(Account.builder()
                .name("Alice")
                .build());

        final Transfer transfer = new Transfer();
        transfer.setAmount(10);
        transfer.setCurrency("USD");

        assertDoesNotThrow(() -> {
            accountService.createTransferOperation(alice.getId(), bob.getId(), transfer);
        });

        final Account aliceResult = accountService.getAccountById(alice.getId()).get();
        assertEquals(0, aliceResult.getBalance());

        final Account bobResult = accountService.getAccountById(bob.getId()).get();
        assertEquals(10, bobResult.getBalance());
    }

    @Test
    void createTransferOperation_withInsufficientBalance_shouldThrowException() {
        final Account alice = accountService.createAccount(Account.builder()
                .name("Alice")
                .build());

        final Deposit aliceDeposit = new Deposit();
        aliceDeposit.setAmount(10);
        aliceDeposit.setCurrency("USD");
        assertDoesNotThrow(() -> {
            accountService.createDepositOperation(alice.getId(), aliceDeposit);
        });

        final Account bob = accountService.createAccount(Account.builder()
                .name("Alice")
                .build());

        final Transfer transfer = new Transfer();
        transfer.setAmount(11);
        transfer.setCurrency("USD");

        assertThrows(InsufficientAmountOnBalanceException.class, () -> {
            accountService.createTransferOperation(alice.getId(), bob.getId(), transfer);
        });

        final Account aliceResult = accountService.getAccountById(alice.getId()).get();
        assertEquals(10, aliceResult.getBalance());

        final Account bobResult = accountService.getAccountById(bob.getId()).get();
        assertEquals(0, bobResult.getBalance());
    }
}
