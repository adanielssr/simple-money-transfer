package test.adanielssr.simple.money.transfer.business.service.exceptions;

import java.text.MessageFormat;

/**
 * Created by arodrigues on 13/08/2017.
 */
public class AccountNotFoundExceptionSimple extends SimpleMoneyTransferException {

    public AccountNotFoundExceptionSimple(Long accountNumber) {
        super(MessageFormat.format("Account with number {1} does not exists!", accountNumber));
    }
}
