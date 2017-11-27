package com.example.transferapi.resources;

import com.example.transferapi.model.Account;
import com.example.transferapi.model.Transaction;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.util.List;

import static org.junit.Assert.*;

public class TransactionTest  extends TestBase {


    /*
     * Test of the money transfer end-point
     */
    @Test
    public void testTransferMoney(){

        Account sourceAccount = this.createAccount("source account", 10);

        Account targetAccount = this.createAccount("target account", 10);

        double amount = 5;

        double sourceAccountStartBalance = sourceAccount.getBalance();
        double sourceAccountEndBalance = sourceAccountStartBalance - amount;

        double targetAccountStartBalance = targetAccount.getBalance();
        double targetAccountEndBalance = targetAccountStartBalance + amount;

        String message = "Sending money for the groceries";

        Form form = new Form();
        form.param("sourceAccountId", String.valueOf(sourceAccount.getUuid()));
        form.param("targetAccountId", String.valueOf(targetAccount.getUuid()));
        form.param("amount", String.valueOf(amount));
        form.param("message", message);

        Response response = target("transaction")
                .request()
                .post(Entity.form(form));

        assertEquals("Response should be 201 - OK", Response.Status.CREATED.getStatusCode(), response.getStatus());

        Transaction transaction = response.readEntity(Transaction.class);

        assertEquals("Source account must match", sourceAccount.getUuid(), transaction.getSourceAccount().intValue());
        assertEquals("Target account must match", targetAccount.getUuid(), transaction.getTargetAccount().intValue());
        assertEquals("Amount must match", amount, transaction.getAmount(), 0);
        assertEquals("Description must match", message, transaction.getMessage());
        assertEquals("Source account start balance must be valid", sourceAccountStartBalance, transaction.getSourceAccountStartBalance(), 0);
        assertEquals("Source account end balance must be valid", sourceAccountEndBalance, transaction.getSourceAccountEndBalance(), 0);
        assertEquals("Target account start balance must be valid", targetAccountStartBalance, transaction.getTargetAccountStartBalance(), 0);
        assertEquals("Target account end balance must be valid", targetAccountEndBalance, transaction.getTargetAccountEndBalance(), 0);

        sourceAccount = target("account")
                .path(String.valueOf(sourceAccount.getUuid()))
                .request()
                .get(Account.class);

        assertEquals("Funds must be deducted from source account", sourceAccountEndBalance, sourceAccount.getBalance(), 0 );

        targetAccount = target("account")
                .path(String.valueOf(targetAccount.getUuid()))
                .request()
                .get(Account.class);

        assertEquals("Funds must be added to the target account", targetAccountEndBalance, targetAccount.getBalance(), 0 );

    }

