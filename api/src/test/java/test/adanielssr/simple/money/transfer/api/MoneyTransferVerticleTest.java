package test.adanielssr.simple.money.transfer.api;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import test.adanielssr.simple.money.transfer.domain.model.Account;
import test.adanielssr.simple.money.transfer.domain.model.Transfer;
import test.adanielssr.simple.money.transfer.domain.model.TransferStatus;

import static io.vertx.core.http.HttpHeaders.CONTENT_LENGTH;
import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(VertxUnitRunner.class)
public class MoneyTransferVerticleTest {

    public static final String LOCALHOST = "localhost";

    public static final String APPLICATION_JSON_CHARSET_UTF_8 = "application/json; charset=utf-8";

    public static final int TIMEOUT = 2;

    public static final String EMPTY_ACCOUNT = "{}";

    public static final String ACCOUNT_WITH_100 = "{\"balance\": 100}";

    public static final String ACCOUNT_WITH_10 = "{\"balance\": 10}";

    private static Vertx vertx;

    private static int port;

    private HttpClient httpClient;

    @BeforeClass
    public static void setUpVertx() throws IOException {
        ServerSocket socket = new ServerSocket(0);
        port = socket.getLocalPort();
        socket.close();

        vertx = Vertx.vertx();
    }

    @AfterClass
    public static void closeVertx() {
        vertx.close();
    }

    @Before
    public void setUp(TestContext context) {
        DeploymentOptions options = new DeploymentOptions().setConfig(new JsonObject().put("http.port", port));
        vertx.deployVerticle(MoneyTransferVerticle.class.getName(), options, context.asyncAssertSuccess());
        httpClient = vertx.createHttpClient();
    }

    @After
    public void tearDown(TestContext context) {
        for (String deploymentId : vertx.deploymentIDs()) {
            vertx.undeploy(deploymentId, context.asyncAssertSuccess());
        }
    }

    @Test
    public void testCreateAccount(TestContext context) throws InterruptedException {
        assertAccountsSize(httpClient, 0);

        Account accountCreated = postCreateAccount(httpClient, EMPTY_ACCOUNT);
        assertNotNull(accountCreated);
        assertNotNull(accountCreated.getAccountNumber());
        assertEquals((Double) 0.0D, accountCreated.getBalance());

        assertAccountsSize(httpClient, 1);

        Account secondAccountCreated = postCreateAccount(httpClient, ACCOUNT_WITH_100);
        assertNotNull(secondAccountCreated);
        assertNotNull(secondAccountCreated.getAccountNumber());
        assertEquals((Double) 100.0D, secondAccountCreated.getBalance());

        assertAccountsSize(httpClient, 2);
    }

    @Test
    public void testCreateAccountWithExistentAccountNumber(TestContext context) throws InterruptedException {
        Account acountCreated = postCreateAccount(httpClient, EMPTY_ACCOUNT);

        Account newAccount = new Account();
        newAccount.setAccountNumber(acountCreated.getAccountNumber());

        postCreateAccount(httpClient, HttpResponseStatus.CONFLICT.code(), Json.encode(newAccount));
    }

    @Test
    public void testCreateAccountAndRetrieveAccount(TestContext context) throws InterruptedException {
        Account accountCreated = postCreateAccount(httpClient, EMPTY_ACCOUNT);
        assertNotNull(accountCreated);
        assertNotNull(accountCreated.getAccountNumber());
        assertEquals((Double) 0.0D, accountCreated.getBalance());

        Account retrivedAccount = getAccountByAccountNumber(httpClient, HttpResponseStatus.OK.code(),
                accountCreated.getAccountNumber().toString());
        assertEquals((Double) 0.0D, retrivedAccount.getBalance());
    }

    @Test
    public void testGetAccountByAccountNumberWithInvalidAccountNumber(TestContext context) throws InterruptedException {
        getAccountByAccountNumber(httpClient, HttpResponseStatus.BAD_REQUEST.code(), "asfsa");
    }

    @Test
    public void testGetAccountByAccountNumberWithNonexistentAccount(TestContext context) throws InterruptedException {
        getAccountByAccountNumber(httpClient, HttpResponseStatus.NOT_FOUND.code(), "1");
    }

    @Test
    public void testGetAccountByAccountNumberWithExistentAccount(TestContext context) throws InterruptedException {
        Account accountCreated = postCreateAccount(httpClient, EMPTY_ACCOUNT);

        getAccountByAccountNumber(httpClient, HttpResponseStatus.OK.code(),
                accountCreated.getAccountNumber().toString());
    }

    @Test
    public void testMakeTransferInvalidAccountNumber(TestContext context) throws InterruptedException {
        postCreateTransfer(httpClient, HttpResponseStatus.BAD_REQUEST.code(), "asda", "{}");
    }

    @Test
    public void testMakeTransferNoAccountTo(TestContext context) throws InterruptedException {
        postCreateTransfer(httpClient, HttpResponseStatus.BAD_REQUEST.code(), "1", "{}");
    }

