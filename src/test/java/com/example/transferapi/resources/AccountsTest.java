package com.example.transferapi.resources;

import com.example.transferapi.model.Account;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

import static org.junit.Assert.*;

public class AccountsTest extends TestBase {

    /*
     * Test to check account name is required when creating an account
     */
    @Test
    public void testCreateAccountNoName() {

        Response response = target("account")
                .request()
                .post(Entity.entity(null, MediaType.APPLICATION_FORM_URLENCODED_TYPE));

        assertEquals("Response should be 400 - Bad Request", Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    /*
     * Test to check starting balance should be positive when creating an account
     */
    @Test
    public void testCreateAccountNegativeBalance() {

        Form form = new Form();
        form.param("name", "test account");
        form.param("startBalance", "-1");

        Response response = target("account")
                .request()
                .post(Entity.form(form));

        assertEquals("Response should be 400 - Bad Request", Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());

    }

    /*
     * Test to check the response code of a correct account creation
     */
    @Test
    public void testCreateAccountResponseCode(){
        Form form = new Form();
        form.param("name", "test account");

        Response response = target("account")
                .request()
                .post(Entity.form(form));

        assertEquals("Response should be 201 - Created", Response.Status.CREATED.getStatusCode(), response.getStatus());
    }

    /*
     * Test to check the headers in a correct account creation
     */
    @Test
    public void testCreateAccountResponseHeader(){
        Form form = new Form();
        form.param("name", "test account");

        Response response = target("account")
                .request()
                .post(Entity.form(form));

        assertNotNull("Headers should include Location", response.getHeaderString("Location"));
    }

    /*
     * Test to check accounts can be created with no starting balance
     */
    @Test
    public void testCreateAccountNoBalance() {

        Form form = new Form();
        form.param("name", "test account");

        Response response = target("account")
                .request()
                .post(Entity.form(form));

        Account account = response.readEntity(Account.class);

        assertEquals("Created account should have balance 0", 0, account.getBalance(), 0);
    }

    /*
     * Test to check account creation handles name correctly
     */
    @Test
    public void testCreateAccountName() {

        String accountName = "Test account";

        Account account = this.createAccount(accountName, 0);

        assertEquals("Created account should have the correct name", accountName, account.getName());
    }

    /*
     * Test to check account creation should create active accounts
     */
    @Test
    public void testCreateAccountisActive() {

        Account account = this.createAccount();

        assertEquals("Created account should be active", Account.AccountStatus.ACTIVE, account.getStatus());
    }


    /*
     * Test to check accounts are created with the correct start balance
     */
    @Test
    public void testCreateAccountStartBalance() {

        Account account = this.createAccount("test account", 10);

        assertEquals("Created account should have the correct starting balance",  10, account.getBalance(),0);
    }

    /*
     * Test of get all accounts
     */
    @Test
    public void testGetAllAccounts(){

        this.createAccount();

        Response response = target("account")
                .request()
                .get();

        assertEquals("Get accounts response should be 200 - OK", Response.Status.OK.getStatusCode(), response.getStatus());

        List<Account> accounts = response.readEntity(new GenericType<List<Account>>() {});

        assertNotNull("Entity should not be null", accounts);

        assertNotSame("List should contain at least one element", accounts.size(), 0);
    }

    /*
     * Test to get an account by id
     */
    @Test
    public void testGetAccountById(){

        Account accountCreated = this.createAccount();

        Response response = target("account")
                .path(String.valueOf(accountCreated.getUuid()))
                .request()
                .get();

        assertEquals("Get accounts response should be 200 - OK", Response.Status.OK.getStatusCode(), response.getStatus());

        Account accountQueried = response.readEntity(Account.class);

        assertNotNull("Queried account should not be null", accountQueried);

        assertEquals("Queried account should have same id as created account", accountQueried.getUuid(), accountCreated.getUuid());

    }

    /*
     * Test to get a non-existent account
     */
    @Test
    public void testGetNonExistentAccount(){

        Response response = target("account")
                .path("999999")
                .request()
                .get();

        assertEquals("Get accounts response should be 404 - Not found", Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());

    }

    /*
     * Test of account name update
     */
    @Test
    public void testUpdateAccountName(){

        String originalName = "Original name";
        String newName = "New name";

        Account accountCreated = this.createAccount(originalName, 0);

        Form form = new Form();
        form.param("name", newName);

        Response response = target("account")
                .path(String.valueOf(accountCreated.getUuid()))
                .request()
                .put(Entity.form(form));

        assertEquals("Response should be 200 - OK", Response.Status.OK.getStatusCode(), response.getStatus());

        Account updatedAccount = response.readEntity(Account.class);

        assertEquals("Queried account should have same id as created account", accountCreated.getUuid(), updatedAccount.getUuid());
        assertEquals("Account should have the new name", newName, updatedAccount.getName());

    }

    /*
     * Test of account name update with same name
     */
    @Test
    public void testUpdateAccountSameName(){

        String originalName = "Original name";

        Account accountCreated = this.createAccount(originalName, 0);

        Form form = new Form();
        form.param("name", originalName);

        Response response = target("account")
                .path(String.valueOf(accountCreated.getUuid()))
                .request()
                .put(Entity.form(form));

        assertEquals("Response should be 304 - Not modified", Response.Status.NOT_MODIFIED.getStatusCode(), response.getStatus());
    }

    /*
     * Test of account deactivation
     */
    @Test
    public void testDeactivateAccount(){

        Account accountCreated = this.createAccount();

        Response response = target("account")
                .path(String.valueOf(accountCreated.getUuid()))
                .request()
                .delete();

        accountCreated = response.readEntity(Account.class);

        assertEquals("Account should be inactive", Account.AccountStatus.INACTIVE, accountCreated.getStatus());

    }

    /*
     * Test of account deactivation of an already deactivated account
     */
    @Test
    public void testDeactivateInactiveAccount(){

        Account accountCreated = this.createAccount();

        Response response = target("account")
                .path(String.valueOf(accountCreated.getUuid()))
                .request()
                .delete();

        accountCreated = response.readEntity(Account.class);

        response = target("account")
                .path(String.valueOf(accountCreated.getUuid()))
                .request()
                .delete();

        assertEquals("Response should be 410 - Gone", Response.Status.GONE.getStatusCode(), response.getStatus());

    }

    /*
     * Test to update the name of an inactive account
     */
    @Test
    public void testUpdateInactiveAccount(){

        Account accountCreated = this.createAccount();

        Response response = target("account")
                .path(String.valueOf(accountCreated.getUuid()))
                .request()
                .delete();

        accountCreated = response.readEntity(Account.class);

        Form form = new Form();
        form.param("name", "new name");

        response = target("account")
                .path(String.valueOf(accountCreated.getUuid()))
                .request()
                .put(Entity.form(form));

        assertEquals("Response should be 410 - Gone", Response.Status.GONE.getStatusCode(), response.getStatus());

    }

}
