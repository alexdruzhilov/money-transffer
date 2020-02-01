package com.revolut.moneytransfer.service;

import com.revolut.moneytransfer.database.AccountMapper;
import com.revolut.moneytransfer.exception.AccountNotFoundException;
import com.revolut.moneytransfer.exception.InsufficientAmountOnBalanceException;
import com.revolut.moneytransfer.exception.UnequalCurrenciesException;
import com.revolut.moneytransfer.model.Account;
import com.revolut.moneytransfer.model.Deposit;
import com.revolut.moneytransfer.model.Transfer;
import com.revolut.moneytransfer.model.Withdrawal;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import javax.inject.Singleton;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Optional;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor
public class AccountService {
    private final SqlSessionFactory sqlSessionFactory;

    /**
     * Validate account data and create new bank account
     * @param account Account data
     * @return Account data with filled id
     * @throws IllegalArgumentException if account is null
     * @throws ConstraintViolationException error occurs during constraint checking process
     */
    public Account createAccount(@Valid final Account account) {
        account.setId(UUID.randomUUID().toString());
        try (final SqlSession sqlSession = sqlSessionFactory.openSession(true)) {
            getAccountMapper(sqlSession).createAccount(account);
        }
        return account;
    }

    /**
     * Get account by id
     * @param id Id of account
     * @return Optional<Account>
     * @throws NullPointerException if id is null
     */
    public Optional<Account> getAccountById(@NotNull final String id) {
        try (final SqlSession sqlSession = sqlSessionFactory.openSession(true)) {
            return Optional.ofNullable(getAccountMapper(sqlSession).findById(id));
        }
    }

    /**
     * Deposit on account balance
     * @param accountId target account id
     * @param deposit operation data
     * @throws AccountNotFoundException if no such account in database
     * @throws InsufficientAmountOnBalanceException if operation tries to change balance to less than zero
     */
    public void createDepositOperation(@NotEmpty final String accountId, @Valid final Deposit deposit) {
        changeBalance(accountId, deposit.getAmount(), deposit.getCurrency());
    }

    /**
     * Withdraw from account balance
     * @param accountId target account id
     * @param withdrawal operation data
     * @throws AccountNotFoundException if no such account in database
     * @throws InsufficientAmountOnBalanceException if operation tries to change balance to less than zero
     */
    public void createWithdrawalOperation(@NotEmpty final String accountId, @Valid final Withdrawal withdrawal) {
        changeBalance(accountId, -withdrawal.getAmount(), withdrawal.getCurrency());
    }

    /**
     * Transfer money from source to target account
     * @param sourceAccountId account id to withdraw money
     * @param targetAccountId account id deposit money
     * @param transfer operation data
     * @throws AccountNotFoundException if no such account in database
     * @throws InsufficientAmountOnBalanceException if operation tries to change balance to invalid amount
     * @throws UnequalCurrenciesException if operation has different currency than account
     */
    public void createTransferOperation(@NotEmpty final String sourceAccountId,
                                        @NotEmpty final String targetAccountId,
                                        @Valid final Transfer transfer) {
        try (final SqlSession sqlSession = sqlSessionFactory.openSession(false)) {
            final AccountMapper mapper = getAccountMapper(sqlSession);

            final Account sourceAccount = mapper.findById(sourceAccountId);
            if (sourceAccount == null) {
                throw new AccountNotFoundException("Source account not found: " + sourceAccountId);
            }

            final long newSourceBalance = sourceAccount.getBalance() - transfer.getAmount();
            if (newSourceBalance < 0) {
                throw new InsufficientAmountOnBalanceException("Account has not enough money on balance: " + sourceAccountId);
            }

            final Account targetAccount = mapper.findById(targetAccountId);
            if (targetAccount == null) {
                throw new AccountNotFoundException("Target account not found: " + targetAccountId);
            }

            if (!sourceAccount.getCurrency().equals(transfer.getCurrency()) ||
                !targetAccount.getCurrency().equals(transfer.getCurrency())) {
                throw new UnequalCurrenciesException("Account currency differs with the currency of operation");
            }

            final long newTargetBalance = targetAccount.getBalance() + transfer.getAmount();
            mapper.updateBalance(sourceAccountId, newSourceBalance);
            mapper.updateBalance(targetAccountId, newTargetBalance);

            sqlSession.commit();
        }
    }

    /**
     * Change account balance on some amount
     * @param accountId Id of account
     * @param amount Amount of money to append on account balance (could be less than zero in case of withdrawal)
     * @param currency Currency of operation
     * @throws AccountNotFoundException if no such account in database
     * @throws InsufficientAmountOnBalanceException if operation tries to change balance to less than zero
     * @throws UnequalCurrenciesException if operation has different currency than account
     */
    public void changeBalance(final String accountId, long amount, final String currency) {
        try (final SqlSession sqlSession = sqlSessionFactory.openSession(false)) {
            final AccountMapper mapper = getAccountMapper(sqlSession);
            final Account account = mapper.findById(accountId);
            if (account == null) {
                throw new AccountNotFoundException("Account not found: " + accountId);
            }

            if (!account.getCurrency().equals(currency)) {
                throw new UnequalCurrenciesException("Account currency differs with the currency of operation");
            }

            final long newBalance = account.getBalance() + amount;
            if (newBalance < 0) {
                throw new InsufficientAmountOnBalanceException("Account has not enough money on balance: " + accountId);
            }

            mapper.updateBalance(accountId, newBalance);
            sqlSession.commit();
        }
    }

    private AccountMapper getAccountMapper(final SqlSession sqlSession) {
        return sqlSession.getMapper(AccountMapper.class);
    }
}