    @Test
    public void testMakeTransferNoAmount(TestContext context) throws InterruptedException {
        Transfer transfer = new Transfer();
        transfer.setAccountNumberTo(2L);

        postCreateTransfer(httpClient, HttpResponseStatus.BAD_REQUEST.code(), "1", Json.encode(transfer));
    }

    @Test
    public void testMakeTransferSameAccount(TestContext context) throws InterruptedException {
        Transfer transfer = new Transfer();
        transfer.setAccountNumberTo(1L);
        transfer.setAmount(10.0D);

        postCreateTransfer(httpClient, HttpResponseStatus.BAD_REQUEST.code(), "1", Json.encode(transfer));
    }

    @Test
    public void testMakeTransferNegativeAmount(TestContext context) throws InterruptedException {
        Transfer transfer = new Transfer();
        transfer.setAccountNumberTo(2L);
        transfer.setAmount(-10.0D);

        postCreateTransfer(httpClient, HttpResponseStatus.BAD_REQUEST.code(), "1", Json.encode(transfer));
    }

    @Test
    public void testMakeTransferWithNonexistentAccount(TestContext context) throws InterruptedException {
        Transfer transfer = new Transfer();
        transfer.setAccountNumberTo(2L);
        transfer.setAmount(10.0D);

        postCreateTransfer(httpClient, HttpResponseStatus.NOT_FOUND.code(), "1", Json.encode(transfer));
    }

    @Test
    public void testMakeTransferWithNonexistentAccountTo(TestContext context) throws InterruptedException {
        Account accountCreated = postCreateAccount(httpClient, EMPTY_ACCOUNT);
        Transfer transfer = new Transfer();
        transfer.setAccountNumberTo(2L);
        transfer.setAmount(10.0D);

        postCreateTransfer(httpClient, HttpResponseStatus.NOT_FOUND.code(),
                accountCreated.getAccountNumber().toString(), Json.encode(transfer));
    }

    @Test
    public void testMakeTransferWithSameAccountOnBody(TestContext context) throws InterruptedException {
        Account accountCreated = postCreateAccount(httpClient, EMPTY_ACCOUNT);
        Transfer transfer = new Transfer();
        transfer.setAccountNumberFrom(2L);
        transfer.setAccountNumberTo(2L);
        transfer.setAmount(10.0D);

        postCreateTransfer(httpClient, HttpResponseStatus.NOT_FOUND.code(),
                accountCreated.getAccountNumber().toString(), Json.encode(transfer));
    }

    @Test
    public void testMakeTransferWithAmountBiggerThanAccountBalance(TestContext context) throws InterruptedException {
        Account accountCreated = postCreateAccount(httpClient, ACCOUNT_WITH_10);
        Account secondAccountCreated = postCreateAccount(httpClient, ACCOUNT_WITH_10);

        Transfer transfer = new Transfer();
        transfer.setAccountNumberTo(secondAccountCreated.getAccountNumber());
        transfer.setAmount(20.0D);

        postCreateTransfer(httpClient, HttpResponseStatus.CONFLICT.code(), accountCreated.getAccountNumber().toString(),
                Json.encode(transfer));
    }

    @Test
    public void testMakeTransferWithSuccessfullyAmount(TestContext context) throws InterruptedException {
        Account accountCreated = postCreateAccount(httpClient, ACCOUNT_WITH_10);
        Account secondAccountCreated = postCreateAccount(httpClient, ACCOUNT_WITH_10);

        Transfer transfer = new Transfer();
        transfer.setAccountNumberTo(secondAccountCreated.getAccountNumber());
        transfer.setAmount(10.004D);

        Transfer createdTransfer = postCreateTransfer(httpClient, HttpResponseStatus.CREATED.code(),
                accountCreated.getAccountNumber().toString(), Json.encode(transfer));
        assertNotNull(createdTransfer);
        assertNotNull(createdTransfer.getTransferNumber());
        assertNotNull(createdTransfer.getTransferTimestamp());
        assertEquals(accountCreated.getAccountNumber(), createdTransfer.getAccountNumberFrom());
        assertEquals(secondAccountCreated.getAccountNumber(), createdTransfer.getAccountNumberTo());
        assertEquals(TransferStatus.PERFORMED, createdTransfer.getStatus());
    }

    @Test
    public void testMakeTransferWithHalfUpRound(TestContext context) throws InterruptedException {
        Account accountCreated = postCreateAccount(httpClient, ACCOUNT_WITH_10);
        Account secondAccountCreated = postCreateAccount(httpClient, ACCOUNT_WITH_10);

        Transfer transfer = new Transfer();
        transfer.setAccountNumberTo(secondAccountCreated.getAccountNumber());
        transfer.setAmount(5.004D);

        postCreateTransfer(httpClient, HttpResponseStatus.CREATED.code(), accountCreated.getAccountNumber().toString(),
                Json.encode(transfer));

        postCreateTransfer(httpClient, HttpResponseStatus.CREATED.code(), accountCreated.getAccountNumber().toString(),
                Json.encode(transfer));

        Account accountAfterTransfer = getAccountByAccountNumber(httpClient, HttpResponseStatus.OK.code(),
                accountCreated.getAccountNumber().toString());
        assertNotNull(accountAfterTransfer);
        assertEquals((Double) 0.0D, accountAfterTransfer.getBalance());

        Account secondAccountCreatedAfterTransfer = getAccountByAccountNumber(httpClient, HttpResponseStatus.OK.code(),
                secondAccountCreated.getAccountNumber().toString());
        assertNotNull(secondAccountCreatedAfterTransfer);
        assertEquals((Double) 20.0D, secondAccountCreatedAfterTransfer.getBalance());
    }

