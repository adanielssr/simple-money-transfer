package test.adanielssr.simple.money.transfer.business.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;

import test.adanielssr.simple.money.transfer.business.service.exceptions.NotEnoughBalanceException;
import test.adanielssr.simple.money.transfer.business.service.exceptions.SimpleMoneyTransferException;
import test.adanielssr.simple.money.transfer.business.service.exceptions.TransferValidationException;
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

    public TransferService(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * Access modifier to default for testing purposes
     *
     * @return the map of Transfers
     */
    ConcurrentMap<Long, Transfer> getMapTransferNumberToTransfer() {
        return mapTransferNumberToTransfer;
    }

    /**
     * Create a Transfer and performs it in the correspondent accounts.
     *
     * @param transfer the transfer
     * @return the created transfer
     * @throws SimpleMoneyTransferException                                                               if the input transfer is not valid
     * @throws NotEnoughBalanceException                                                                  if the account from what this transfer is supposed to come from doesn't have enough balance to make it.
     * @throws test.adanielssr.simple.money.transfer.business.service.exceptions.AccountNotFoundException if some of the given accountNumber does not exist
     */
    public Transfer createAndPerformTransfer(Transfer transfer) {
        validateTransfer(transfer);

        // get accounts from account Service
        Account transferFrom = accountService.getAccountByNumber(transfer.getAccountNumberFrom());

        Account transferTo = accountService.getAccountByNumber(transfer.getAccountNumberTo());

        //initialise transfer
        transfer.setTransferNumber(transferNumberIncrementer.incrementAndGet());
        transfer.setStatus(TransferStatus.REGISTERED);
        transfer.setTransferTimestamp(new Date());

        //stores transfer on a registered state
        mapTransferNumberToTransfer.putIfAbsent(transfer.getTransferNumber(), transfer);

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
            BigDecimal fromAccountResult = currentFromBalance.add(transferAmount)
                    .setScale(DEFAULT_SCALE, RoundingMode.HALF_UP);
            existing.setBalance(fromAccountResult.doubleValue());

            return existing;
        };

        accountService.performAccountOperation(transferTo.getAccountNumber(), addOperation);
    }

    private void updateFromAccount(Account transferFrom, BigDecimal transferAmount) {
        BiFunction<Long, Account, Account> subtractOperation = (aAccountNumber, existing) -> {
            BigDecimal currentFromBalance = new BigDecimal(existing.getBalance());
            BigDecimal fromAccountResult = currentFromBalance.subtract(transferAmount)
                    .setScale(DEFAULT_SCALE, RoundingMode.HALF_UP);

            if (fromAccountResult.doubleValue() < 0.0D) {
                throw new NotEnoughBalanceException(aAccountNumber);
            }

            existing.setBalance(fromAccountResult.doubleValue());

            return existing;
        };
        accountService.performAccountOperation(transferFrom.getAccountNumber(), subtractOperation);
    }

    private void validateTransfer(Transfer transfer) {
        if (transfer == null) {
            throw new SimpleMoneyTransferException("Transfer object needed!");
        }
        if (transfer.getAccountNumberFrom() == null) {
            throw new TransferValidationException("accountNumberFrom needed!");
        }
        if (transfer.getAccountNumberTo() == null) {
            throw new TransferValidationException("accountNumberTo needed!");
        }
        if (transfer.getAmount() == null) {
            throw new TransferValidationException("amount needed!");
        }

        if (transfer.getAccountNumberFrom().longValue() == transfer.getAccountNumberTo().longValue()) {
            throw new TransferValidationException("Cannot perform transfer in the same account!");
        }
        if (transfer.getAmount() <= 0.0D) {
            throw new TransferValidationException("amount must be greater than 0.0!");
        }
    }
}
