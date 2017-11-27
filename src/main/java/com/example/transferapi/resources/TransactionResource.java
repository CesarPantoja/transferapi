package com.example.transferapi.resources;

import com.example.transferapi.controller.BankController;
import com.example.transferapi.controller.ValidationHelper;
import com.example.transferapi.model.Account;
import com.example.transferapi.model.Transaction;

import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;
import java.util.logging.Logger;

@Path("transaction")
public class TransactionResource {

    private final static Logger L = Logger.getLogger(TransactionResource.class.getName());

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Transaction> getTransactions(){

        L.info("Getting all transactions");

        return BankController.getInstance().getBank().getTransactions();

    }

    @GET
    @Path("{transactionId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Transaction getTransaction(@PathParam("transactionId") int transactionId){

        L.info(String.format("Getting transaction with id %s", transactionId));

        ValidationHelper.checkTransactionExists(transactionId);

        return BankController.getInstance().getBank().getTransactions().get(transactionId);

    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response transferMoney(
            @FormParam("sourceAccountId") @NotNull int sourceAccountId,
            @FormParam("targetAccountId") @NotNull int targetAccountId,
            @FormParam("amount") @NotNull double amount,
            @FormParam("message") @NotNull String message
    ){

        L.info(String.format("Transferring %f from account %s to account %s with message: \"%s\"", amount, sourceAccountId, targetAccountId, message));

        ValidationHelper.checkAmountPositive(amount);
        ValidationHelper.checkAccountActive(sourceAccountId);
        ValidationHelper.checkAccountActive(targetAccountId);

        BankController bankController = BankController.getInstance();

        Account sourceAccount = bankController.getBank().getAccounts().get(sourceAccountId);
        Account targetAccount = bankController.getBank().getAccounts().get(targetAccountId);

        synchronized (bankController) {

            if (sourceAccount.getBalance() < amount) {
                String exception = String.format("Account %s does not have enough funds for this transfer", sourceAccountId);
                L.warning(exception);
                throw new WebApplicationException(exception, Response.Status.BAD_REQUEST);
            }

            Transaction transaction =  bankController.transferMoney(sourceAccount, targetAccount, amount, message);

            return Response.created(URI.create("transaction/"+transaction.getUuid())).entity(transaction).build();

        }

    }


    @POST
    @Path("deposit")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response depositMoney(
            @FormParam("targetAccountId") @NotNull int targetAccountId,
            @FormParam("amount") @NotNull double amount
    ){

        L.info(String.format("Depositing %f into account %s", amount, targetAccountId));

        ValidationHelper.checkAccountActive(targetAccountId);
        ValidationHelper.checkAmountPositive(amount);

        BankController bankController = BankController.getInstance();

        Account targetAccount = bankController.getBank().getAccounts().get(targetAccountId);

        synchronized (bankController) {

            Transaction transaction = bankController.depositMoney(targetAccount, amount);

            return Response.created(URI.create("transaction/" + transaction.getUuid())).entity(transaction).build();

        }
    }

    @POST
    @Path("withdraw")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response withdrawMoney(
            @FormParam("sourceAccountId") @NotNull int sourceAccountId,
            @FormParam("amount") @NotNull double amount
    ){
        L.info(String.format("Withdrawing %f from account %s", amount, sourceAccountId));

        ValidationHelper.checkAccountActive(sourceAccountId);
        ValidationHelper.checkAmountPositive(amount);

        BankController bankController = BankController.getInstance();

        Account sourceAccount = bankController.getBank().getAccounts().get(sourceAccountId);

        synchronized (bankController) {

            if (sourceAccount.getBalance() < amount) {
                String exception = String.format("Account %s does not have enough funds for this withdrawal", sourceAccountId);
                L.warning(exception);
                throw new WebApplicationException(exception, Response.Status.BAD_REQUEST);
            }

            Transaction transaction = bankController.withdrawMoney(sourceAccount, amount);

            return Response.created(URI.create("transaction/" + transaction.getUuid())).entity(transaction).build();
        }
    }


}
