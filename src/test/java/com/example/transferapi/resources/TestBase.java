package com.example.transferapi.resources;

import com.example.transferapi.model.Account;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Form;

public abstract class TestBase extends JerseyTest {

    @Override
    public Application configure() {
        //enable(TestProperties.LOG_TRAFFIC);
        //enable(TestProperties.DUMP_ENTITY);
        return new ResourceConfig().packages("com.example.transferapi");
    }

    /*
     * Helper method to create an account
     */
    protected Account createAccount(){

        Form form = new Form();
        form.param("name", "test account");

        return target("account")
                .request()
                .post(Entity.form(form), Account.class);
    }

    /*
     * Helper method to create an account, specifying the name and amount
     */
    protected Account createAccount(String name, double amount){

        Form form = new Form();
        form.param("name", name);
        form.param("startBalance", String.valueOf(amount));

        return target("account")
                .request()
                .post(Entity.form(form), Account.class);
    }
}