    /*
     * Test to transfer negative amount
     */
    @Test
    public void testTransferNegativeAmount(){

        Account sourceAccount = this.createAccount("source account", 10);

        Account targetAccount = this.createAccount("target account", 10);

        double amount = -5;

        String message = "Sending money for the groceries";

        Form form = new Form();
        form.param("sourceAccountId", String.valueOf(sourceAccount.getUuid()));
        form.param("targetAccountId", String.valueOf(targetAccount.getUuid()));
        form.param("amount", String.valueOf(amount));
        form.param("message", message);

        Response response = target("transaction")
                .request()
                .post(Entity.form(form));

        assertEquals("Response should be 400 - Bad request", Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    /*
     * Test to transfer from/to a non-existing account
     */
    @Test
    public void testTransferNonExistingAccounts(){

        Account sourceAccount = this.createAccount("source account", 10);

        double amount = 10;

        String message = "Sending money for the groceries";

        Form form = new Form();
        form.param("sourceAccountId", String.valueOf(sourceAccount.getUuid()));
        form.param("targetAccountId", "99999");
        form.param("amount", String.valueOf(amount));
        form.param("message", message);

        Response response = target("transaction")
                .request()
                .post(Entity.form(form));

        assertEquals("Response should be 404 - Not found", Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());

        form = new Form();
        form.param("sourceAccountId", "99999");
        form.param("targetAccountId", String.valueOf(sourceAccount.getUuid()));
        form.param("amount", String.valueOf(amount));
        form.param("message", message);

        response = target("transaction")
                .request()
                .post(Entity.form(form));

        assertEquals("Response should be 404 - Not found", Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    /*
     * Test to transfer from/to inactive accounts
     */
    @Test
    public void testTransferInactiveAccounts(){

        Account sourceAccount = this.createAccount("source account", 10);
        
        target("account")
                .path(String.valueOf(sourceAccount.getUuid()))
                .request()
                .delete();

        Account targetAccount = this.createAccount("target account", 10);

        double amount = 5;

        String message = "Sending money for the groceries";

        Form form = new Form();
        form.param("sourceAccountId", String.valueOf(sourceAccount.getUuid()));
        form.param("targetAccountId", String.valueOf(targetAccount.getUuid()));
        form.param("amount", String.valueOf(amount));
        form.param("message", message);

        Response response = target("transaction")
                .request()
                .post(Entity.form(form));

        assertEquals("Response should be 410 - Gone", Response.Status.GONE.getStatusCode(), response.getStatus());

        form = new Form();
        form.param("sourceAccountId", String.valueOf(targetAccount.getUuid()));
        form.param("targetAccountId", String.valueOf(sourceAccount.getUuid()));
        form.param("amount", String.valueOf(amount));
        form.param("message", message);

        response = target("transaction")
                .request()
                .post(Entity.form(form));

        assertEquals("Response should be 410 - Gone", Response.Status.GONE.getStatusCode(), response.getStatus());

    }

    /*
     * Test to perform a transfer from an account with insufficient funds
     */
    @Test
    public void testCheckSufficientFunds(){

        Account sourceAccount = this.createAccount("source account", 10);

        Account targetAccount = this.createAccount("target account", 10);

        double amount = 15;

        String message = "Sending money for the groceries";

        Form form = new Form();
        form.param("sourceAccountId", String.valueOf(sourceAccount.getUuid()));
        form.param("targetAccountId", String.valueOf(targetAccount.getUuid()));
        form.param("amount", String.valueOf(amount));
        form.param("message", message);

        Response response = target("transaction")
                .request()
                .post(Entity.form(form));

        assertEquals("Response should be 400 - Bad request", Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());

    }

    /*
     * Test to deposit an amount to an account
     */
    @Test
    public void testDeposit(){

        Account targetAccount = this.createAccount("target account", 10);

        double amount = 5;

        double targetAccountStartBalance = targetAccount.getBalance();
        double targetAccountEndBalance = targetAccountStartBalance + amount;

        Form form = new Form();
        form.param("targetAccountId", String.valueOf(targetAccount.getUuid()));
        form.param("amount", String.valueOf(amount));

        Response response = target("transaction")
                .path("deposit")
                .request()
                .post(Entity.form(form));

        assertEquals("Response should be 201 - OK", Response.Status.CREATED.getStatusCode(), response.getStatus());
        assertNotNull("Headers should include Location", response.getHeaderString("Location"));

        Transaction transaction = response.readEntity(Transaction.class);

        assertNull("Source account must be null", transaction.getSourceAccount());
        assertEquals("Target account must match", targetAccount.getUuid(), transaction.getTargetAccount().intValue());
        assertEquals("Amount must match", amount, transaction.getAmount(), 0);
        assertEquals("Source account start balance must be 0", 0, transaction.getSourceAccountStartBalance(), 0);
        assertEquals("Source account end balance must be 0", 0, transaction.getSourceAccountEndBalance(), 0);
        assertEquals("Target account start balance must be valid", targetAccountStartBalance, transaction.getTargetAccountStartBalance(), 0);
        assertEquals("Target account end balance must be valid", targetAccountEndBalance, transaction.getTargetAccountEndBalance(), 0);

        targetAccount = target("account")
                .path(String.valueOf(targetAccount.getUuid()))
                .request()
                .get(Account.class);

        assertEquals("Funds must be added to the target account", targetAccountEndBalance, targetAccount.getBalance(), 0 );

    }

    /*
     * Test to deposit into a non-existing/invalid account
     */
    @Test
    public void testDepositInvalidAccount(){

        double amount = 5;

        Form form = new Form();
        form.param("targetAccountId", "99999");
        form.param("amount", String.valueOf(amount));

        Response response = target("transaction")
                .path("deposit")
                .request()
                .post(Entity.form(form));

        assertEquals("Response should be 404 - Not found", Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());

        Account targetAccount = this.createAccount("target account", 10);

        target("account")
                .path(String.valueOf(targetAccount.getUuid()))
                .request()
                .delete();

        form = new Form();
        form.param("targetAccountId", String.valueOf(targetAccount.getUuid()));
        form.param("amount", String.valueOf(amount));

        response = target("transaction")
                .path("deposit")
                .request()
                .post(Entity.form(form));

        assertEquals("Response should be 410 - Not found", Response.Status.GONE.getStatusCode(), response.getStatus());
    }


    /*
     * Test to withdraw money from an account
     */
    @Test
    public void testWithdrawal(){

        Account sourceAccount = this.createAccount("source account", 10);

        double amount = 5;

        double sourceAccountStartBalance = sourceAccount.getBalance();
        double sourceAccountEndBalance = sourceAccountStartBalance - amount;

        Form form = new Form();
        form.param("sourceAccountId", String.valueOf(sourceAccount.getUuid()));
        form.param("amount", String.valueOf(amount));

        Response response = target("transaction")
                .path("withdraw")
                .request()
                .post(Entity.form(form));

        assertEquals("Response should be 201 - OK", Response.Status.CREATED.getStatusCode(), response.getStatus());

        Transaction transaction = response.readEntity(Transaction.class);

        assertEquals("Source account must match", sourceAccount.getUuid(), transaction.getSourceAccount().intValue());
        assertNull("Target account must be null", transaction.getTargetAccount());
        assertEquals("Amount must match", amount, transaction.getAmount(), 0);
        assertEquals("Source account start balance must be valid", sourceAccountStartBalance, transaction.getSourceAccountStartBalance(), 0);
        assertEquals("Source account end balance must be valid", sourceAccountEndBalance, transaction.getSourceAccountEndBalance(), 0);
        assertEquals("Target account start balance must be 0", 0, transaction.getTargetAccountStartBalance(), 0);
        assertEquals("Target account end balance must be 0", 0, transaction.getTargetAccountEndBalance(), 0);

        sourceAccount = target("account")
                .path(String.valueOf(sourceAccount.getUuid()))
                .request()
                .get(Account.class);

        assertEquals("Funds must be deducted from source account", sourceAccountEndBalance, sourceAccount.getBalance(), 0 );

    }

    /*
     * Test to withdraw money from an account with insufficient funds
     */
    @Test
    public void testWithdrawalSufficientFunds() {

        Account sourceAccount = this.createAccount("source account", 10);

        double amount = 15;

        Form form = new Form();
        form.param("sourceAccountId", String.valueOf(sourceAccount.getUuid()));
        form.param("amount", String.valueOf(amount));

        Response response = target("transaction")
                .path("withdraw")
                .request()
                .post(Entity.form(form));

        assertEquals("Response should be 400 - Bad request", Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());

    }

    /*
     * Test to withdraw from a non-existing/inactive account
     */
    @Test
    public void testWithdrawalInvalidAccounts(){

        double amount = 5;

        Form form = new Form();
        form.param("sourceAccountId", "99999");
        form.param("amount", String.valueOf(amount));

        Response response = target("transaction")
                .path("withdraw")
                .request()
                .post(Entity.form(form));

        assertEquals("Response should be 404 - Not found", Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());

        Account sourceAccount = this.createAccount("source account", 10);

        target("account")
                .path(String.valueOf(sourceAccount.getUuid()))
                .request()
                .delete();

        form = new Form();
        form.param("sourceAccountId", String.valueOf(sourceAccount.getUuid()));
        form.param("amount", String.valueOf(amount));

        response = target("transaction")
                .path("withdraw")
                .request()
                .post(Entity.form(form));

        assertEquals("Response should be 410 - Gone", Response.Status.GONE.getStatusCode(), response.getStatus());

    }

    /*
     * Test to get the list of transactions
     */
    @Test
    public void testGetListOfTransactions(){

        Account targetAccount = this.createAccount("target account", 10);

        Form form = new Form();
        form.param("targetAccountId", String.valueOf(targetAccount.getUuid()));
        form.param("amount", "5");

        target("transaction")
                .path("deposit")
                .request()
                .post(Entity.form(form));

        Response response = target("transaction")
                .request()
                .get();

        assertEquals("Get transactions response should be 200 - OK", Response.Status.OK.getStatusCode(), response.getStatus());

        List<Transaction> transactions = response.readEntity(new GenericType<List<Transaction>>() {});

        assertNotNull("Entity should not be null", transactions);

        assertNotSame("List should contain at least one element", transactions.size(), 0);
    }

    /*
     * Test to get a transfer by id
     */
    @Test
    public void testTransferById(){

        Account sourceAccount = this.createAccount("source account", 10);

        Account targetAccount = this.createAccount("target account", 10);

        double amount = 5;

        String message = "Sending money for the groceries";

        Form form = new Form();
        form.param("sourceAccountId", String.valueOf(sourceAccount.getUuid()));
        form.param("targetAccountId", String.valueOf(targetAccount.getUuid()));
        form.param("amount", String.valueOf(amount));
        form.param("message", message);

        Transaction createdTransaction = target("transaction")
                .request()
                .post(Entity.form(form), Transaction.class);


        Response response = target("transaction")
                .path(String.valueOf(createdTransaction.getUuid()))
                .request()
                .get();

        assertEquals("Get transactions response should be 200 - OK", Response.Status.OK.getStatusCode(), response.getStatus());

        Transaction queriedTransaction = response.readEntity(Transaction.class);

        assertNotNull("Response should include entity", queriedTransaction);
        assertEquals("Transactions should have the same id", createdTransaction.getUuid(), queriedTransaction.getUuid());

    }

}
