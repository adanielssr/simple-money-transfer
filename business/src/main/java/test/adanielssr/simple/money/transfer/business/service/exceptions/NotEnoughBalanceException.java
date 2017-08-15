package test.adanielssr.simple.money.transfer.business.service.exceptions;

import java.text.MessageFormat;

/**
 * Created by arodrigues on 15/08/2017.
 */
public class NotEnoughBalanceException extends SimpleMoneyTransferException {

    public NotEnoughBalanceException(Long accountNumber) {
        super(MessageFormat.format("Account with number {1} doesn\'t have enough balance to perform this operation!",
                accountNumber));
    }
}
