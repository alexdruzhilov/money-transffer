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
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.http.uri.UriBuilder;
import io.micronaut.test.annotation.MicronautTest;
import io.micronaut.test.annotation.MockBean;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.inject.Inject;
import javax.validation.ConstraintViolationException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@MicronautTest
public class AccountControllerTest {
    @Inject
    AccountService accountService;

    @Inject
    @Client("/account")
    private HttpClient client;

    @MockBean(AccountService.class)
    AccountService accountService() {
        return Mockito.mock(AccountService.class);
    }

    @Test
    void createAccount_withValidData_respond200() {
        final Account validAccount = Account.builder()
                .id("cindyId")
                .name("cindy")
                .build();

        when(accountService.createAccount(validAccount))
                .thenReturn(validAccount);

        final HttpResponse<Account> response = createAccount(validAccount);
        assertEquals(HttpResponseStatus.CREATED.code(), response.code());

        final Account responseAccount = response.body();
        assertNotNull(responseAccount);
        assertNotNull(responseAccount.getId());
        assertEquals(validAccount.getName(), responseAccount.getName());
        assertEquals(0, responseAccount.getBalance());
    }

    @Test
    void createAccount_withoutName_respond400() {
        final Account invalidAccount = Account.builder()
                .build();

        when(accountService.createAccount(invalidAccount))
                .thenThrow(new ConstraintViolationException(new HashSet<>()));

        final HttpResponse<Account> response = createAccount(invalidAccount);
        assertEquals(HttpResponseStatus.BAD_REQUEST.code(), response.code());
    }

    @Test
    void getAccount_byId_respond200() {
        final Account aliceAccount = Account.builder()
                .id("aliceId")
                .name("alice")
                .balance(100)
                .build();

        when(accountService.getAccountById(aliceAccount.getId()))
                .thenReturn(Optional.of(aliceAccount));

        final HttpResponse<Account> response = getAccount(aliceAccount.getId());
        assertEquals(HttpResponse.ok().code(), response.code());

        final Account account = response.body();
        assertNotNull(account);
        assertEquals(aliceAccount.getId(), account.getId());
        assertEquals(aliceAccount.getName(), account.getName());
        assertEquals(aliceAccount.getBalance(), account.getBalance());
    }

    @Test
    void getAccount_byId_respond404() {
        when(accountService.getAccountById(anyString()))
                .thenReturn(Optional.empty());

        final HttpResponse<Account> response = getAccount("randomId");
        assertEquals(HttpResponse.notFound().code(), response.code());
    }

    @Test
    void deposit_validData_200ok() {
        final Deposit deposit = new Deposit();
        deposit.setCurrency("USD");
        deposit.setAmount(10);

        doNothing().when(accountService)
                .createDepositOperation("anyId", deposit);

        final HttpResponse<Object> response = deposit("anyId", deposit);
        assertEquals(HttpResponseStatus.OK.code(), response.code());
    }

    @Test
    void deposit_accountNotFound_404() {
        final Deposit deposit = new Deposit();
        deposit.setCurrency("USD");
        deposit.setAmount(10);

        doThrow(new AccountNotFoundException("")).when(accountService)
                .createDepositOperation("anyId", deposit);

        final HttpResponse<Object> response = deposit("anyId", deposit);
        assertEquals(HttpResponseStatus.NOT_FOUND.code(), response.code());
    }

    @Test
    void withdraw_validData_200ok() {
        final Withdrawal withdrawal = new Withdrawal();
        withdrawal.setCurrency("USD");
        withdrawal.setAmount(10);

        doNothing().when(accountService)
                .createWithdrawalOperation("anyId", withdrawal);

        final HttpResponse<Object> response = withdraw("anyId", withdrawal);
        assertEquals(HttpResponseStatus.OK.code(), response.code());
    }

