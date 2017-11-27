package com.example.transferapi.model;

import java.util.ArrayList;
import java.util.List;

public class Bank {

    private List<Account> accounts;

    private List<Transaction> transactions;

    public List<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
}

    public Bank() {
        this.setAccounts(new ArrayList<Account>());
        this.setTransactions(new ArrayList<Transaction>());
    }


}
