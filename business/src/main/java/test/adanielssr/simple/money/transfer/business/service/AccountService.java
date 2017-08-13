package test.adanielssr.simple.money.transfer.business.service;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;

import test.adanielssr.simple.money.transfer.business.service.exceptions.AccountAlreadyExistsException;
import test.adanielssr.simple.money.transfer.business.service.exceptions.AccountNotFoundException;
import test.adanielssr.simple.money.transfer.business.service.exceptions.SimpleMoneyTransferException;
import test.adanielssr.simple.money.transfer.domain.model.Account;

/**
 * Created by arodrigues on 13/08/2017.
 */
public class AccountService {

    private final ConcurrentMap<Long, Account> mapAccountNumberToAccount = new ConcurrentHashMap<>();

    private final AtomicLong accountNumberIncrementer = new AtomicLong();

    /**
     * Creates a new Account and stores it.
     * Sets the account Balance to 0.0 if there is no given account balance.
     * Generates a new account number if no account number is given
     *
     * @param newAccount account with the optional account number and account balance
     * @return created account
     * @throws SimpleMoneyTransferException if the account parameter is not valid
     * @throws AccountAlreadyExistsException if the account number given is already assigned to an existing account
     */
    public Account createAccount(Account newAccount) {
        if (newAccount == null) {
            throw new SimpleMoneyTransferException("Account object needed!");
        }

        if (newAccount.getAccountNumber() == null) {
            newAccount.setAccountNumber(accountNumberIncrementer.incrementAndGet());
        }
        if (newAccount.getBalance() == null) {
            newAccount.setBalance(0.0D);
        }

        if (mapAccountNumberToAccount.putIfAbsent(newAccount.getAccountNumber(), newAccount) != null) {
            throw new AccountAlreadyExistsException(newAccount.getAccountNumber());
        } else {
            return newAccount;
        }
    }

    /**
     * Retrieve all stored accounts
     * @return the collection of Accounts
     */
    public Collection<Account> getAllAccounts() {
        return mapAccountNumberToAccount.values();
    }

    /**
     * Retrieves an account by its account number.
     *
     * @param accountNumber the account number of the account to search for
     * @return the found account
     * @throws SimpleMoneyTransferException if the account number is invalid
     * @throws AccountNotFoundException if no account with the given account number exists in the store
     */
    public Account getAccountByNumber(Long accountNumber) {
        if (accountNumber == null) {
            throw new SimpleMoneyTransferException("Account number needed!");
        }
        Account foundAccount = mapAccountNumberToAccount.get(accountNumber);
        if (foundAccount == null) {
            throw new AccountNotFoundException(accountNumber);
        } else {
            return foundAccount;
        }
    }

    /**
     * Performs a operation over an account if it exists on the store.
     * The operation is done if the acconu
     *
     * @param accountNumber the account number
     * @param operation function where the first argument is the account Number the second argument is the
     */
    public void performAccountOperation(Long accountNumber, BiFunction<Long, Account, Account> operation) {
        if (accountNumber == null) {
            throw new SimpleMoneyTransferException("Account number needed!");
        }
        mapAccountNumberToAccount.compute(accountNumber, operation);
    }
}
