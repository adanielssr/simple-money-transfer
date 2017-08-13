package test.adanielssr.simple.money.transfer.business.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;

import test.adanielssr.simple.money.transfer.business.service.exceptions.SimpleMoneyTransferException;
import test.adanielssr.simple.money.transfer.domain.model.Account;
import test.adanielssr.simple.money.transfer.domain.model.Transfer;
import test.adanielssr.simple.money.transfer.domain.model.TransferStatus;

/**
 * Created by arodrigues on 13/08/2017.
 */
public class TransferService {

    private static final int DEFAULT_SCALE = 2;

    private final ConcurrentMap<Long, Transfer> mapTransferNumberToTransfer = new ConcurrentHashMap<>();

    private final AtomicLong transferNumberIncrementer = new AtomicLong();

    private final AccountService accountService;

    public TransferService(AccountService accountService){
        this.accountService = accountService;
    }

    /**
     * Create a Transfer and performs it in the correspondent accounts.
     *
     * @param transfer the transfer
     * @return the created transfer
     * @throws SimpleMoneyTransferException if the input transfer is not valid
     * @throws test.adanielssr.simple.money.transfer.business.service.exceptions.AccountNotFoundException if some of the given accountNumber does not exist
     */
    public Transfer createAndPerformTransfer(Transfer transfer) {
        validateTransfer(transfer);

        //initialise transfer
        transfer.setTransferNumber(transferNumberIncrementer.incrementAndGet());
        transfer.setStatus(TransferStatus.REGISTERED);

        //stores transfer on a registered state
        mapTransferNumberToTransfer.putIfAbsent(transfer.getTransferNumber(), transfer);

        // get accounts from account Service
        Account transferFrom = accountService.getAccountByNumber(transfer.getAccountNumberFrom());

        Account transferTo = accountService.getAccountByNumber(transfer.getAccountNumberTo());

        final BigDecimal transferAmount = new BigDecimal(transfer.getAmount());

        updateFromAccount(transferFrom, transferAmount);

        updateToAccount(transferTo, transferAmount);

        //update transfer to a performed state
        mapTransferNumberToTransfer.compute(transfer.getTransferNumber(), (transferNumber, foundTransfer) -> {
            foundTransfer.setStatus(TransferStatus.PERFORMED);
            return foundTransfer;
        });

        return transfer;
    }

    private void updateToAccount(Account transferTo, BigDecimal transferAmount) {
        BiFunction<Long, Account, Account> addOperation = (aAccountNumber, existing) -> {
            BigDecimal currentFromBalance = new BigDecimal(existing.getBalance());
            BigDecimal fromAccountResult = currentFromBalance.add(transferAmount).setScale(DEFAULT_SCALE,
                    RoundingMode.HALF_UP);
            existing.setBalance(fromAccountResult.doubleValue());

            return existing;
        };

        accountService.performAccountOperation(transferTo.getAccountNumber(), addOperation);
    }

    private void updateFromAccount(Account transferFrom, BigDecimal transferAmount) {
        BiFunction<Long, Account, Account> substractOperation = (aAccountNumber, existing) -> {
            BigDecimal currentFromBalance = new BigDecimal(existing.getBalance());
            BigDecimal fromAccountResult = currentFromBalance.subtract(transferAmount).setScale(DEFAULT_SCALE,
                    RoundingMode.HALF_UP);

            existing.setBalance(fromAccountResult.doubleValue());

            return existing;
        };
        accountService.performAccountOperation(transferFrom.getAccountNumber(), substractOperation);
    }

    private void validateTransfer(Transfer transfer) {
        if (transfer == null) {
            throw new SimpleMoneyTransferException("Transfer object needed!");
        }
        if (transfer.getAccountNumberFrom() == null) {
            throw new SimpleMoneyTransferException("accountNumberFrom needed!");
        }
        if (transfer.getAccountNumberTo() == null) {
            throw new SimpleMoneyTransferException("accountNumberTo needed!");
        }
        if (transfer.getAmount() == null) {
            throw new SimpleMoneyTransferException("amount needed!");
        }
    }
}
