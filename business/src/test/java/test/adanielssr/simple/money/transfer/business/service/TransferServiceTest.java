package test.adanielssr.simple.money.transfer.business.service;

import org.junit.Before;

import static org.mockito.Mockito.mock;

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

    
}
