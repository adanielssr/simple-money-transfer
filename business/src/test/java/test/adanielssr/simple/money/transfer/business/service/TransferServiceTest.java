package test.adanielssr.simple.money.transfer.business.service;

import java.util.function.BiFunction;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import test.adanielssr.simple.money.transfer.business.service.exceptions.AccountNotFoundException;
import test.adanielssr.simple.money.transfer.business.service.exceptions.SimpleMoneyTransferException;
import test.adanielssr.simple.money.transfer.business.service.exceptions.TransferValidationException;
import test.adanielssr.simple.money.transfer.domain.model.Account;
import test.adanielssr.simple.money.transfer.domain.model.Transfer;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Created by arodrigues on 13/08/2017.
 */
public class TransferServiceTest {

    private AccountService accountService;

    private TransferService transferService;

    @Before
    public void setup() {
        accountService = mock(AccountService.class);
        transferService = new TransferService(accountService);
    }

    @Test(expected = SimpleMoneyTransferException.class)
    public void createAndPerformTransferWithNullTransfer() {
        transferService.createAndPerformTransfer(null);
    }

    @Test(expected = TransferValidationException.class)
    public void createAndPerformTransferWithNullFromAccount() {
        transferService.createAndPerformTransfer(new Transfer());
    }

    @Test(expected = TransferValidationException.class)
    public void createAndPerformTransferWithNullToAccount() {
        Transfer transfer = new Transfer();
        transfer.setAccountNumberFrom(1L);

        transferService.createAndPerformTransfer(transfer);
    }

    @Test(expected = TransferValidationException.class)
    public void createAndPerformTransferWithNullAmount() {
        Transfer transfer = new Transfer();
        transfer.setAccountNumberFrom(1L);
        transfer.setAccountNumberTo(2L);

        transferService.createAndPerformTransfer(transfer);
    }

    @Test(expected = TransferValidationException.class)
    public void createAndPerformTransferWithSameAccount() {
        Transfer transfer = new Transfer();
        transfer.setAccountNumberFrom(1L);
        transfer.setAccountNumberTo(1L);
        transfer.setAmount(1.0D);

        transferService.createAndPerformTransfer(transfer);
    }

    @Test(expected = TransferValidationException.class)
    public void createAndPerformTransferWithLessOrEqualToZeroAmount() {
        Transfer transfer = new Transfer();
        transfer.setAccountNumberFrom(1L);
        transfer.setAccountNumberTo(2L);
        transfer.setAmount(0.0D);

        transferService.createAndPerformTransfer(transfer);
    }

    @Test(expected = AccountNotFoundException.class)
    public void createAndPerformTransferWithFromNonexistentAccount() {
        Transfer transfer = createValidTransfer();

        when(accountService.getAccountByNumber(eq(1L))).thenThrow(AccountNotFoundException.class);

        transferService.createAndPerformTransfer(transfer);
    }

    @Test(expected = AccountNotFoundException.class)
    public void createAndPerformTransferWithToNonexistentAccount() {
        Transfer transfer = createValidTransfer();

        when(accountService.getAccountByNumber(eq(2L))).thenThrow(AccountNotFoundException.class);

        transferService.createAndPerformTransfer(transfer);
    }

    @Test
    public void createAndPerformTransfer() {
        ArgumentCaptor<BiFunction> operationCaptor = ArgumentCaptor.forClass(BiFunction.class);
        ArgumentCaptor<Long> accountNumberCaptor = ArgumentCaptor.forClass(Long.class);

        Transfer transfer = createValidTransfer();
        transfer.setAmount(10.0D);

        Account accountFrom = createAccountFrom();

        Account accountTo = createAccountTo();

        when(accountService.getAccountByNumber(anyLong())).thenReturn(accountFrom, accountTo);

        doNothing().when(accountService)
                .performAccountOperation(accountNumberCaptor.capture(), operationCaptor.capture());

        transferService.createAndPerformTransfer(transfer);

        assertEquals(2, accountNumberCaptor.getAllValues().size());
        assertEquals(2, operationCaptor.getAllValues().size());

        assertEquals((Long) 1L, accountNumberCaptor.getAllValues().get(0));
        assertEquals((Long) 2L, accountNumberCaptor.getAllValues().get(1));

        BiFunction firstAccountOperation = operationCaptor.getAllValues().get(0);

        Account resultAccountOne = (Account) firstAccountOperation.apply(accountFrom.getAccountNumber(), createAccountFrom());

        assertEquals((Double)0.0D, resultAccountOne.getBalance());

        BiFunction secondAccountOperation = operationCaptor.getAllValues().get(1);

        Account resultAccountTwo = (Account) secondAccountOperation.apply(accountTo.getAccountNumber(), createAccountTo());
        assertEquals((Double)10.0D, resultAccountTwo.getBalance());
    }

    private Account createAccountFrom() {
        Account accountFrom = new Account();
        accountFrom.setAccountNumber(1L);
        accountFrom.setBalance(10.0D);
        return accountFrom;
    }


    private Account createAccountTo() {
        Account accountTo = new Account();
        accountTo.setAccountNumber(2L);
        accountTo.setBalance(0.0D);
        return accountTo;
    }

    private Transfer createValidTransfer() {
        Transfer transfer = new Transfer();
        transfer.setAccountNumberFrom(1L);
        transfer.setAccountNumberTo(2L);
        transfer.setAmount(1.0D);
        return transfer;
    }
}