    private Transfer postCreateTransfer(HttpClient httpClient, int expectedStatus, String accountNumber,
            String transferBody) throws InterruptedException {
        final CountDownLatch postLatch = new CountDownLatch(1);

        final Transfer[] retrievedAccount = { null };
        AtomicInteger status = new AtomicInteger();

        httpClient.post(port, LOCALHOST, "/accounts/" + accountNumber + "/transfers").handler(response -> {
            status.set(response.statusCode());

            if (HttpResponseStatus.CREATED.code() == expectedStatus) {
                response.handler(body -> {
                    Transfer transfer = Json.decodeValue(body.toString(), Transfer.class);

                    retrievedAccount[0] = transfer;

                    postLatch.countDown();
                });
            } else {
                postLatch.countDown();
            }

        }).putHeader(CONTENT_LENGTH, transferBody.length() + "").putHeader(CONTENT_TYPE, APPLICATION_JSON_CHARSET_UTF_8)
                .write(transferBody).end();

        waitForLatch(postLatch);

        assertEquals(expectedStatus, status.get());

        return retrievedAccount[0];
    }

    private Account getAccountByAccountNumber(HttpClient httpClient, int expectedStatus, String accountNumber)
            throws InterruptedException {
        CountDownLatch assertLatch = new CountDownLatch(1);

        final Account[] retrievedAccount = { null };
        AtomicInteger status = new AtomicInteger();

        httpClient.getNow(port, LOCALHOST, "/accounts/" + accountNumber, response -> {
            status.set(response.statusCode());

            if (HttpResponseStatus.OK.code() == expectedStatus) {
                response.handler(body -> {
                    Account account = Json.decodeValue(body.toString(), Account.class);
                    assertNotNull(account);
                    assertEquals(new Long(accountNumber), account.getAccountNumber());

                    retrievedAccount[0] = account;

                    assertLatch.countDown();
                });
            } else {
                assertLatch.countDown();
            }
        });

        waitForLatch(assertLatch);

        assertEquals(expectedStatus, status.get());

        return retrievedAccount[0];
    }

    private Account postCreateAccount(HttpClient httpClient, String accountBody) throws InterruptedException {
        return postCreateAccount(httpClient, HttpResponseStatus.CREATED.code(), accountBody);
    }

    private Account postCreateAccount(HttpClient httpClient, int expectedStatus, String accountBody)
            throws InterruptedException {
        final CountDownLatch postLatch = new CountDownLatch(1);

        final Account[] retrievedAccount = { null };
        AtomicInteger status = new AtomicInteger();

        httpClient.post(port, LOCALHOST, "/accounts").handler(response -> {
            status.set(response.statusCode());
            if (HttpResponseStatus.CREATED.code() == expectedStatus) {
                response.handler(body -> {
                    Account account = Json.decodeValue(body.toString(), Account.class);

                    retrievedAccount[0] = account;

                    postLatch.countDown();
                });
            } else {
                postLatch.countDown();
            }
        }).putHeader(CONTENT_LENGTH, accountBody.length() + "").putHeader(CONTENT_TYPE, APPLICATION_JSON_CHARSET_UTF_8)
                .write(accountBody).end();

        waitForLatch(postLatch);

        assertEquals(expectedStatus, status.get());

        return retrievedAccount[0];
    }

    private static void assertAccountsSize(HttpClient httpClient, int size) throws InterruptedException {
        CountDownLatch assertLatch = new CountDownLatch(1);
        AtomicInteger status = new AtomicInteger();

        httpClient.getNow(port, LOCALHOST, "/accounts", response -> {
            status.set(response.statusCode());

            if (HttpResponseStatus.OK.code() == response.statusCode()) {
                response.handler(body -> {
                    List<Account> accounts = Json.decodeValue(body.toString(), List.class);
                    assertNotNull(accounts);
                    assertEquals(size, accounts.size());

                    assertLatch.countDown();
                });
            } else {
                assertLatch.countDown();
            }
        });

        waitForLatch(assertLatch);

        assertEquals(HttpResponseStatus.OK.code(), status.get());
    }

    private static void waitForLatch(CountDownLatch assertLatch) throws InterruptedException {
        assertLatch.await(TIMEOUT, TimeUnit.SECONDS);
        assertEquals(0, assertLatch.getCount());
    }
}