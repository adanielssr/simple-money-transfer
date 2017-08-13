package test.adanielssr.simple.money.transfer.business.service.exceptions;

/**
 * Created by arodrigues on 13/08/2017.
 */
public class SimpleMoneyTransferException extends RuntimeException {
    public SimpleMoneyTransferException(String reason) {
        super(reason);
    }
}
