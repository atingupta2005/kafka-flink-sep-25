package com.example.flink;

public class Transaction {
    public String userId;
    public String category;
    public double amount;
    public long timestamp;

    // Default constructor for Jackson / Flink
    public Transaction() {}

    public Transaction(String userId, String category, double amount, long timestamp) {
        this.userId = userId;
        this.category = category;
        this.amount = amount;
        this.timestamp = timestamp;
    }
}
