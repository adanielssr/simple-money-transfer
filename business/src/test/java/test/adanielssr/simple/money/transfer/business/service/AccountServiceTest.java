package test.adanielssr.simple.money.transfer.business.service;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;

import test.adanielssr.simple.money.transfer.business.service.exceptions.AccountAlreadyExistsException;
import test.adanielssr.simple.money.transfer.business.service.exceptions.AccountNotFoundException;
import test.adanielssr.simple.money.transfer.business.service.exceptions.SimpleMoneyTransferException;
import test.adanielssr.simple.money.transfer.domain.model.Account;

import static org.junit.Assert.*;

/**
 * Created by arodrigues on 13/08/2017.
 */
public class AccountServiceTest {

    private AccountService accountService;

    @Before
    public void setup() {
        accountService = new AccountService();
    }

    @Test(expected = SimpleMoneyTransferException.class)
    public void testCreateAccountWithNullAccount() {
        accountService.createAccount(null);
    }

    @Test
    public void testCreateAccountWithEmptyAccount() {
        Account newAccount = accountService.createAccount(new Account());

        assertNotNull(newAccount.getAccountNumber());
        assertNotNull(newAccount.getBalance());
    }

    @Test
    public void testCreateAccountWithAccountNumberAndBalance() {
        Account givenAccount = new Account();
        givenAccount.setAccountNumber(1L);
        givenAccount.setBalance(100.0D);

        Account newAccount = accountService.createAccount(givenAccount);

        assertEquals(1L, newAccount.getAccountNumber().longValue());
        assertEquals((Double) 100.0D, newAccount.getBalance());
    }

    @Test(expected = AccountAlreadyExistsException.class)
    public void testCreateAccountWithExistingAccount() {
        Account newAccount = new Account();
        accountService.createAccount(newAccount);

        accountService.createAccount(newAccount);
    }

    @Test
    public void testRetrieveAllAccounts() {
        assertNotNull(accountService.getAllAccounts());
        assertTrue(accountService.getAllAccounts().isEmpty());

        Account newAccount = new Account();
        accountService.createAccount(newAccount);
        assertEquals(1, accountService.getAllAccounts().size());
        assertEquals(newAccount, accountService.getAllAccounts().iterator().next());

        accountService.createAccount(new Account());
        assertEquals(2, accountService.getAllAccounts().size());
    }

    @Test(expected = SimpleMoneyTransferException.class)
    public void testGetAccountByNumberWithNullNumber() {
        accountService.getAccountByNumber(null);
    }

    @Test(expected = AccountNotFoundException.class)
    public void testGetAccountByNumberWithNonexistentAccount() {
        accountService.getAccountByNumber(1L);
    }

    @Test
    public void testGetAccountByNumberSuccess() {
        Account newAccount = new Account();
        accountService.createAccount(newAccount);

        Account newAccountRetrieved = accountService.getAccountByNumber(newAccount.getAccountNumber());
        assertEquals(newAccount, newAccountRetrieved);
    }

    @Test(expected = SimpleMoneyTransferException.class)
    public void testPerformOperationWithNullAccountNumber() {
        accountService.performAccountOperation(null, null);
    }

    @Test(expected = SimpleMoneyTransferException.class)
    public void testPerformOperationWithNullOperation() {
        accountService.performAccountOperation(1L, null);
    }

    @Test
    public void testPerformOperationWithNonexistentAccountNumber() {
        final AtomicInteger atomicInteger = new AtomicInteger(0);

        accountService.performAccountOperation(1L, (aNumber, aAccount) -> {
            atomicInteger.incrementAndGet();
            return aAccount;
        });
        assertEquals(0, atomicInteger.get());
    }

    @Test
    public void testPerformOperationWithExistentAccountNumber() {
        final AtomicInteger atomicInteger = new AtomicInteger(0);
        final Account account = new Account();
        account.setAccountNumber(1L);

        accountService.createAccount(account);

        accountService.performAccountOperation(account.getAccountNumber(), (aNumber, aAccount) -> {
            atomicInteger.incrementAndGet();
            assertEquals(aAccount, account);
            return aAccount;
        });
        assertEquals(1, atomicInteger.get());
    }
}
