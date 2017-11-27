package com.example.transferapi.controller;

import com.example.transferapi.model.Account;
import com.example.transferapi.model.Transaction;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ValidationHelper {


    private final static Logger L = Logger.getLogger(ValidationHelper.class.getName());

    public static void checkAccountExists(int accountId){

        if(BankController.getInstance().getBank().getAccounts().size()<=accountId){

            String message = String.format("Account %s does not exist", accountId);

            L.log(Level.WARNING, message);

            throw new WebApplicationException(message, Response.Status.NOT_FOUND);
        }
    }

    public static void checkAccountActive(int accountId){

        checkAccountExists(accountId);

        Account account = BankController.getInstance().getBank().getAccounts().get(accountId);

        if(account.getStatus().equals(Account.AccountStatus.INACTIVE)){
            String message = String.format("Account %s is inactive", accountId);

            L.log(Level.WARNING, message);

            throw new WebApplicationException(message, Response.Status.GONE);
        }
    }

    public static void checkAmountNotNegative(double amount){

        if(amount<0){

            String message = amount + " is not a valid value";

            L.log(Level.WARNING, message);

            throw new WebApplicationException(message, Response.Status.BAD_REQUEST);
        }

    }

    public static void checkAmountPositive(double amount){

        if(amount<=0){
            String message = amount + " is not a positive amount";

            L.log(Level.WARNING, message);

            throw new WebApplicationException(message, Response.Status.BAD_REQUEST);
        }

    }

    public static void checkTransactionExists(int transactionId) {

        Transaction transaction = BankController.getInstance().getBank().getTransactions().get(transactionId);

        if(transaction==null){
            String message = String.format("Transaction %s does not exist", transactionId);

            L.log(Level.WARNING, message);
            throw new WebApplicationException(message, Response.Status.NOT_FOUND);
        }

    }
}
