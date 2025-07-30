package com.example;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

import com.example.account.Account;
import com.example.customer.Customer;
import com.example.customer.TransactionLog;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class TransferService {

    @Transactional
    public void transfer(Long fromAccountId, Long toAccountId, BigDecimal amount) {
        transfer(fromAccountId, toAccountId, amount, false);
    }

    @Transactional
    public void transfer(Long fromAccountId, Long toAccountId, BigDecimal amount, boolean simulateFailure) {
        // === SQL SERVER OPERATIONS (Accounts) ===
        Account fromAccount = Account.findById(fromAccountId);
        Account toAccount = Account.findById(toAccountId);

        Objects.requireNonNull(fromAccount, "Source account not found");
        Objects.requireNonNull(toAccount, "Destination account not found");

        if (fromAccount.balance.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient funds!");
        }

        // Update account balances in SQL Server
        fromAccount.balance = fromAccount.balance.subtract(amount);
        toAccount.balance = toAccount.balance.add(amount);
        fromAccount.persist();
        toAccount.persist();

        // === POSTGRESQL OPERATIONS (Customers) ===
        // Update customer records to reflect the transaction
        Customer fromCustomer = Customer.findById(fromAccount.customerId);
        Customer toCustomer = Customer.findById(toAccount.customerId);

        Objects.requireNonNull(fromCustomer, "Source customer not found");
        Objects.requireNonNull(toCustomer, "Destination customer not found");

        // Update customer names to show transaction activity (PostgreSQL)
        fromCustomer.name = fromCustomer.name.replaceAll(" \\(.*\\)", "") + " (Sent $" + amount + ")";
        toCustomer.name = toCustomer.name.replaceAll(" \\(.*\\)", "") + " (Received $" + amount + ")";
        fromCustomer.persist();
        toCustomer.persist();

        // === XA TRANSACTION DEMO ===
        // Optional failure simulation - will rollback BOTH SQL Server AND PostgreSQL
        // changes
        if (simulateFailure) {
            throw new RuntimeException("Simulated failure to demonstrate XA rollback!");
        }
    }

    // ===========================
    // SCENARIO 1: LOYALTY POINTS + ACCOUNT TRANSFER
    // ===========================
    @Transactional
    public void transferWithLoyalty(Long fromAccountId, Long toAccountId, BigDecimal amount, boolean simulateFailure) {
        // === SQL SERVER OPERATIONS (Accounts) ===
        Account fromAccount = Account.findById(fromAccountId);
        Account toAccount = Account.findById(toAccountId);

        Objects.requireNonNull(fromAccount, "Source account not found");
        Objects.requireNonNull(toAccount, "Destination account not found");

        if (fromAccount.balance.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient funds!");
        }

        // Update account balances and transaction counts
        fromAccount.balance = fromAccount.balance.subtract(amount);
        toAccount.balance = toAccount.balance.add(amount);
        fromAccount.transactionCount++;
        toAccount.transactionCount++;
        fromAccount.lastTransactionDate = LocalDateTime.now();
        toAccount.lastTransactionDate = LocalDateTime.now();
        fromAccount.persist();
        toAccount.persist();

        // === POSTGRESQL OPERATIONS (Customers & Logs) ===
        // Award loyalty points to sender
        Customer fromCustomer = Customer.findById(fromAccount.customerId);
        Objects.requireNonNull(fromCustomer, "Source customer not found");

        int loyaltyPoints = calculateLoyaltyPoints(amount);
        fromCustomer.loyaltyPoints += loyaltyPoints;
        fromCustomer.totalTransactions++;
        fromCustomer.lastLoginDate = LocalDateTime.now();
        fromCustomer.persist();

        // Log transaction in PostgreSQL
        TransactionLog log = new TransactionLog();
        log.customerId = fromAccount.customerId;
        log.fromAccountId = fromAccountId;
        log.toAccountId = toAccountId;
        log.amount = amount;
        log.transactionType = "TRANSFER";
        log.status = "SUCCESS";
        log.description = "Transfer with " + loyaltyPoints + " loyalty points awarded";
        log.persist();

        // Optional failure simulation
        if (simulateFailure) {
            throw new RuntimeException("Simulated failure - loyalty points and transfer will be rolled back!");
        }
    }

    // ===========================
    // SCENARIO 2: ACCOUNT LIMITS + CUSTOMER STATUS
    // ===========================
    @Transactional
    public void transferWithLimits(Long accountId, BigDecimal amount, boolean simulateFailure) {
        // === SQL SERVER OPERATIONS (Account) ===
        Account account = Account.findById(accountId);
        Objects.requireNonNull(account, "Account not found");

        if (account.balance.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient funds!");
        }

        if (account.availableLimit.compareTo(amount) < 0) {
            throw new IllegalStateException("Daily limit exceeded! Available: $" + account.availableLimit);
        }

        // Update account limits and balance
        account.availableLimit = account.availableLimit.subtract(amount);
        account.balance = account.balance.subtract(amount);
        account.transactionCount++;
        account.lastTransactionDate = LocalDateTime.now();
        account.persist();

        // === POSTGRESQL OPERATIONS (Customer Status) ===
        Customer customer = Customer.findById(account.customerId);
        Objects.requireNonNull(customer, "Customer not found");

        customer.totalTransactions++;

        // Auto-upgrade customer status based on activity
        if (customer.totalTransactions >= 100 && "STANDARD".equals(customer.status)) {
            customer.status = "PREMIUM";
            customer.loyaltyPoints += 1000; // Bonus points for premium upgrade
        }
        customer.persist();

        // Log the transaction and status change
        TransactionLog log = new TransactionLog();
        log.customerId = account.customerId;
        log.fromAccountId = accountId;
        log.amount = amount;
        log.transactionType = "WITHDRAWAL";
        log.status = "SUCCESS";
        log.description = "Withdrawal with limit check. Customer status: " + customer.status;
        log.persist();

        // Optional failure simulation
        if (simulateFailure) {
            throw new RuntimeException("Simulated failure - account and customer status changes will be rolled back!");
        }
    }

    // ===========================
    // SCENARIO 3: ACCOUNT FREEZE + CUSTOMER NOTIFICATION
    // ===========================
    @Transactional
    public void freezeAccountWithNotification(Long accountId, String reason, boolean simulateFailure) {
        // === SQL SERVER OPERATIONS (Account) ===
        Account account = Account.findById(accountId);
        Objects.requireNonNull(account, "Account not found");

        String previousStatus = account.status;
        account.status = "FROZEN";
        account.lastTransactionDate = LocalDateTime.now();
        account.persist();

        // === POSTGRESQL OPERATIONS (Customer & Logs) ===
        Customer customer = Customer.findById(account.customerId);
        Objects.requireNonNull(customer, "Customer not found");

        String previousCustomerStatus = customer.status;
        customer.status = "SUSPENDED";
        customer.updatedAt = LocalDateTime.now();
        customer.persist();

        // Log the freeze action
        TransactionLog log = new TransactionLog();
        log.customerId = account.customerId;
        log.fromAccountId = accountId;
        log.transactionType = "FREEZE";
        log.status = "SUCCESS";
        log.description = "Account frozen: " + reason +
                ". Account status: " + previousStatus + " -> FROZEN" +
                ". Customer status: " + previousCustomerStatus + " -> SUSPENDED";
        log.persist();

        // Optional failure simulation
        if (simulateFailure) {
            throw new RuntimeException(
                    "Simulated failure - account freeze and customer suspension will be rolled back!");
        }
    }

    // ===========================
    // UTILITY METHODS
    // ===========================
    private int calculateLoyaltyPoints(BigDecimal amount) {
        // 1 point per $10 transferred, minimum 1 point
        return Math.max(1, amount.divide(new BigDecimal("10")).intValue());
    }
}