package com.revolut.moneytransfer.controller;

import com.revolut.moneytransfer.exception.AccountNotFoundException;
import com.revolut.moneytransfer.exception.InsufficientAmountOnBalanceException;
import com.revolut.moneytransfer.model.Account;
import com.revolut.moneytransfer.model.Deposit;
import com.revolut.moneytransfer.model.Transfer;
import com.revolut.moneytransfer.model.Withdrawal;
import com.revolut.moneytransfer.service.AccountService;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Error;
import io.micronaut.http.annotation.*;

import javax.inject.Inject;
import javax.validation.constraints.NotBlank;

/**
 * Bank account API
 */
@Controller("/account")
public class AccountController {

    @Inject
    private AccountService accountService;

    /**
     * Get account by id
     * @param id Account id
     * @return Account object or 404 error if not found
     */
    @Get("/{id}")
    @Status(HttpStatus.OK)
    public Account getAccount(@NotBlank final String id) {
        return accountService.getAccountById(id)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));
    }

    /**
     * Create new bank account
     * @param account Account data necessary for bank account creation
     * @return code 201 with account data or 400 code if request data is invalid
     */
    @Post
    @Status(HttpStatus.CREATED)
    public Account createAccount(@Body final Account account) {
        return accountService.createAccount(account);
    }

    /**
     * Create deposit operation
     * @param id id of target account
     * @param deposit operation data
     */
    @Post("/{id}/deposit")
    @Status(HttpStatus.OK)
    public void deposit(@QueryValue("id") final String id, @Body final Deposit deposit) {
        accountService.createDepositOperation(id, deposit);
    }

    /**
     * Create withdraw operation
     * @param id id of target account
     * @param withdrawal operation data
     */
    @Post("/{id}/withdraw")
    @Status(HttpStatus.OK)
    public void withdraw(@QueryValue("id") final String id, @Body final Withdrawal withdrawal) {
        accountService.createWithdrawalOperation(id, withdrawal);
    }

    /**
     * Create transfer operation
     * @param transfer operation data
     */
    @Post("/{sourceAccountId}/transfer/{targetAccountId}")
    @Status(HttpStatus.OK)
    public void transfer(@QueryValue("sourceAccountId") final String sourceAccountId,
                         @QueryValue("targetAccountId") final String targetAccountId,
                         @Body final Transfer transfer) {
        accountService.createTransferOperation(sourceAccountId, targetAccountId, transfer);
    }

    @Error(exception = AccountNotFoundException.class)
    public <T, U> HttpResponse<T> onAccountNotFound(HttpRequest<U> request, AccountNotFoundException ex) {
        return HttpResponse.notFound();
    }

    @Error(exception = InsufficientAmountOnBalanceException.class)
    public <T, U> HttpResponse<T> onInvalidAmount(HttpRequest<U> request, AccountNotFoundException ex) {
        return HttpResponse.status(HttpStatus.FORBIDDEN);
    }
}
