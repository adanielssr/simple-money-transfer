package test.adanielssr.simple.money.transfer.business.service.exceptions;

/**
 * Created by arodrigues on 13/08/2017.
 */
public class AccountAlreadyExistsException extends ResourceAlreadyExistsException {

    public AccountAlreadyExistsException(Long accountNumber) {
        super("Account", "number", accountNumber);
    }
}
