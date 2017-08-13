package test.adanielssr.simple.money.transfer.business.service.exceptions;

/**
 * Created by arodrigues on 14/08/2017.
 */
public class TransferValidationException extends SimpleMoneyTransferException {

    public TransferValidationException(String reason) {
        super(reason);
    }
}