    @Test
    void withdraw_invalidAmount_403() {
        final Withdrawal withdrawal = new Withdrawal();
        withdrawal.setCurrency("USD");
        withdrawal.setAmount(10);

        doThrow(new InsufficientAmountOnBalanceException("")).when(accountService)
                .createWithdrawalOperation("anyId", withdrawal);

        final HttpResponse<Object> response = withdraw("anyId", withdrawal);
        assertEquals(HttpResponseStatus.FORBIDDEN.code(), response.code());
    }

    @Test
    void withdraw_accountNotFound_404() {
        final Withdrawal withdrawal = new Withdrawal();
        withdrawal.setCurrency("USD");
        withdrawal.setAmount(10);

        doThrow(new AccountNotFoundException("")).when(accountService)
                .createWithdrawalOperation("anyId", withdrawal);

        final HttpResponse<Object> response = withdraw("anyId", withdrawal);
        assertEquals(HttpResponseStatus.NOT_FOUND.code(), response.code());
    }

    @Test
    void transfer_validData_200ok() {
        final Transfer transfer = new Transfer();
        transfer.setCurrency("USD");
        transfer.setAmount(10);

        doNothing().when(accountService)
                .createTransferOperation("source", "target", transfer);

        final HttpResponse<Object> response = transfer("source", "target", transfer);
        assertEquals(HttpResponseStatus.OK.code(), response.code());
    }

    @Test
    void transfer_accountNotFound_404() {
        final Transfer transfer = new Transfer();
        transfer.setCurrency("USD");
        transfer.setAmount(10);

        doThrow(new AccountNotFoundException("")).when(accountService)
                .createTransferOperation("source", "target", transfer);

        final HttpResponse<Object> response = transfer("source", "target", transfer);
        assertEquals(HttpResponseStatus.NOT_FOUND.code(), response.code());
    }

    @Test
    void transfer_insufficientBalance_403() {
        final Transfer transfer = new Transfer();
        transfer.setCurrency("USD");
        transfer.setAmount(10);

        doThrow(new InsufficientAmountOnBalanceException("")).when(accountService)
                .createTransferOperation("source", "target", transfer);

        final HttpResponse<Object> response = transfer("source", "target", transfer);
        assertEquals(HttpResponseStatus.FORBIDDEN.code(), response.code());
    }

    private HttpResponse<Account> getAccount(final String id) {
        return exchange(HttpRequest.GET("/" + id), Account.class);
    }

    private HttpResponse<Account> createAccount(final Account account) {
        return exchange(HttpRequest.POST("/", account), Account.class);
    }

    private HttpResponse<Object> deposit(final String accountId, final Deposit deposit) {
        final String uri = UriBuilder.of("/{id}/deposit")
                .expand(Collections.singletonMap("id", accountId))
                .toString();
        return exchange(HttpRequest.POST(uri, deposit));
    }

    private HttpResponse<Object> withdraw(final String accountId, final Withdrawal withdrawal) {
        final String uri = UriBuilder.of("/{id}/withdraw")
                .expand(Collections.singletonMap("id", accountId))
                .toString();
        return exchange(HttpRequest.POST(uri, withdrawal));
    }

    private HttpResponse<Object> transfer(final String sourceId, final String targetId, final Transfer transfer) {
        final String uri = "/" + sourceId + "/transfer/" + targetId;
        return exchange(HttpRequest.POST(uri, transfer));
    }

    private <T, U> HttpResponse<U> exchange(final MutableHttpRequest<T> request) {
        try {
            return client.toBlocking().exchange(request);
        } catch (HttpClientResponseException e) {
            //noinspection unchecked
            return (HttpResponse<U>) e.getResponse();
        }
    }

    private <T, U> HttpResponse<U> exchange(final MutableHttpRequest<T> request, final Class<U> responseClass) {
        try {
            return client.toBlocking().exchange(request, responseClass);
        } catch (HttpClientResponseException e) {
            //noinspection unchecked
            return (HttpResponse<U>) e.getResponse();
        }
    }
}
