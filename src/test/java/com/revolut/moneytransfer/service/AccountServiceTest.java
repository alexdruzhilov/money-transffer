package com.revolut.moneytransfer.service;

import com.revolut.moneytransfer.database.AccountMapper;
import com.revolut.moneytransfer.exception.AccountNotFoundException;
import com.revolut.moneytransfer.exception.InsufficientAmountOnBalanceException;
import com.revolut.moneytransfer.model.Account;
import com.revolut.moneytransfer.model.Deposit;
import com.revolut.moneytransfer.model.Transfer;
import com.revolut.moneytransfer.model.Withdrawal;
import com.revolut.moneytransfer.util.RandomUtils;
import io.micronaut.test.annotation.MicronautTest;
import io.micronaut.test.annotation.MockBean;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.validation.ConstraintViolationException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@MicronautTest
public class AccountServiceTest {

    @Inject
    private AccountService accountService;

    @Inject
    private SqlSessionFactory sqlSessionFactory;

    private AccountMapper accountMapper;

    @MockBean(SqlSessionFactory.class)
    SqlSessionFactory sqlSessionFactory() {
        final SqlSessionFactory mock = mock(SqlSessionFactory.class);
        final SqlSession sqlSession = mock(SqlSession.class);
        accountMapper = mock(AccountMapper.class);
        when(sqlSession.getMapper(AccountMapper.class))
                .thenReturn(accountMapper);
        when(mock.openSession())
                .thenReturn(sqlSession);
        when(mock.openSession(anyBoolean()))
                .thenReturn(sqlSession);
        return mock;
    }

    @Test
    void createAccount_withValidData_success() {
        final Account account = accountService.createAccount(Account.builder()
                .name("Alice")
                .build());
        assertNotNull(account.getId());
    }

    @Test
    void createAccount_withNullValue_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> {
            accountService.createAccount(null);
        });
    }

    @Test
    void createAccount_withoutName_shouldThrowException() {
        assertThrows(ConstraintViolationException.class, () -> {
            accountService.createAccount(Account.builder()
                    .build());
        });
    }

    @Test
    void createAccount_withTooLongName_shouldThrowException() {
        assertThrows(ConstraintViolationException.class, () -> {
            accountService.createAccount(Account.builder()
                    .name(RandomUtils.randomString(256))
                    .build());
        });
    }

    @Test
    void getAccountById_notFound_shouldReturnEmptyResult() {
        final Optional<Account> account = accountService.getAccountById("aliceId");
        assertFalse(account.isPresent());
    }

    @Test
    void getAccountById_found_shouldReturnAccountData() {
        when(accountMapper.findById("aliceId"))
                .thenReturn(Account.builder()
                        .id("aliceId")
                        .build());

        final Optional<Account> account = accountService.getAccountById("aliceId");
        assertTrue(account.isPresent());
    }


    @Test
    void createDepositOperation_withValidData_success() {
        when(accountMapper.findById("aliceId"))
                .thenReturn(Account.builder()
                        .id("aliceId")
                        .build());

        final Deposit deposit = new Deposit();
        deposit.setAmount(10);
        deposit.setCurrency("USD");
        accountService.createDepositOperation("aliceId", deposit);
    }

    @Test
    void createDepositOperation_accountNotFound_shouldThrowException() {
        final Deposit deposit = new Deposit();
        deposit.setAmount(10);
        deposit.setCurrency("USD");
        assertThrows(AccountNotFoundException.class, () -> {
            accountService.createDepositOperation("aliceId", deposit);
        });
    }

    @Test
    void createWithdrawOperation_withValidData_success() {
        when(accountMapper.findById("aliceId"))
                .thenReturn(Account.builder()
                        .id("aliceId")
                        .balance(100)
                        .build());

        final Withdrawal withdrawal = new Withdrawal();
        withdrawal.setAmount(10);
        withdrawal.setCurrency("USD");
        accountService.createWithdrawalOperation("aliceId", withdrawal);
    }

    @Test
    void createWithdrawOperation_accountNotFound_shouldThrowException() {
        final Withdrawal withdrawal = new Withdrawal();
        withdrawal.setAmount(10);
        withdrawal.setCurrency("USD");
        assertThrows(AccountNotFoundException.class, () -> {
            accountService.createWithdrawalOperation("aliceId", withdrawal);
        });
    }

    @Test
    void createWithdrawOperation_withInsufficientAmount_shouldThrowException() {
        when(accountMapper.findById("aliceId"))
                .thenReturn(Account.builder()
                        .id("aliceId")
                        .balance(10)
                        .build());

        final Withdrawal withdrawal = new Withdrawal();
        withdrawal.setAmount(11);
        withdrawal.setCurrency("USD");

        assertThrows(InsufficientAmountOnBalanceException.class, () -> {
            accountService.createWithdrawalOperation("aliceId", withdrawal);
        });
    }

    @Test
    void createTransferOperation_withValidData_success() {
        when(accountMapper.findById("aliceId"))
                .thenReturn(Account.builder()
                        .id("aliceId")
                        .balance(100)
                        .build());

        when(accountMapper.findById("bobId"))
                .thenReturn(Account.builder()
                        .id("bobId")
                        .balance(200)
                        .build());

        final Transfer transfer = new Transfer();
        transfer.setAmount(100);
        transfer.setCurrency("USD");
        assertDoesNotThrow(() -> {
            accountService.createTransferOperation("aliceId", "bobId", transfer);
        });
    }

    @Test
    void createTransferOperation_sourceAccountNotFound_shouldThrowException() {
        when(accountMapper.findById("bobId"))
                .thenReturn(Account.builder()
                        .id("bobId")
                        .balance(200)
                        .build());

        final Transfer transfer = new Transfer();
        transfer.setAmount(100);
        transfer.setCurrency("USD");
        assertThrows(AccountNotFoundException.class, () -> {
            accountService.createTransferOperation("aliceId", "bobId", transfer);
        });
    }

    @Test
    void createTransferOperation_targetAccountNotFound_shouldThrowException() {
        when(accountMapper.findById("aliceId"))
                .thenReturn(Account.builder()
                        .id("aliceId")
                        .balance(100)
                        .build());

        final Transfer transfer = new Transfer();
        transfer.setAmount(100);
        transfer.setCurrency("USD");
        assertThrows(AccountNotFoundException.class, () -> {
            accountService.createTransferOperation("aliceId", "bobId", transfer);
        });
    }

    @Test
    void createTransferOperation_withInsufficientAmount_shouldThrowException() {
        when(accountMapper.findById("aliceId"))
                .thenReturn(Account.builder()
                        .id("aliceId")
                        .balance(100)
                        .build());

        when(accountMapper.findById("bobId"))
                .thenReturn(Account.builder()
                        .id("bobId")
                        .balance(200)
                        .build());

        final Transfer transfer = new Transfer();
        transfer.setAmount(200);
        transfer.setCurrency("USD");
        assertThrows(InsufficientAmountOnBalanceException.class, () -> {
            accountService.createTransferOperation("aliceId", "bobId", transfer);
        });
    }
}
