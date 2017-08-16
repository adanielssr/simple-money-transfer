package test.adanielssr.simple.money.transfer.business.service.exceptions;

import java.text.MessageFormat;

/**
 * Created by arodrigues on 13/08/2017.
 */
public class ResourceNotFoundException extends SimpleMoneyTransferException {

    public ResourceNotFoundException(String resourceName, String resourceParameterName, Object resource) {
        super(MessageFormat
                .format("{1} with {2} equal to {1} does not exist!", resourceName, resourceParameterName, resource));
    }
}
