package com.example.transferapi.resources;

import com.example.transferapi.controller.BankController;
import com.example.transferapi.controller.ValidationHelper;
import com.example.transferapi.model.Account;

import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;
import java.util.logging.Logger;

@Path("account")
public class AccountResource {

    private final static Logger L = Logger.getLogger(AccountResource.class.getName());

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createAccount(
            @FormParam("name") @NotNull String name,
            @FormParam("startBalance") double startBalance
    ){

        L.info(String.format("Creating account \"%s\" with starting balance %f", name, startBalance));

        ValidationHelper.checkAmountNotNegative(startBalance);

        BankController bankController = BankController.getInstance();

        Account account = bankController.addAccount(name, startBalance);

        return Response.created(URI.create("account/"+account.getUuid())).entity(account).build();

    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Account> getAccounts(){
        L.info("Getting all accounts");
        return BankController.getInstance().getBank().getAccounts();
    }


    @GET
    @Path("{accountId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Account getAccount(@PathParam("accountId")int accountId){

        L.info(String.format("Getting account with id %s", accountId));

        ValidationHelper.checkAccountExists(accountId);

        return BankController.getInstance().getBank().getAccounts().get(accountId);
    }

    @PUT
    @Path("{accountId}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Account updateAccount(
            @PathParam("accountId")int accountId,
            @FormParam("name") @NotNull String name
    ){

        L.info(String.format("Updating name of account id %s. New name: %s", accountId, name));

        ValidationHelper.checkAccountActive(accountId);

        Account account = BankController.getInstance().getBank().getAccounts().get(accountId);

        if(account.getName().equals(name)){
            L.warning("Account has the same name. Not changed.");
            throw new WebApplicationException(Response.Status.NOT_MODIFIED);
        }

        account.setName(name);

        return account;
    }

    @DELETE
    @Path("{accountId}")
    @Produces({MediaType.APPLICATION_JSON})
    public Account deactivateAccount(@PathParam("accountId")int accountId){

        L.info(String.format("Deactivating account id %s", accountId));

        ValidationHelper.checkAccountActive(accountId);

        BankController bankController = BankController.getInstance();

        Account account = bankController.getBank().getAccounts().get(accountId);

        synchronized (bankController) {
            bankController.deactivateAccount(account);
        }

        return account;

    }

}
