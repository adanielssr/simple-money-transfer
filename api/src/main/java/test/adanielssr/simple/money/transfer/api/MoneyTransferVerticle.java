package test.adanielssr.simple.money.transfer.api;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import test.adanielssr.simple.money.transfer.api.controller.AccountController;
import test.adanielssr.simple.money.transfer.business.service.AccountService;
import test.adanielssr.simple.money.transfer.business.service.TransferService;

public class MoneyTransferVerticle extends AbstractVerticle {

    @Override
    public void start(Future<Void> fut) {
        // Create a router object.
        Router router = Router.router(vertx);

        createRouting(router);

        vertx.createHttpServer().requestHandler(router::accept)
                .listen(config().getInteger("http.port", 8080), result -> {
                    if (result.succeeded()) {
                        fut.complete();
                    } else {
                        fut.fail(result.cause());
                    }
                });
    }

    private void createRouting(Router router) {
        AccountService accountService = new AccountService();
        AccountController accountController = new AccountController(accountService, new TransferService(accountService));

        router.get("/accounts").handler(accountController::getAllAccounts);
        router.route("/accounts*").handler(BodyHandler.create());
        router.post("/accounts").handler(accountController::createAccount);
        router.get("/accounts/:accountNumber").handler(accountController::getAccountByNumber);

        router.post("/accounts/:accountNumber/transfers").handler(accountController::createTransfer);

    }
}