package test.adanielssr.simple.money.transfer.api.controller;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import test.adanielssr.simple.money.transfer.business.service.AccountService;
import test.adanielssr.simple.money.transfer.business.service.TransferService;
import test.adanielssr.simple.money.transfer.business.service.exceptions.AccountAlreadyExistsException;
import test.adanielssr.simple.money.transfer.business.service.exceptions.AccountNotFoundException;
import test.adanielssr.simple.money.transfer.business.service.exceptions.SimpleMoneyTransferException;
import test.adanielssr.simple.money.transfer.business.service.exceptions.TransferValidationException;
import test.adanielssr.simple.money.transfer.domain.model.Account;
import test.adanielssr.simple.money.transfer.domain.model.Transfer;

/**
 * Created by arodrigues on 13/08/2017.
 */
public class AccountController {

    private final AccountService accountService;

    private final TransferService transferService;

    public AccountController(AccountService accountService, TransferService transferService) {
        this.accountService = accountService;
        this.transferService = transferService;
    }

    public void getAllAccounts(RoutingContext routingContext) {
        createJsonHttpResponse(routingContext).end(Json.encodePrettily(accountService.getAllAccounts()));
    }

    public void createAccount(RoutingContext routingContext) {
        HttpServerResponse jsonHttpResponse = createJsonHttpResponse(routingContext);
        try {
            jsonHttpResponse.setStatusCode(HttpResponseStatus.CREATED.code()).end(Json.encodePrettily(
                    accountService.createAccount(Json.decodeValue(routingContext.getBodyAsString(), Account.class))));
        } catch (AccountAlreadyExistsException e) {
            jsonHttpResponse.setStatusCode(HttpResponseStatus.CONFLICT.code()).end(createJsonError(e.getMessage()));
        } catch (SimpleMoneyTransferException e) {
            jsonHttpResponse.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
                    .end(createJsonError(e.getMessage()));
        }
    }

    public void getAccountByNumber(RoutingContext routingContext) {
        HttpServerResponse jsonHttpResponse = createJsonHttpResponse(routingContext);

        Long accountNumber = retrieveAndValidateAccountNumber(routingContext, jsonHttpResponse);
        if (accountNumber != null) {
            try {
                jsonHttpResponse.end(Json.encodePrettily(accountService.getAccountByNumber(accountNumber)));
            } catch (AccountNotFoundException e) {
                jsonHttpResponse.setStatusCode(HttpResponseStatus.NOT_FOUND.code())
                        .end(createJsonError(e.getMessage()));
            } catch (SimpleMoneyTransferException e) {
                jsonHttpResponse.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
                        .end(createJsonError(e.getMessage()));
            }
        }
    }

    public void createTransfer(RoutingContext routingContext) {
        HttpServerResponse jsonHttpResponse = createJsonHttpResponse(routingContext);

        Long accountNumber = retrieveAndValidateAccountNumber(routingContext, jsonHttpResponse);
        if (accountNumber != null) {
            Transfer transfer = Json.decodeValue(routingContext.getBodyAsString(), Transfer.class);
            transfer.setAccountNumberFrom(accountNumber);

            try {
                jsonHttpResponse.end(Json.encodePrettily(transferService.createAndPerformTransfer(transfer)));
            } catch (AccountNotFoundException e) {
                jsonHttpResponse.setStatusCode(HttpResponseStatus.NOT_FOUND.code())
                        .end(createJsonError(e.getMessage()));
            } catch (TransferValidationException e) {
                jsonHttpResponse.setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
                        .end(createJsonError(e.getMessage()));
            } catch (SimpleMoneyTransferException e) {
                jsonHttpResponse.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
                        .end(createJsonError(e.getMessage()));
            }
        }
    }

    private HttpServerResponse createJsonHttpResponse(RoutingContext routingContext) {
        return routingContext.response().putHeader("content-type", "application/json; charset=utf-8");
    }

    private String createJsonError(String error) {
        return "{\"error\": \"" + error + "\"}";
    }

    private Long retrieveAndValidateAccountNumber(RoutingContext routingContext,
            HttpServerResponse httpServerResponse) {
        String accountNumberParam = routingContext.request().getParam("accountNumber");
        if (accountNumberParam == null) {
            httpServerResponse.setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
                    .end(createJsonError("accountNumber parameter is required"));
            return null;
        } else {
            try {
                return Long.parseLong(accountNumberParam);
            } catch (NumberFormatException e) {
                httpServerResponse.setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
                        .end(createJsonError("accountNumber format is not a long"));
                return null;
            }
        }
    }
}
