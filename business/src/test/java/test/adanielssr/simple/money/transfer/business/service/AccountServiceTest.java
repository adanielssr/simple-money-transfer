package test.adanielssr.simple.money.transfer.business.service;

import org.junit.Before;
import org.junit.Test;

import test.adanielssr.simple.money.transfer.business.service.exceptions.AccountAlreadyExistsException;
import test.adanielssr.simple.money.transfer.business.service.exceptions.SimpleMoneyTransferException;
import test.adanielssr.simple.money.transfer.domain.model.Account;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
}
