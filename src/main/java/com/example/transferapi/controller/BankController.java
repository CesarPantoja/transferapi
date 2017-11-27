package com.example.transferapi.controller;

import com.example.transferapi.model.Account;
import com.example.transferapi.model.Bank;
import com.example.transferapi.model.Transaction;

import java.util.logging.Logger;

public class BankController {

    private final static Logger L = Logger.getLogger(BankController.class.getName());

    private Bank bank;

    public Bank getBank() {
        return bank;
    }

    public void setBank(Bank bank) {
        this.bank = bank;
    }

    public synchronized void addAccount(Account account) {

        account.setUuid(this.getBank().getAccounts().size());
        this.getBank().getAccounts().add(account);
    }

    public Account addAccount(String name, double startBalance) {

        Account account = new Account();
        account.setName(name);
        account.setStatus(Account.AccountStatus.ACTIVE);
        account.setBalance(startBalance);
        this.addAccount(account);

        Transaction transaction = new Transaction();
        transaction.setTargetAccount(account.getUuid());
        transaction.setTargetAccountStartBalance(0);
        transaction.setTargetAccountEndBalance(startBalance);
        transaction.setAmount(startBalance);
        transaction.setMessage("Starting balance of account "+account.getUuid());
        this.addTransaction(transaction);

        return account;

    }


    public synchronized void addTransaction(Transaction transaction){
        transaction.setUuid(this.getBank().getTransactions().size());
        this.getBank().getTransactions().add(transaction);
    }

    public Transaction transferMoney(Account sourceAccount, Account targetAccount, double amount, String message){

        double sourceAccountStartBalance = sourceAccount.getBalance();
        double targetAccountStartBalance = targetAccount.getBalance();

        double sourceAccountEndBalance = sourceAccountStartBalance - amount;
        double targetAccountEndBalance = targetAccountStartBalance + amount;

        sourceAccount.setBalance(sourceAccountEndBalance);
        targetAccount.setBalance(targetAccountEndBalance);

        Transaction transaction = new Transaction();
        transaction.setSourceAccount(sourceAccount.getUuid());
        transaction.setTargetAccount(targetAccount.getUuid());
        transaction.setAmount(amount);
        transaction.setMessage(message);
        transaction.setSourceAccountStartBalance(sourceAccountStartBalance);
        transaction.setSourceAccountEndBalance(sourceAccountEndBalance);
        transaction.setTargetAccountStartBalance(targetAccountStartBalance);
        transaction.setTargetAccountEndBalance(targetAccountEndBalance);

        this.addTransaction(transaction);

        return transaction;

    }

    public Transaction depositMoney(Account targetAccount, double amount){

        double targetAccountStartBalance = targetAccount.getBalance();

        double targetAccountEndBalance = targetAccountStartBalance + amount;

        targetAccount.setBalance(targetAccountEndBalance);

        Transaction transaction = new Transaction();
        transaction.setTargetAccount(targetAccount.getUuid());
        transaction.setAmount(amount);
        transaction.setMessage(String.format("Deposit of $%f into account %s", amount, targetAccount.getUuid()));
        transaction.setTargetAccountStartBalance(targetAccountStartBalance);
        transaction.setTargetAccountEndBalance(targetAccountEndBalance);

        this.addTransaction(transaction);

        return transaction;
    }


    public Transaction withdrawMoney(Account sourceAccount, double amount) {

        double sourceAccountStartBalance = sourceAccount.getBalance();

        double sourceAccountEndBalance = sourceAccountStartBalance - amount;

        sourceAccount.setBalance(sourceAccountEndBalance);

        Transaction transaction = new Transaction();
        transaction.setSourceAccount(sourceAccount.getUuid());
        transaction.setAmount(amount);
        transaction.setMessage(String.format("Withdraw of $%f from account %s", amount, sourceAccount.getUuid()));
        transaction.setSourceAccountStartBalance(sourceAccountStartBalance);
        transaction.setSourceAccountEndBalance(sourceAccountEndBalance);

        this.addTransaction(transaction);

        return transaction;

    }

    public void deactivateAccount(Account account){

        account.setStatus(Account.AccountStatus.INACTIVE);
        account.setStatus(Account.AccountStatus.INACTIVE);

    }


    protected BankController() {
        this.setBank(new Bank());
    }

    //Singleton
    private static BankController instance;

    public static BankController getInstance(){

        if(instance == null){
            instance = new BankController();
        }

        return instance;
    }
}
