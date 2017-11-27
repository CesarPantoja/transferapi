package com.example.transferapi.model;

public class Transaction {

    private int Uuid;

    private Integer sourceAccount;

    private Integer targetAccount;

    private double amount;

    private double sourceAccountStartBalance;

    private double sourceAccountEndBalance;

    private double targetAccountStartBalance;

    private double targetAccountEndBalance;

    private String message;

    public int getUuid() {
        return Uuid;
    }

    public void setUuid(int uuid) {
        Uuid = uuid;
    }

    public Integer getSourceAccount() {
        return sourceAccount;
    }

    public void setSourceAccount(Integer sourceAccount) {
        this.sourceAccount = sourceAccount;
    }

    public Integer getTargetAccount() {
        return targetAccount;
    }

    public void setTargetAccount(Integer targetAccount) {
        this.targetAccount = targetAccount;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getSourceAccountStartBalance() {
        return sourceAccountStartBalance;
    }

    public void setSourceAccountStartBalance(double sourceAccountStartBalance) {
        this.sourceAccountStartBalance = sourceAccountStartBalance;
    }

    public double getSourceAccountEndBalance() {
        return sourceAccountEndBalance;
    }

    public void setSourceAccountEndBalance(double sourceAccountEndBalance) {
        this.sourceAccountEndBalance = sourceAccountEndBalance;
    }

    public double getTargetAccountStartBalance() {
        return targetAccountStartBalance;
    }

    public void setTargetAccountStartBalance(double targetAccountStartBalance) {
        this.targetAccountStartBalance = targetAccountStartBalance;
    }

    public double getTargetAccountEndBalance() {
        return targetAccountEndBalance;
    }

    public void setTargetAccountEndBalance(double targetAccountEndBalance) {
        this.targetAccountEndBalance = targetAccountEndBalance;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
